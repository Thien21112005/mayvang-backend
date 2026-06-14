package org.example.project_cuoiky_congnghephanmem_oose.controller;

import org.example.project_cuoiky_congnghephanmem_oose.dto.response.RoomTypeResponse;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IRoomTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final IRoomTypeRepository roomTypeRepository;

    public RoomTypeController(IRoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    // GET /api/room-types — danh sách tất cả loại phòng (cho dropdown admin, tìm phòng...)
    @GetMapping
    public ResponseEntity<List<RoomTypeResponse>> getAllRoomTypes() {
        List<RoomTypeResponse> types = roomTypeRepository.findAll().stream()
                .map(t -> RoomTypeResponse.builder()
                        .typeID(t.getTypeID())
                        .typeName(t.getTypeName())
                        .priceRoom(t.getPriceRoom())
                        .occupancy(t.getOccupancy())
                        .build())
                .toList();
        return ResponseEntity.ok(types);
    }
}
