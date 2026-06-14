package org.example.project_cuoiky_congnghephanmem_oose.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RoomDetailResponse {
    private int roomID;
    private String roomNumber;
    private String status;
    private String description;
    private String statusNote;
    private int typeID;
    private String typeName;
    private double priceRoom;
    private int occupancy;

    // Khoảng ngày bảo trì / ngừng hoạt động (để admin xem & sửa lại)
    private LocalDate maintenanceStart;
    private LocalDate maintenanceEnd;
}
