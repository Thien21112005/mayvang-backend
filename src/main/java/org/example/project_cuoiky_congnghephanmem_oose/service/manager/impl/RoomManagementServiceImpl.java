// service/manager/impl/RoomManagementServiceImpl.java
package org.example.project_cuoiky_congnghephanmem_oose.service.manager.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.UpdateRoomRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.RoomDetailResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.RoomTypes;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Rooms;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IRoomRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IRoomTypeRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.manager.IRoomManagementService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomManagementServiceImpl implements IRoomManagementService {

    private final IRoomRepository roomRepository;
    private final IRoomTypeRepository roomTypeRepository;
    private final IBookingRepository bookingRepository;

    public RoomManagementServiceImpl(IRoomRepository roomRepository,
                                     IRoomTypeRepository roomTypeRepository,
                                     IBookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.bookingRepository = bookingRepository;
    }

    // GHI ĐÈ HÀM CŨ CỦA BẠN: Mặc định gọi logic ngày hôm nay
    @Override
    public List<RoomDetailResponse> getAllRooms() {
        return getRoomsByDate(null);
    }

    // GHI ĐÈ HÀM MỚI: Xử lý logic lọc phòng theo ngày được truyền vào
    @Override
    public List<RoomDetailResponse> getRoomsByDate(String dateParam) {
        LocalDate targetDate;
        if (dateParam == null || dateParam.isBlank()) {
            targetDate = LocalDate.now();
        } else {
            try {
                targetDate = LocalDate.parse(dateParam, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                targetDate = LocalDate.now();
            }
        }

        List<Integer> bookedRoomIdsForTargetDate = bookingRepository.findBookedRoomIdsByDate(targetDate);
        final LocalDate viewDate = targetDate; // cần biến final để dùng trong lambda

        return roomRepository.findAll().stream()
                .map(room -> {
                    String currentStatus;

                    // Phòng bảo trì/ngừng hoạt động ĐÚNG ngày đang xem -> hiện trạng thái đó;
                    // ngoài khoảng đó thì tính bình thường (booked/available theo ngày).
                    if (room.isOutOfServiceOn(viewDate)) {
                        currentStatus = room.getStatus();
                    } else if (bookedRoomIdsForTargetDate.contains(room.getRoomID())) {
                        currentStatus = "booked";
                    } else {
                        currentStatus = "available";
                    }

                    return toResponseDynamic(room, currentStatus);
                })
                .collect(Collectors.toList());
    }

    @Override
    public RoomDetailResponse updateRoom(int roomID, UpdateRoomRequest request) {
        Rooms room = roomRepository.findById(roomID)
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại!"));

        if (request.getTypeID() != 0) {
            RoomTypes type = roomTypeRepository.findById(request.getTypeID())
                    .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại!"));
            room.setRoomType(type);
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String newStatus = request.getStatus();
            if (newStatus.equalsIgnoreCase("booked")) {
                throw new RuntimeException("Không được phép gán trạng thái 'Đã đặt' thủ công. Vui lòng tạo Booking.");
            }
            room.setStatus(newStatus);

            if (newStatus.equalsIgnoreCase("maintenance") || newStatus.equalsIgnoreCase("inactive")) {
                // Lưu khoảng ngày bảo trì/ngừng hoạt động (có thể null = vô thời hạn tới khi admin gỡ)
                room.setMaintenanceStart(request.getMaintenanceStart());
                room.setMaintenanceEnd(request.getMaintenanceEnd());
                room.setStatusNote(request.getStatusNote());
            } else {
                // Quay lại hoạt động bình thường -> xóa lịch bảo trì
                room.setMaintenanceStart(null);
                room.setMaintenanceEnd(null);
                room.setStatusNote(null);
            }
        }

        roomRepository.save(room);
        return toResponseDynamic(room, room.getStatus());
    }

    private RoomDetailResponse toResponseDynamic(Rooms room, String dynamicStatus) {
        RoomTypes type = room.getRoomType();
        return new RoomDetailResponse(
                room.getRoomID(),
                room.getRoomNumber(),
                dynamicStatus,
                room.getDescription(),
                room.getStatusNote(),
                type != null ? type.getTypeID() : 0,
                type != null ? type.getTypeName() : "N/A",
                room.getEffectivePrice(),
                type != null ? type.getOccupancy() : 0,
                room.getMaintenanceStart(),
                room.getMaintenanceEnd()
        );
    }
}