// service/manager/impl/PotentialCustomerServiceImpl.java
package org.example.project_cuoiky_congnghephanmem_oose.service.manager.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.response.PotentialCustomerResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.manager.IPotentialCustomerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PotentialCustomerServiceImpl implements IPotentialCustomerService {

    private final IBookingRepository bookingRepository;

    public PotentialCustomerServiceImpl(IBookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<PotentialCustomerResponse> getPotentialCustomers() {
        List<Object[]> results = bookingRepository.findPotentialCustomers();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return results.stream().map(row -> {
            Customer customer = (Customer) row[0];
            long totalBookings = (long) row[1];
            double totalSpent = (double) row[2];
            LocalDateTime lastBooking = (LocalDateTime) row[3];

            long daysSinceLastBooking = ChronoUnit.DAYS.between(lastBooking, now);

            // ── RFM Scoring ──────────────────────────────────────────────
            // Recency: Lần đặt gần đây nhất
            //   ≤ 30 ngày  → 3đ (vẫn đang hoạt động)
            //   ≤ 90 ngày  → 2đ (gần đây)
            //   > 90 ngày  → 1đ (lâu rồi)
            int recencyScore;
            if (daysSinceLastBooking <= 30) recencyScore = 3;
            else if (daysSinceLastBooking <= 90) recencyScore = 2;
            else recencyScore = 1;

            // Frequency: Số lần đặt phòng thành công
            //   ≥ 5 lần → 3đ (thường xuyên)
            //   ≥ 2 lần → 2đ (vài lần)
            //   1 lần   → 1đ (mới thử)
            int frequencyScore;
            if (totalBookings >= 5) frequencyScore = 3;
            else if (totalBookings >= 2) frequencyScore = 2;
            else frequencyScore = 1;

            // Monetary: Tổng chi tiêu
            // Dựa theo hệ thống tier: 80tr = Silver, 250tr = Gold
            //   ≥ 80,000,000 VND  → 3đ (khách VIP, tầm Silver+)
            //   ≥ 20,000,000 VND  → 2đ (chi tiêu khá, ~3-4 lần phòng cao cấp)
            //   < 20,000,000 VND  → 1đ (chi ít)
            int monetaryScore;
            if (totalSpent >= 80_000_000) monetaryScore = 3;
            else if (totalSpent >= 20_000_000) monetaryScore = 2;
            else monetaryScore = 1;

            int rfmScore = recencyScore + frequencyScore + monetaryScore;

            // Phân loại khách dựa trên tổng điểm RFM
            String rfmLabel;
            if (rfmScore >= 7) rfmLabel = "VIP";
            else if (rfmScore >= 5) rfmLabel = "Tiềm năng cao";
            else if (rfmScore >= 3) rfmLabel = "Cần kích hoạt";
            else rfmLabel = "Ngủ đông";

            return PotentialCustomerResponse.builder()
                    .userID(customer.getUserID())
                    .username(customer.getUsername())
                    .email(customer.getEmail())
                    .phone(customer.getPhone())
                    .totalBookings(totalBookings)
                    .totalSpent(totalSpent)
                    .recencyScore(recencyScore)
                    .frequencyScore(frequencyScore)
                    .monetaryScore(monetaryScore)
                    .rfmScore(rfmScore)
                    .rfmLabel(rfmLabel)
                    .lastBookingDate(lastBooking.format(fmt))
                    .build();
        })
        // Xếp theo điểm RFM giảm dần, nếu bằng nhau thì theo tổng chi tiêu
        .sorted(Comparator.comparingInt(PotentialCustomerResponse::getRfmScore).reversed()
                .thenComparing(Comparator.comparingDouble(PotentialCustomerResponse::getTotalSpent).reversed()))
        .collect(Collectors.toList());
    }
}