package org.example.project_cuoiky_congnghephanmem_oose.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateRoomRequest {
    private int typeID;
    private String status;

    // Khoảng ngày bảo trì / ngừng hoạt động (chỉ dùng khi status = maintenance/inactive)
    private LocalDate maintenanceStart;
    private LocalDate maintenanceEnd;
}
