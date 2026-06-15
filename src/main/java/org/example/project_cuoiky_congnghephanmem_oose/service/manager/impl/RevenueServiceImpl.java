package org.example.project_cuoiky_congnghephanmem_oose.service.manager.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.response.RevenueResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.state.BookingStatus;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.manager.IRevenueService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RevenueServiceImpl implements IRevenueService {

    private final IBookingRepository bookingRepository;

    // Booking đã thanh toán: kể cả đã nhận phòng / đã trả phòng vẫn tính doanh thu
    private static final List<String> PAID_STATUSES = BookingStatus.PAID;

    public RevenueServiceImpl(IBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public RevenueResponse getRevenue(String period) {
        LocalDateTime from = getFromDate(period);
        LocalDateTime to = LocalDateTime.now();

        // 1. Lấy booking đã thanh toán (confirmed/checked_in/checked_out)
        List<Booking> bookings = (from == null)
                ? bookingRepository.findByStatusIn(PAID_STATUSES)
                : bookingRepository.findByBookingDateBetweenAndStatusIn(from, to, PAID_STATUSES);

        // 2. Tính các chỉ số
        double totalRevenue = bookings.stream().mapToDouble(Booking::getTotalPrice).sum();
        long totalBookings = bookings.size();
        long bookedRoomsCount = bookings.stream()
                .mapToLong(b -> b.getBookingDetails() != null ? b.getBookingDetails().size() : 0)
                .sum();
        long days = getDays(period);
        double avgDailyRevenue = days > 0 ? totalRevenue / days : totalRevenue;

        // 3. KHỞI TẠO TRỤC THỜI GIAN
        Map<String, Double> grouped = new LinkedHashMap<>();
        DateTimeFormatter formatter = getFormatter(period);
        LocalDate today = LocalDate.now();

        switch (period) {
            case "week":
                // Lấy ra ngày Thứ 2 của tuần hiện tại
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                // Khởi tạo từ Thứ 2 đến Chủ Nhật (7 ngày)
                for (int i = 0; i < 7; i++) {
                    grouped.put(startOfWeek.plusDays(i).format(formatter), 0.0);
                }
                break;
            case "month":
                int daysInMonth = today.lengthOfMonth();
                for (int i = 1; i <= daysInMonth; i++) {
                    grouped.put(today.withDayOfMonth(i).format(formatter), 0.0);
                }
                break;
            case "year":
                for (int i = 1; i <= 12; i++) {
                    grouped.put(today.withMonth(i).format(formatter), 0.0);
                }
                break;
            default:
                grouped = new TreeMap<>();
                break;
        }

        // 4. ĐỔ DỮ LIỆU
        for (Booking b : bookings) {
            String dateKey = b.getBookingDate().format(formatter);
            if (grouped.containsKey(dateKey)) {
                grouped.put(dateKey, grouped.get(dateKey) + b.getTotalPrice());
            } else if ("all".equals(period)) {
                grouped.merge(dateKey, b.getTotalPrice(), Double::sum);
            }
        }

        // 5. Build Response
        List<RevenueResponse.RevenueByDate> chartData = grouped.entrySet().stream()
                .map(e -> new RevenueResponse.RevenueByDate(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new RevenueResponse(totalRevenue, totalBookings, bookedRoomsCount, avgDailyRevenue, chartData);
    }

    private LocalDateTime getFromDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "week"  -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .withHour(0).withMinute(0).withSecond(0).withNano(0); // Bắt đầu từ 00:00 Thứ 2
            case "month" -> now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "year"  -> now.withDayOfYear(1).toLocalDate().atStartOfDay();
            default      -> null;
        };
    }

    private long getDays(String period) {
        return switch (period) {
            case "week"  -> 7;
            case "month" -> LocalDate.now().lengthOfMonth();
            case "year"  -> LocalDate.now().lengthOfYear();
            default      -> 1;
        };
    }

    private DateTimeFormatter getFormatter(String period) {
        return switch (period) {
            case "year" -> DateTimeFormatter.ofPattern("MM/yyyy");
            default     -> DateTimeFormatter.ofPattern("dd/MM");
        };
    }
}