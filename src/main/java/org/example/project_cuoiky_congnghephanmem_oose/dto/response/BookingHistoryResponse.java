package org.example.project_cuoiky_congnghephanmem_oose.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingHistoryResponse {
    private int bookingID;
    private LocalDateTime bookingDate;
    private String status;
    private String paymentStatus;
    private double totalPrice;
    private LocalDateTime expiredAt;
    private boolean canRepay;
    private boolean expired;
    private double discountAmount;
    private List<BookingRoomItemResponse> rooms;
    private LocalDate checkin;
    private LocalDate checkout;
}