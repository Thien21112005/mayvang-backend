package org.example.project_cuoiky_congnghephanmem_oose.scheduler;

import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Payment;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Rooms;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IPaymentRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IRoomRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IEmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BookingExpirationScheduler {

    private final IBookingRepository bookingRepository;
    private final IPaymentRepository paymentRepository;
    private final IEmailService emailService;
    private final IRoomRepository roomRepository;

    // Stats tracking
    private LocalDateTime lastExpireJobRun;
    private int lastExpireJobCount = 0;
    private LocalDateTime lastStatusJobRun;
    private int lastStatusJobCheckIn = 0;
    private int lastStatusJobCheckOut = 0;

    public BookingExpirationScheduler(IBookingRepository bookingRepository,
                                      IPaymentRepository paymentRepository,
                                      IEmailService emailService,
                                      IRoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
        this.roomRepository = roomRepository;
    }

    // Tự kích hoạt lại phòng khi đã qua ngày kết thúc bảo trì/ngừng hoạt động
    @Scheduled(fixedDelay = 3600000) // mỗi giờ
    @Transactional
    public void autoReactivateMaintenanceRooms() {
        LocalDate today = LocalDate.now();
        List<Rooms> expired = roomRepository.findExpiredMaintenanceRooms(today);
        if (expired.isEmpty()) return;

        for (Rooms room : expired) {
            room.setStatus("available");
            room.setMaintenanceStart(null);
            room.setMaintenanceEnd(null);
            room.setStatusNote(null);
        }
        roomRepository.saveAll(expired);
        System.out.println("[ROOM_MAINTENANCE_JOB] Da kich hoat lai " + expired.size() + " phong het han bao tri luc " + today);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[BOOKING_EXPIRE_JOB] Kiểm tra booking hết hạn vào " + now);

        List<Booking> expiredBookings = bookingRepository
                .findByStatusIgnoreCaseAndExpiredAtBefore("pending", now);

        if (expiredBookings.isEmpty()) {
            System.out.println("[BOOKING_EXPIRE_JOB] Không có booking nào hết hạn");
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.setStatus("cancelled");

            List<Payment> payments = paymentRepository
                    .findByBookingBookingIDOrderByPaymentIDDesc(booking.getBookingID());

            for (Payment payment : payments) {
                if ("pending".equalsIgnoreCase(payment.getStatus())) {
                    payment.setStatus("failed");
                }
            }

            paymentRepository.saveAll(payments);
        }

        bookingRepository.saveAll(expiredBookings);

        // Track stats
        this.lastExpireJobRun = now;
        this.lastExpireJobCount = expiredBookings.size();

        System.out.println("[BOOKING_EXPIRE_JOB] Da huy " + expiredBookings.size() + " booking het han luc " + now);
    }

    @Scheduled(fixedDelay = 60000) // Chạy mỗi phút (hoặc cấu hình tùy ý)
    @Transactional
    public void autoUpdateBookingStatus() {
        LocalDate today = LocalDate.now();
        System.out.println("[BOOKING_STATUS_JOB] Kiểm tra cập nhật trạng thái booking vào " + today);
        int checkInCount = 0;
        int checkOutCount = 0;
        List<Booking> newlyCheckedOut = new ArrayList<>();

        // 1. Chuyển "confirmed" -> "checked_in" hoặc "checked_out"
        List<Booking> confirmedBookings = bookingRepository.findByStatus("confirmed");
        for (Booking booking : confirmedBookings) {
            if (booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()) {
                // Lấy ngày check-in/out của phòng đầu tiên làm đại diện
                LocalDate checkin = booking.getBookingDetails().get(0).getCheckinDate();
                LocalDate checkout = booking.getBookingDetails().get(0).getCheckoutDate();
                
                if (today.isAfter(checkout)) {
                     booking.setStatus("checked_out");
                     checkOutCount++;
                     newlyCheckedOut.add(booking);
                } else if (!today.isBefore(checkin)) { // today >= checkin
                     booking.setStatus("checked_in");
                     checkInCount++;
                }
            }
        }
        if (checkInCount > 0 || checkOutCount > 0) {
            bookingRepository.saveAll(confirmedBookings);
        }

        // 2. Chuyển "checked_in" -> "checked_out" nếu đã qua ngày trả phòng
        List<Booking> checkedInBookings = bookingRepository.findByStatus("checked_in");
        int extraCheckOutCount = 0;
        for (Booking booking : checkedInBookings) {
            if (booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()) {
                LocalDate checkout = booking.getBookingDetails().get(0).getCheckoutDate();
                if (today.isAfter(checkout)) {
                    booking.setStatus("checked_out");
                    extraCheckOutCount++;
                    newlyCheckedOut.add(booking);
                }
            }
        }
        if (extraCheckOutCount > 0) {
            bookingRepository.saveAll(checkedInBookings);
            checkOutCount += extraCheckOutCount;
        }

        // Gửi mail cảm ơn cho các booking vừa được trả phòng (lỗi mail không làm hỏng job)
        for (Booking booking : newlyCheckedOut) {
            try {
                emailService.sendCheckoutThankYouEmail(booking.getCustomer(), booking);
            } catch (Exception e) {
                System.err.println("[BOOKING_STATUS_JOB] Gui mail cam on check-out that bai cho booking #"
                        + booking.getBookingID() + ": " + e.getMessage());
            }
        }

        if (checkInCount > 0 || checkOutCount > 0) {
            // Track stats
            this.lastStatusJobRun = LocalDateTime.now();
            this.lastStatusJobCheckIn = checkInCount;
            this.lastStatusJobCheckOut = checkOutCount;

            System.out.println("[BOOKING_STATUS_JOB] Tu dong Check-in: " + checkInCount + " | Check-out: " + checkOutCount + " vao " + LocalDateTime.now());
        }
    }

    // Public methods để lấy stats
    public Map<String, Object> getSchedulerStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("lastExpireJobRun", lastExpireJobRun);
        stats.put("lastExpireJobCount", lastExpireJobCount);
        stats.put("lastStatusJobRun", lastStatusJobRun);
        stats.put("lastStatusJobCheckIn", lastStatusJobCheckIn);
        stats.put("lastStatusJobCheckOut", lastStatusJobCheckOut);
        return stats;
    }
}