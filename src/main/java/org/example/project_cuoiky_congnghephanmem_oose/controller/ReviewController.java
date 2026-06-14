package org.example.project_cuoiky_congnghephanmem_oose.controller;

import org.example.project_cuoiky_congnghephanmem_oose.dto.response.ReviewResponseDTO;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Review;
import org.example.project_cuoiky_congnghephanmem_oose.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/public")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews(Authentication authentication) {
        return ResponseEntity.ok(reviewService.getMyReviews(authentication.getName()));
    }

    @PostMapping("/")
    public ResponseEntity<?> createReview(@RequestBody Review review, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để đánh giá"));
        }
        Review savedReview = reviewService.createReview(review, authentication.getName());
        return ResponseEntity.ok(savedReview);
    }

    @PostMapping("/me/images")
    public ResponseEntity<?> uploadReviewImages(
            @RequestPart("files") MultipartFile[] files,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        List<String> urls = reviewService.uploadReviewImages(files, authentication.getName());
        return ResponseEntity.ok(urls);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable int id, @RequestBody Review review, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        Review existing = reviewService.getReviewById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (existing.getCustomer() == null || !existing.getCustomer().getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền sửa đánh giá này"));
        }

        Review updated = reviewService.updateReview(existing, review);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable int id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }

        Review existing = reviewService.getReviewById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        if (existing.getCustomer() == null || !existing.getCustomer().getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền xóa đánh giá này"));
        }

        reviewService.deleteReview(existing);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá"));
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<?> adminReply(@PathVariable int id, @RequestBody Map<String, String> payload) {
        String reply = payload.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nội dung phản hồi không được trống"));
        }
        Review updatedReview = reviewService.addAdminReply(id, reply);
        if (updatedReview != null) {
            return ResponseEntity.ok(Map.of("message", "Đã phản hồi thành công"));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/reply")
    public ResponseEntity<?> deleteAdminReply(@PathVariable int id) {
        Review review = reviewService.getReviewById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        reviewService.removeAdminReply(review);
        return ResponseEntity.ok(Map.of("message", "Đã xóa phản hồi"));
    }
}
