package org.example.project_cuoiky_congnghephanmem_oose.service;

import org.example.project_cuoiky_congnghephanmem_oose.dto.response.ReviewResponseDTO;
import org.example.project_cuoiky_congnghephanmem_oose.entity.BookingDetails;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Review;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ReviewRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ICustomerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private IBookingRepository bookingRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private IEmailService emailService;

    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(r -> mapToDTO(r, true)) // isPublic = true
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getMyReviews(String username) {
        return reviewRepository.findByCustomer_Username(username).stream()
                .map(r -> mapToDTO(r, false)) // isPublic = false
                .collect(Collectors.toList());
    }

    public Review getReviewById(int id) {
        return reviewRepository.findById(id).orElse(null);
    }

    public Review createReview(Review review, String username) {
        Customer customer = customerRepository.findByUsername(username).orElse(null);
        if (customer != null) {
            review.setCustomer(customer);
        }
        if (review.getBooking() != null && review.getBooking().getBookingID() > 0) {
            Booking booking = bookingRepository.findById(review.getBooking().getBookingID()).orElse(null);
            if (booking != null) {
                review.setBooking(booking);
            }
        }
        review.setReviewDate(java.time.LocalDateTime.now());
        Review saved = reviewRepository.save(review);

        // Gửi mail cảm ơn khách đã đánh giá (không để lỗi mail làm hỏng việc lưu)
        try {
            emailService.sendReviewSubmittedEmail(customer, saved);
        } catch (Exception e) {
            System.err.println("Không gửi được mail cảm ơn đánh giá: " + e.getMessage());
        }

        return saved;
    }

    public Review updateReview(Review existing, Review updatedData) {
        existing.setRating(updatedData.getRating());
        existing.setComment(updatedData.getComment());
        existing.setCleanlinessRating(updatedData.getCleanlinessRating());
        existing.setServiceRating(updatedData.getServiceRating());
        existing.setFacilitiesRating(updatedData.getFacilitiesRating());
        existing.setLocationRating(updatedData.getLocationRating());
        existing.setAnonymous(updatedData.isAnonymous());
        if (updatedData.getImageUrls() != null) {
            existing.setImageUrls(updatedData.getImageUrls());
        }
        return reviewRepository.save(existing);
    }

    public void deleteReview(Review review) {
        reviewRepository.delete(review);
    }

    public Review addAdminReply(int id, String reply) {
        Review review = getReviewById(id);
        if (review != null) {
            review.setAdminReply(reply);
            review.setReplyDate(java.time.LocalDateTime.now());
            Review saved = reviewRepository.save(review);

            // Gửi mail báo cho khách biết khách sạn đã phản hồi
            try {
                emailService.sendAdminReplyEmail(saved.getCustomer(), saved);
            } catch (Exception e) {
                System.err.println("Không gửi được mail phản hồi đánh giá: " + e.getMessage());
            }

            return saved;
        }
        return null;
    }

    public List<String> uploadReviewImages(MultipartFile[] files, String username) {
        List<String> imageUrls = new ArrayList<>();
        if (files == null || files.length == 0) return imageUrls;

        for (MultipartFile file : files) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "hotel-style/reviews",
                                "resource_type", "image"
                        )
                );
                imageUrls.add(uploadResult.get("secure_url").toString());
            } catch (Exception e) {
                System.err.println("Lỗi upload ảnh đánh giá: " + e.getMessage());
            }
        }
        return imageUrls;
    }

    private ReviewResponseDTO mapToDTO(Review review, boolean isPublic) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewID(review.getReviewID());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        dto.setReplyDate(review.getReplyDate());

        dto.setCleanlinessRating(review.getCleanlinessRating());
        dto.setServiceRating(review.getServiceRating());
        dto.setFacilitiesRating(review.getFacilitiesRating());
        dto.setLocationRating(review.getLocationRating());
        dto.setImageUrls(review.getImageUrls());
        dto.setAnonymous(review.isAnonymous());

        if (review.getCustomer() != null) {
            if (isPublic && review.isAnonymous()) {
                dto.setCustomerName("Khách ẩn danh");
                dto.setCustomerAvatar("asset/default-avatar.png");
            } else {
                dto.setCustomerName(review.getCustomer().getUsername() + (review.isAnonymous() ? " (Ẩn danh)" : ""));
                dto.setCustomerAvatar(review.getCustomer().getAvatar());
            }
        }

        if (review.getBooking() != null) {
            dto.setBookingID(review.getBooking().getBookingID());
            if (review.getBooking().getBookingDetails() != null && !review.getBooking().getBookingDetails().isEmpty()) {
                BookingDetails firstDetail = review.getBooking().getBookingDetails().get(0);
                dto.setCheckinDate(firstDetail.getCheckinDate());
                dto.setCheckoutDate(firstDetail.getCheckoutDate());
                if (firstDetail.getRoom() != null && firstDetail.getRoom().getRoomType() != null) {
                    dto.setRoomType(firstDetail.getRoom().getRoomType().getTypeName());
                }
            }
        }
        return dto;
    }
}
