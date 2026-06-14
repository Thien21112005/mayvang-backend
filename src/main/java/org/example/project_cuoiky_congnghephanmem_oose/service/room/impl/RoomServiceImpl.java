package org.example.project_cuoiky_congnghephanmem_oose.service.room.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.RoomSearchRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.RoomResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.RoomSearchResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.RoomTypeResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Rooms;
import org.example.project_cuoiky_congnghephanmem_oose.exception.AppException;
import org.example.project_cuoiky_congnghephanmem_oose.exception.ErrorCode;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IRoomRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.room.IRoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements IRoomService {

    private final IRoomRepository roomRepository;

    public RoomServiceImpl(IRoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public RoomSearchResponse searchAvailableRooms(RoomSearchRequest request) {
        validateSearchRequest(request);

        String roomType = normalizeRoomType(request.getRoomType());

        // ✅ Tính số người tối thiểu mỗi phòng cần chứa
        // ceil(guests / numberOfRooms) — ví dụ: 5 người / 2 phòng = ceil(2.5) = 3
        int guestsPerRoom = (int) Math.ceil(
                (double) request.getGuests() / request.getNumberOfRooms()
        );

        List<Rooms> allAvailable = roomRepository.findAvailableRooms(
                request.getCheckin(),
                request.getCheckout(),
                roomType,
                guestsPerRoom
        );

        int totalFound = allAvailable.size();
        int requested  = request.getNumberOfRooms();
        boolean enough = totalFound >= requested;

        // Trả về TẤT CẢ các phòng khả dụng để người dùng tự do lựa chọn
        List<RoomResponse> result = allAvailable.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        String warning = enough ? null :
                String.format(
                    "Chỉ còn %d phòng trống phù hợp (bạn yêu cầu %d phòng)",
                    totalFound, requested
                );

        return RoomSearchResponse.builder()
                .totalFound(totalFound)
                .requested(requested)
                .enough(enough)
                .warning(warning)
                .rooms(result)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateSearchRequest(RoomSearchRequest request) {
        LocalDate today = LocalDate.now();

        if (request.getCheckin().isBefore(today)) {
            throw new AppException(ErrorCode.INVALID_CHECKIN_DATE);
        }
        if (!request.getCheckout().isAfter(request.getCheckin())) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        if (request.getGuests() < 1 || request.getGuests() > 10) {
            throw new AppException(ErrorCode.INVALID_OCCUPANCY);
        }
        if (request.getNumberOfRooms() < 1 || request.getNumberOfRooms() > 10) {
            throw new AppException(ErrorCode.INVALID_ROOM_COUNT);
        }
    }

    private String normalizeRoomType(String roomType) {
        if (roomType == null || roomType.isBlank() || roomType.equalsIgnoreCase("all")) {
            return null;
        }
        return roomType.trim();
    }

    private RoomResponse toResponse(Rooms room) {
        RoomTypeResponse typeResponse = RoomTypeResponse.builder()
                .typeID(room.getRoomType().getTypeID())
                .typeName(room.getRoomType().getTypeName())
                .priceRoom(room.getEffectivePrice())
                .occupancy(room.getRoomType().getOccupancy())
                .build();

        return RoomResponse.builder()
                .roomID(room.getRoomID())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus())
                .description(room.getDescription())
                .roomType(typeResponse)
                .build();
    }
}