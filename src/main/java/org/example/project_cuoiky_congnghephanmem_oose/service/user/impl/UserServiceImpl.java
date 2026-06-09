package org.example.project_cuoiky_congnghephanmem_oose.service.user.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.UpdateProfileRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.BookingHistoryResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.BookingRoomItemResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.UserProfileResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.BookingDetails;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.MembershipTier;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Payment;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ICustomerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IMembershipTierRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IPaymentRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.user.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.ChangePasswordRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImpl implements IUserService {

    private final ICustomerRepository customerRepository;
    private final IBookingRepository bookingRepository;
    private final IMembershipTierRepository membershipTierRepository;
    private final IPaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(ICustomerRepository customerRepository,
                           IBookingRepository bookingRepository,
                           IMembershipTierRepository membershipTierRepository,
                           IPaymentRepository paymentRepository,
                           PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
        this.membershipTierRepository = membershipTierRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserProfileResponse getMyProfile(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        refreshTier(customer);

        MembershipTier tier = customer.getMembershipTier();
        return UserProfileResponse.builder()
                .userID(customer.getUserID())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(customer.getDateOfBirth())
                .avatar(customer.getAvatar())
                .point(customer.getPoint())
                .membershipTier(tier != null ? tier.getTierName() : "Bronze")
                .discountRate(tier != null ? tier.getDiscountRate() : 0.0)
                .benefits(tier != null ? tier.getBenefits() : "Giá gốc, hỗ trợ cơ bản")
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String username, UpdateProfileRequest request) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(customer.getUsername())) {
                if (customerRepository.findByUsername(newUsername).isPresent()) {
                    throw new RuntimeException("Tên đăng nhập đã tồn tại");
                }
                customer.setUsername(newUsername);
            }
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(customer.getEmail())) {
                if (customerRepository.findByEmail(newEmail).isPresent()) {
                    throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
                }
                customer.setEmail(newEmail);
            }
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(customer.getPhone())) {
                if (customerRepository.findByPhone(newPhone).isPresent()) {
                    throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác");
                }
                customer.setPhone(newPhone);
            }
        }
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAvatar() != null && !request.getAvatar().isBlank()) {
            customer.setAvatar(request.getAvatar().trim());
        }

        customerRepository.save(customer);
        return getMyProfile(customer.getUsername());
    }

    @Override
    @Transactional
    public List<BookingHistoryResponse> getMyBookingHistory(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        List<Booking> bookings = bookingRepository.findByCustomerUserIDOrderByBookingDateDesc(customer.getUserID());

        for (Booking booking : bookings) {
            syncExpiredBooking(booking);
        }

        return bookings.stream()
                .map(this::toBookingHistory)
                .toList();
    }

    private BookingHistoryResponse toBookingHistory(Booking booking) {
        List<BookingRoomItemResponse> rooms = booking.getBookingDetails()
                .stream()
                .map(this::toBookingRoomItem)
                .toList();

        BookingDetails first = booking.getBookingDetails()
                .stream()
                .min(Comparator.comparing(BookingDetails::getCheckinDate))
                .orElse(null);

        Payment latestPayment = paymentRepository.findTopByBookingBookingIDOrderByPaymentIDDesc(booking.getBookingID())
                .orElse(null);

        boolean expired = booking.getExpiredAt() != null && !booking.getExpiredAt().isAfter(LocalDateTime.now());
        boolean canRepay = "pending".equalsIgnoreCase(booking.getStatus()) && !expired;

        double originalTotal = 0;
        for (BookingDetails detail : booking.getBookingDetails()) {
            long detailNights = java.time.temporal.ChronoUnit.DAYS.between(detail.getCheckinDate(), detail.getCheckoutDate());
            if (detailNights <= 0) detailNights = 1;
            originalTotal += detail.getRoom().getRoomType().getPriceRoom() * detailNights;
        }
        double discountAmount = originalTotal - booking.getTotalPrice();
        if (discountAmount < 0) discountAmount = 0;

        return BookingHistoryResponse.builder()
                .bookingID(booking.getBookingID())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .paymentStatus(latestPayment != null ? latestPayment.getStatus() : "pending")
                .totalPrice(booking.getTotalPrice())
                .expiredAt(booking.getExpiredAt())
                .canRepay(canRepay)
                .expired(expired)
                .discountAmount(discountAmount)
                .rooms(rooms)
                .checkin(first != null ? first.getCheckinDate() : null)
                .checkout(first != null ? first.getCheckoutDate() : null)
                .build();
    }

    private BookingRoomItemResponse toBookingRoomItem(BookingDetails detail) {
        return BookingRoomItemResponse.builder()
                .roomID(detail.getRoom().getRoomID())
                .roomNumber(detail.getRoom().getRoomNumber())
                .roomType(detail.getRoom().getRoomType().getTypeName())
                .pricePerNight(detail.getRoom().getRoomType().getPriceRoom())
                .subTotal(detail.getSubTotal())
                .build();
    }

    private void refreshTier(Customer customer) {
        membershipTierRepository.findTopByMinPointLessThanEqualOrderByMinPointDesc(customer.getPoint())
                .ifPresent(customer::setMembershipTier);
        customerRepository.save(customer);
    }

    private void syncExpiredBooking(Booking booking) {
        if (booking == null) return;

        if ("pending".equalsIgnoreCase(booking.getStatus())
                && booking.getExpiredAt() != null
                && !booking.getExpiredAt().isAfter(LocalDateTime.now())) {

            booking.setStatus("cancelled");
            bookingRepository.save(booking);

            paymentRepository.findByBookingBookingIDOrderByPaymentIDDesc(booking.getBookingID())
                    .forEach(payment -> {
                        if ("pending".equalsIgnoreCase(payment.getStatus())) {
                            payment.setStatus("failed");
                            paymentRepository.save(payment);
                        }
                    });
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), customer.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void cancelBooking(String username, Integer bookingId) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng"));

        if (booking.getCustomer().getUserID() != customer.getUserID()) {
            throw new RuntimeException("Không có quyền hủy đơn đặt phòng này");
        }

        if (!"pending".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn đặt phòng đang chờ thanh toán");
        }

        booking.setStatus("cancelled");
        bookingRepository.save(booking);

        paymentRepository.findByBookingBookingIDOrderByPaymentIDDesc(booking.getBookingID())
                .forEach(payment -> {
                    if ("pending".equalsIgnoreCase(payment.getStatus())) {
                        payment.setStatus("cancelled");
                        paymentRepository.save(payment);
                    }
                });
    }
}