package org.example.project_cuoiky_congnghephanmem_oose.service.payment.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.response.PaymentUrlResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Payment;
import org.example.project_cuoiky_congnghephanmem_oose.entity.state.BookingStatus;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ICustomerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IMembershipTierRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IPaymentRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IEmailService;
import org.example.project_cuoiky_congnghephanmem_oose.service.payment.IPaymentService;
import org.example.project_cuoiky_congnghephanmem_oose.util.VnPayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private static final long HOLD_MINUTES = 15;

    private final IBookingRepository bookingRepository;
    private final IPaymentRepository paymentRepository;
    private final ICustomerRepository customerRepository;
    private final IMembershipTierRepository membershipTierRepository;
    private final IEmailService emailService;
    private final VnPayUtil vnPayUtil;

    public PaymentServiceImpl(IBookingRepository bookingRepository,
                              IPaymentRepository paymentRepository,
                              ICustomerRepository customerRepository,
                              IMembershipTierRepository membershipTierRepository,
                              IEmailService emailService,
                              VnPayUtil vnPayUtil) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.membershipTierRepository = membershipTierRepository;
        this.emailService = emailService;
        this.vnPayUtil = vnPayUtil;
    }

    @Override
    @Transactional
    public PaymentUrlResponse createVnPayUrl(String username, int bookingId, String ipAddress) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (!booking.getCustomer().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền thanh toán booking này");
        }

        syncBookingState(booking);

        if (!booking.canBePaid()) {
            throw new RuntimeException("Booking này không còn ở trạng thái chờ thanh toán");
        }

        if (booking.getExpiredAt() == null || !booking.getExpiredAt().isAfter(LocalDateTime.now())) {
            booking.cancel();
            bookingRepository.save(booking);
            throw new RuntimeException("Booking đã hết thời gian giữ phòng. Vui lòng đặt lại phòng mới");
        }

        List<Payment> oldPayments = paymentRepository.findByBookingBookingIDOrderByPaymentIDDesc(bookingId);
        for (Payment oldPayment : oldPayments) {
            if ("pending".equalsIgnoreCase(oldPayment.getStatus())) {
                oldPayment.setStatus("failed");
            }
        }
        paymentRepository.saveAll(oldPayments);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .method("vnpay")
                .status("pending")
                .transactionCode("BOOKING_" + bookingId + "_" + System.currentTimeMillis())
                .build();

        paymentRepository.save(payment);

        String paymentUrl = vnPayUtil.createPaymentUrl(
                booking.getBookingID(),
                (long) booking.getTotalPrice(),
                ipAddress
        );

        return new PaymentUrlResponse(paymentUrl);
    }

    @Override
    @Transactional
    public Map<String, String> handleVnPayReturn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();

        try {
            boolean valid = vnPayUtil.validateReturn(params);
            if (!valid) {
                result.put("status", "failed");
                result.put("message", "Chữ ký VNPay không hợp lệ");
                result.put("earnedPoint", "0");
                result.put("bookingStatus", BookingStatus.PENDING);
                return result;
            }

            // TxnRef có dạng "bookingId_timestamp" → tách lấy bookingId
            String txnRef = params.get("vnp_TxnRef");
            int bookingId = Integer.parseInt(txnRef.contains("_") ? txnRef.split("_")[0] : txnRef);
            String responseCode = params.get("vnp_ResponseCode");
            String transactionNo = params.getOrDefault("vnp_TransactionNo", "");
            String amount = params.getOrDefault("vnp_Amount", "0");

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

            syncBookingState(booking);

            Payment payment = paymentRepository.findTopByBookingBookingIDOrderByPaymentIDDesc(bookingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy payment"));

            result.put("bookingId", String.valueOf(booking.getBookingID()));
            result.put("amount", String.valueOf(booking.getTotalPrice()));
            result.put("transactionCode", transactionNo);
            result.put("earnedPoint", "0");

            if (!booking.canBePaid()) {
                payment.setStatus("failed");
                paymentRepository.save(payment);

                result.put("status", "failed");
                result.put("message", "Booking đã hết hạn hoặc không còn cho phép thanh toán");
                result.put("bookingStatus", booking.getStatus());
                return result;
            }

            if ("00".equals(responseCode)) {
                payment.setStatus("success");
                payment.setPaymentDate(LocalDateTime.now());
                payment.setTransactionCode(transactionNo);

                booking.confirmPayment();

                Customer customer = booking.getCustomer();
                int earnedPoint = (int) Math.floor(booking.getTotalPrice() / 100000);
                customer.setPoint(customer.getPoint() + earnedPoint);

                membershipTierRepository.findTopByMinPointLessThanEqualOrderByMinPointDesc(customer.getPoint())
                        .ifPresent(customer::setMembershipTier);

                paymentRepository.save(payment);
                bookingRepository.save(booking);
                customerRepository.save(customer);

                try {
                    emailService.sendBookingConfirmationEmail(customer, booking, payment);
                } catch (Exception ex) {
                    System.err.println("Lỗi gửi email xác nhận đặt phòng: " + ex.getMessage());
                    // Không ném lỗi ra ngoài để luồng thanh toán vẫn thành công
                }

                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
                result.put("transactionCode", transactionNo);
                result.put("earnedPoint", String.valueOf(earnedPoint));
                result.put("bookingStatus", booking.getStatus());
                return result;
            }

            payment.setStatus("failed");
            paymentRepository.save(payment);

            if (booking.getExpiredAt() != null && !booking.getExpiredAt().isAfter(LocalDateTime.now())) {
                booking.cancel();
                bookingRepository.save(booking);

                result.put("status", "failed");
                result.put("message", "Thanh toán thất bại và booking đã hết hạn giữ phòng");
                result.put("bookingStatus", booking.getStatus());
                return result;
            }

            booking.setStatus(BookingStatus.PENDING);
            bookingRepository.save(booking);

            result.put("status", "failed");
            result.put("message", "Thanh toán thất bại hoặc bị hủy. Bạn vẫn có thể thanh toán lại nếu booking còn hạn giữ phòng");
            result.put("bookingStatus", booking.getStatus());
            return result;

        } catch (Exception e) {
            result.put("status", "failed");
            result.put("message", "Có lỗi khi xử lý thanh toán: " + e.getMessage());
            result.put("earnedPoint", "0");
            result.put("bookingStatus", BookingStatus.PENDING);
            return result;
        }
    }

    private void syncBookingState(Booking booking) {
        if (booking == null) return;

        if (booking.isPending()
                && booking.getExpiredAt() != null
                && !booking.getExpiredAt().isAfter(LocalDateTime.now())) {

            booking.cancel();
            bookingRepository.save(booking);

            List<Payment> payments = paymentRepository.findByBookingBookingIDOrderByPaymentIDDesc(booking.getBookingID());
            for (Payment item : payments) {
                if ("pending".equalsIgnoreCase(item.getStatus())) {
                    item.setStatus("failed");
                }
            }
            paymentRepository.saveAll(payments);
        }
    }
}