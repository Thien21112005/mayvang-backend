package org.example.project_cuoiky_congnghephanmem_oose.controller;

import org.example.project_cuoiky_congnghephanmem_oose.scheduler.BookingExpirationScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    private final BookingExpirationScheduler bookingExpirationScheduler;

    public TestController(BookingExpirationScheduler bookingExpirationScheduler) {
        this.bookingExpirationScheduler = bookingExpirationScheduler;
    }

    @GetMapping("/")
    public String hello() {
        return "Hotel Booking API đang chạy!";
    }

    @GetMapping("/api/scheduler/stats")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getSchedulerStats() {
        return ResponseEntity.ok(bookingExpirationScheduler.getSchedulerStats());
    }
}