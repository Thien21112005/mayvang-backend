package org.example.project_cuoiky_congnghephanmem_oose.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.PaymentUrlResponse;
import org.example.project_cuoiky_congnghephanmem_oose.service.payment.IPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${frontend.url:https://mayvang.vercel.app}")
    private String frontendUrl;

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/vnpay/{bookingId}")
    public ResponseEntity<PaymentUrlResponse> createPaymentUrl(
            Authentication authentication,
            @PathVariable int bookingId,
            HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        return ResponseEntity.ok(paymentService.createVnPayUrl(authentication.getName(), bookingId, ipAddress));
    }

    @GetMapping("/vnpay-return")
    public void vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response
    ) throws IOException {
        Map<String, String> result = paymentService.handleVnPayReturn(params);

        String status = URLEncoder.encode(result.getOrDefault("status", "failed"), StandardCharsets.UTF_8);
        String message = URLEncoder.encode(result.getOrDefault("message", "Thanh toán thất bại"), StandardCharsets.UTF_8);
        String bookingId = URLEncoder.encode(result.getOrDefault("bookingId", ""), StandardCharsets.UTF_8);
        String amount = URLEncoder.encode(result.getOrDefault("amount", "0"), StandardCharsets.UTF_8);
        String transactionCode = URLEncoder.encode(result.getOrDefault("transactionCode", ""), StandardCharsets.UTF_8);
        String earnedPoint = URLEncoder.encode(result.getOrDefault("earnedPoint", "0"), StandardCharsets.UTF_8);
        String bookingStatus = URLEncoder.encode(result.getOrDefault("bookingStatus", "pending"), StandardCharsets.UTF_8);

        String redirectUrl = frontendUrl + "/payment-result.html"
                + "?status=" + status
                + "&message=" + message
                + "&bookingId=" + bookingId
                + "&amount=" + amount
                + "&transactionCode=" + transactionCode
                + "&earnedPoint=" + earnedPoint
                + "&bookingStatus=" + bookingStatus;

        response.sendRedirect(redirectUrl);
    }
}