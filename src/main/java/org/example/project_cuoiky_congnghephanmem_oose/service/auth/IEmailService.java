package org.example.project_cuoiky_congnghephanmem_oose.service.auth;

import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Payment;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Review;

public interface IEmailService {
    void sendOtpEmail(String toEmail, String otp);
    void sendBookingConfirmationEmail(Customer customer, Booking booking, Payment payment);

    // Cảm ơn sau khi khách trả phòng (check-out)
    void sendCheckoutThankYouEmail(Customer customer, Booking booking);

    // Cảm ơn khi khách gửi đánh giá
    void sendReviewSubmittedEmail(Customer customer, Review review);

    // Thông báo khi khách sạn phản hồi đánh giá
    void sendAdminReplyEmail(Customer customer, Review review);
}
