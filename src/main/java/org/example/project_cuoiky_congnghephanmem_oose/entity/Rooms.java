package org.example.project_cuoiky_congnghephanmem_oose.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "Rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rooms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roomID;
    private String roomNumber;
    private String status;
    private String description;
    private String statusNote; // Lý do bảo trì / ngừng kinh doanh

    @Column(columnDefinition = "double default 0")
    private double priceExtra;

    // Khoảng ngày bảo trì / ngừng hoạt động (null = áp dụng vô thời hạn cho tới khi admin gỡ)
    private LocalDate maintenanceStart;
    private LocalDate maintenanceEnd;

    @ManyToOne
    @JoinColumn(name = "typeID")
    private RoomTypes roomType;

    private boolean isOutOfServiceType() {
        return "maintenance".equalsIgnoreCase(status) || "inactive".equalsIgnoreCase(status);
    }

    // Phòng có ngừng phục vụ vào ĐÚNG ngày D không (dùng cho màn hình admin xem theo ngày)
    public boolean isOutOfServiceOn(LocalDate date) {
        if (!isOutOfServiceType()) return false;
        if (maintenanceStart == null || maintenanceEnd == null) return true; // vô thời hạn
        return !date.isBefore(maintenanceStart) && !date.isAfter(maintenanceEnd);
    }

    // Phòng có vướng bảo trì trong khoảng lưu trú [checkin, checkout) không (chặn đặt phòng)
    public boolean isOutOfServiceForStay(LocalDate checkin, LocalDate checkout) {
        if (!isOutOfServiceType()) return false;
        if (maintenanceStart == null || maintenanceEnd == null) return true; // vô thời hạn
        // Trùng khoảng: bảo trì bắt đầu trước ngày trả phòng VÀ kết thúc từ ngày nhận phòng trở đi
        return maintenanceStart.isBefore(checkout) && !maintenanceEnd.isBefore(checkin);
    }

    public boolean isAvailable(Date checkin, Date checkout) {
        return false;
    }

    public void updateStatus(String status) {
    }

    public void updateInfo() {
    }

    // Giá thực tế của phòng = giá loại phòng + phụ phí theo view/vị trí cụ thể
    public double getEffectivePrice() {
        double base = roomType != null ? roomType.getPriceRoom() : 0;
        return base + priceExtra;
    }
}