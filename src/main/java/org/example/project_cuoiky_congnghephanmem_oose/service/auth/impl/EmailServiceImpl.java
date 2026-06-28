package org.example.project_cuoiky_congnghephanmem_oose.service.auth.impl;

import jakarta.mail.internet.MimeMessage;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.BookingDetails;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Payment;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Review;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:huyalex009@gmail.com}")
    private String fromEmail;

    @Value("${brevo.sender.name:Mây Vàng}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    public EmailServiceImpl() {
    }

    // ── Gửi 1 email HTML qua Brevo API ──────────────────────────────────────────
    private void sendHtml(String to, String subject, String html) throws Exception {
        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("Accept", "application/json");

        Map<String, Object> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", fromEmail);

        Map<String, Object> toRecipient = new HashMap<>();
        toRecipient.put("email", to);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(toRecipient));
        body.put("subject", subject);
        body.put("htmlContent", html);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Brevo API trả về lỗi: " + response.getBody());
        }
    }

    // ── Khung email sang trọng dùng chung (header vàng + footer), tối giản icon ──
    private String wrapEmail(String title, String bodyHtml) {
        return ""
            + "<div style=\"margin:0;padding:0;background:#f4f1ea;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\" style=\"padding:36px 16px;\">"
            + "<table width=\"100%\" style=\"max-width:620px;background:#ffffff;border-radius:20px;overflow:hidden;"
            +   "box-shadow:0 12px 40px rgba(160,120,20,0.12);border:1px solid #efe6d4;\">"
            // Header
            + "<tr><td align=\"center\" style=\"padding:46px 40px 38px;background:linear-gradient(135deg,#FF8C00,#D4A017);\">"
            +   "<div style=\"font-size:11px;letter-spacing:6px;color:rgba(255,255,255,0.85);text-transform:uppercase;margin-bottom:12px;\">Luxury Experience</div>"
            +   "<div style=\"font-size:30px;letter-spacing:8px;color:#ffffff;font-weight:700;text-transform:uppercase;\">MÂY VÀNG</div>"
            +   "<div style=\"width:48px;height:2px;background:rgba(255,255,255,0.6);margin:18px auto 0;\"></div>"
            +   "<div style=\"margin-top:16px;color:#ffffff;font-size:16px;letter-spacing:1px;\">" + title + "</div>"
            + "</td></tr>"
            // Body
            + "<tr><td style=\"padding:40px;color:#4a3f35;font-size:15px;line-height:1.7;\">" + bodyHtml + "</td></tr>"
            // Footer
            + "<tr><td align=\"center\" style=\"padding:30px 40px;background:#faf7f0;border-top:1px solid #efe6d4;\">"
            +   "<div style=\"font-style:italic;color:#b08a1e;font-size:15px;\">\"Chọn chúng tôi, chọn phong cách!\"</div>"
            +   "<div style=\"margin-top:14px;color:#b8ab9c;font-size:11px;letter-spacing:1px;text-transform:uppercase;\">"
            +     "&copy; 2026 Mây Vàng &middot; S&#7889; 1 V&#245; V&#259;n Ng&#7847;n, Th&#7911; &#272;&#7913;c, TP.HCM</div>"
            + "</td></tr>"
            + "</table></td></tr></table></div>";
    }

    // ── OTP ─────────────────────────────────────────────────────────────────────
    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            String body = ""
                + "<h2 style=\"margin:0 0 14px;color:#4a3f35;font-size:22px;font-weight:600;text-align:center;\">Mã xác thực của bạn</h2>"
                + "<p style=\"margin:0 0 28px;color:#7a6e66;text-align:center;\">Vui lòng nhập mã OTP dưới đây để hoàn tất xác thực tài khoản tại <b>Mây Vàng</b>.</p>"
                + "<div style=\"text-align:center;margin-bottom:28px;\">"
                +   "<div style=\"display:inline-block;background:#fbf6ec;border:1px solid #e7d9bd;border-radius:14px;padding:24px 36px;\">"
                +     "<span style=\"font-family:'Consolas',monospace;font-size:40px;font-weight:700;letter-spacing:12px;color:#c46a10;\">" + otp + "</span>"
                +   "</div>"
                + "</div>"
                + "<p style=\"margin:0;color:#a8998d;font-size:13px;text-align:center;\">Mã có hiệu lực trong vòng <b style=\"color:#c46a10;\">5 phút</b>. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>";
            sendHtml(toEmail, "[Mây Vàng] Mã OTP xác thực tài khoản", wrapEmail("Xác thực tài khoản", body));
        } catch (Exception e) {
            throw new RuntimeException("Gửi mail OTP thất bại: " + e.getMessage());
        }
    }

    // ── Xác nhận đặt phòng ──────────────────────────────────────────────────────
    @Override
    public void sendBookingConfirmationEmail(Customer customer, Booking booking, Payment payment) {
        try {
            StringBuilder rows = new StringBuilder();
            for (BookingDetails detail : booking.getBookingDetails()) {
                rows.append("<tr>")
                    .append(td("<b style=\"color:#4a3f35;\">" + detail.getRoom().getRoomNumber() + "</b>"))
                    .append(td("<span style=\"color:#7a6e66;\">" + detail.getRoom().getRoomType().getTypeName() + "</span>"))
                    .append(td("<span style=\"color:#7a6e66;\">" + detail.getCheckinDate() + " &rarr; " + detail.getCheckoutDate() + "</span>"))
                    .append("<td style=\"padding:14px;border-bottom:1px solid #f0e8d8;text-align:right;color:#c46a10;font-weight:700;\">" + money(detail.getSubTotal()) + "</td>")
                    .append("</tr>");
            }

            String body = ""
                + "<p style=\"margin:0 0 6px;font-size:17px;\">Xin chào <b style=\"color:#1d1d1f;\">" + customer.getUsername() + "</b>,</p>"
                + "<p style=\"margin:0 0 26px;color:#7a6e66;\">Cảm ơn quý khách đã tin tưởng lựa chọn <b>Mây Vàng</b>. Chúng tôi rất hân hạnh được phục vụ quý khách trong kỳ nghỉ sắp tới.</p>"
                + "<div style=\"background:#fbf6ec;border:1px solid #e7d9bd;border-radius:16px;padding:22px 24px;margin-bottom:30px;\">"
                +   summaryRow("Mã booking", "#" + booking.getBookingID())
                +   summaryRow("Trạng thái", booking.getStatus())
                +   summaryRow("Mã giao dịch", payment.getTransactionCode())
                +   "<div style=\"border-top:1px dashed #e0d2b6;margin:12px 0;\"></div>"
                +   "<table width=\"100%\"><tr><td style=\"font-size:16px;font-weight:700;color:#4a3f35;\">Tổng thanh toán</td>"
                +     "<td style=\"text-align:right;font-size:20px;font-weight:800;color:#c46a10;\">" + money(booking.getTotalPrice()) + "</td></tr></table>"
                + "</div>"
                + sectionTitle("Chi tiết dịch vụ")
                + "<table width=\"100%\" style=\"border-collapse:collapse;margin-bottom:30px;\">"
                +   "<thead><tr style=\"background:#fbf2e0;\">"
                +     th("Phòng") + th("Loại") + th("Thời gian")
                +     "<th style=\"padding:11px 14px;text-align:right;color:#b8860b;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;\">Thành tiền</th>"
                +   "</tr></thead><tbody>" + rows + "</tbody></table>"
                + sectionTitle("Hướng dẫn nhận phòng")
                + "<div style=\"background:#faf7f0;border:1px solid #efe6d4;border-radius:14px;padding:22px 24px;\">"
                +   guideRow("Địa chỉ", "Khách sạn Mây Vàng — Số 1 Võ Văn Ngân, Phường Linh Chiểu, Thành phố Thủ Đức, TP. Hồ Chí Minh.")
                +   guideRow("Thời gian", "Nhận phòng từ 14:00 &middot; Trả phòng trước 12:00 trưa hôm sau.")
                +   guideRowLast("Giấy tờ cần chuẩn bị", "Vui lòng mang theo CMND/CCCD hoặc Hộ chiếu bản gốc của người đặt phòng khi làm thủ tục nhận phòng.")
                + "</div>";

            sendHtml(customer.getEmail(),
                    "[Mây Vàng] Xác nhận đặt phòng thành công - #" + booking.getBookingID(),
                    wrapEmail("Xác nhận đặt phòng", body));
        } catch (Exception e) {
            throw new RuntimeException("Gửi mail xác nhận thất bại: " + e.getMessage());
        }
    }

    // ── Cảm ơn sau khi trả phòng (check-out) ────────────────────────────────────
    @Override
    public void sendCheckoutThankYouEmail(Customer customer, Booking booking) {
        if (customer == null || customer.getEmail() == null) return;
        try {
            String stay = "";
            if (booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()) {
                BookingDetails d = booking.getBookingDetails().get(0);
                stay = summaryRow("Thời gian lưu trú", d.getCheckinDate() + " &rarr; " + d.getCheckoutDate());
            }
            String body = ""
                + "<p style=\"margin:0 0 6px;font-size:17px;\">Xin chào <b style=\"color:#1d1d1f;\">" + customer.getUsername() + "</b>,</p>"
                + "<p style=\"margin:0 0 24px;color:#7a6e66;\">Cảm ơn quý khách đã lựa chọn <b>Mây Vàng</b> cho kỳ nghỉ vừa qua. Thật vinh hạnh khi được đồng hành cùng quý khách, và chúng tôi hy vọng đã mang đến những trải nghiệm trọn vẹn nhất.</p>"
                + "<div style=\"background:#fbf6ec;border:1px solid #e7d9bd;border-radius:16px;padding:22px 24px;margin-bottom:26px;\">"
                +   summaryRow("Mã booking", "#" + booking.getBookingID())
                +   stay
                + "</div>"
                + "<p style=\"margin:0 0 24px;color:#7a6e66;\">Đánh giá của quý khách là nguồn động viên quý giá giúp chúng tôi hoàn thiện hơn mỗi ngày. Quý khách có thể chia sẻ cảm nhận trong mục <b>Lịch sử đặt phòng</b> trên website.</p>"
                + "<p style=\"margin:0;color:#7a6e66;\">Hẹn gặp lại quý khách trong những hành trình sắp tới!</p>";
            sendHtml(customer.getEmail(),
                    "[Mây Vàng] Cảm ơn quý khách đã lưu trú - #" + booking.getBookingID(),
                    wrapEmail("Cảm ơn quý khách", body));
        } catch (Exception e) {
            System.err.println("Gửi mail cảm ơn check-out thất bại: " + e.getMessage());
        }
    }

    // ── Cảm ơn khi khách gửi đánh giá ───────────────────────────────────────────
    @Override
    public void sendReviewSubmittedEmail(Customer customer, Review review) {
        if (customer == null || customer.getEmail() == null) return;
        try {
            String comment = review.getComment() != null ? review.getComment() : "";
            String body = ""
                + "<p style=\"margin:0 0 6px;font-size:17px;\">Xin chào <b style=\"color:#1d1d1f;\">" + customer.getUsername() + "</b>,</p>"
                + "<p style=\"margin:0 0 24px;color:#7a6e66;\">Cảm ơn quý khách đã dành thời gian gửi đánh giá cho <b>Mây Vàng</b>. Mỗi chia sẻ của quý khách đều giúp chúng tôi phục vụ ngày một tốt hơn.</p>"
                + "<div style=\"background:#fbf6ec;border:1px solid #e7d9bd;border-radius:16px;padding:22px 24px;margin-bottom:26px;\">"
                +   summaryRow("Mức đánh giá", String.format("%.1f", review.getRating()) + " / 5.0")
                +   "<div style=\"border-top:1px dashed #e0d2b6;margin:12px 0;\"></div>"
                +   "<div style=\"color:#7a6e66;font-style:italic;\">\"" + comment + "\"</div>"
                + "</div>"
                + "<p style=\"margin:0;color:#7a6e66;\">Mây Vàng sẽ xem xét và có thể phản hồi đánh giá của quý khách trong thời gian sớm nhất. Trân trọng cảm ơn!</p>";
            sendHtml(customer.getEmail(),
                    "[Mây Vàng] Cảm ơn bạn đã gửi đánh giá",
                    wrapEmail("Cảm ơn đánh giá của bạn", body));
        } catch (Exception e) {
            System.err.println("Gửi mail cảm ơn đánh giá thất bại: " + e.getMessage());
        }
    }

    // ── Khi Mây Vàng phản hồi đánh giá ─────────────────────────────────────────
    @Override
    public void sendAdminReplyEmail(Customer customer, Review review) {
        if (customer == null || customer.getEmail() == null) return;
        try {
            String comment = review.getComment() != null ? review.getComment() : "";
            String reply = review.getAdminReply() != null ? review.getAdminReply() : "";
            String body = ""
                + "<p style=\"margin:0 0 6px;font-size:17px;\">Xin chào <b style=\"color:#1d1d1f;\">" + customer.getUsername() + "</b>,</p>"
                + "<p style=\"margin:0 0 24px;color:#7a6e66;\">Khách sạn <b>Mây Vàng</b> đã gửi phản hồi cho đánh giá của quý khách. Xin chân thành cảm ơn những góp ý quý báu.</p>"
                + "<div style=\"background:#f7f7f8;border:1px solid #e6e6ea;border-radius:14px;padding:18px 20px;margin-bottom:18px;\">"
                +   "<div style=\"font-size:12px;text-transform:uppercase;letter-spacing:0.5px;color:#9a9aa2;margin-bottom:6px;\">Đánh giá của bạn</div>"
                +   "<div style=\"color:#555;font-style:italic;\">\"" + comment + "\"</div>"
                + "</div>"
                + "<div style=\"background:#fbf6ec;border:1px solid #e7d9bd;border-left:3px solid #D4A017;border-radius:14px;padding:18px 20px;\">"
                +   "<div style=\"font-size:12px;text-transform:uppercase;letter-spacing:0.5px;color:#b8860b;margin-bottom:6px;\">Phản hồi từ Mây Vàng</div>"
                +   "<div style=\"color:#4a3f35;\">" + reply + "</div>"
                + "</div>";
            sendHtml(customer.getEmail(),
                    "[Mây Vàng] Mây Vàng đã phản hồi đánh giá của bạn",
                    wrapEmail("Phản hồi từ Mây Vàng", body));
        } catch (Exception e) {
            System.err.println("Gửi mail phản hồi đánh giá thất bại: " + e.getMessage());
        }
    }

    // ── Helpers dựng HTML ───────────────────────────────────────────────────────
    private String money(double v) {
        return String.format("%,.0f VNĐ", v);
    }

    private String td(String content) {
        return "<td style=\"padding:14px;border-bottom:1px solid #f0e8d8;\">" + content + "</td>";
    }

    private String th(String label) {
        return "<th style=\"padding:11px 14px;text-align:left;color:#b8860b;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;\">" + label + "</th>";
    }

    private String summaryRow(String label, String value) {
        return "<table width=\"100%\"><tr>"
            + "<td style=\"padding:5px 0;color:#7a6e66;\">" + label + "</td>"
            + "<td style=\"padding:5px 0;text-align:right;color:#4a3f35;font-weight:600;\">" + value + "</td>"
            + "</tr></table>";
    }

    private String sectionTitle(String t) {
        return "<h3 style=\"font-size:17px;color:#4a3f35;margin:0 0 16px;padding-left:14px;border-left:3px solid #D4A017;\">" + t + "</h3>";
    }

    private String guideRow(String label, String value) {
        return "<div style=\"margin-bottom:16px;\">"
            + "<div style=\"font-weight:700;color:#4a3f35;margin-bottom:3px;\">" + label + "</div>"
            + "<div style=\"color:#7a6e66;\">" + value + "</div>"
            + "</div>";
    }

    private String guideRowLast(String label, String value) {
        return "<div>"
            + "<div style=\"font-weight:700;color:#4a3f35;margin-bottom:3px;\">" + label + "</div>"
            + "<div style=\"color:#7a6e66;\">" + value + "</div>"
            + "</div>";
    }
}
