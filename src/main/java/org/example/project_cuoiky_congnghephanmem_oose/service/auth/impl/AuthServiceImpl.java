package org.example.project_cuoiky_congnghephanmem_oose.service.auth.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.LoginRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.RegisterRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.AuthResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.User;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ICustomerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IUserRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IAuthService;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IEmailService;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IOtpService;
import org.example.project_cuoiky_congnghephanmem_oose.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements IAuthService {

    private final IUserRepository userRepository;
    private final ICustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IEmailService emailService;
    private final IOtpService otpService;

    @Value("${google.client-id}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    // Lưu tạm thông tin đăng ký trong lúc chờ xác thực OTP (key = email viết thường)
    private final Map<String, RegisterRequest> pendingRegistrations = new ConcurrentHashMap<>();

    public AuthServiceImpl(IUserRepository userRepository,
                           ICustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           IEmailService emailService,
                           IOtpService otpService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    // ── Đăng ký bước 1: kiểm tra dữ liệu + gửi OTP (chưa tạo tài khoản) ──────────
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        String email = request.getEmail().trim().toLowerCase();

        // Giữ lại dữ liệu đăng ký, gửi OTP về email
        pendingRegistrations.put(email, request);
        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(request.getEmail(), otp);

        return new AuthResponse(
                "Mã OTP đã được gửi tới email của bạn. Vui lòng kiểm tra hộp thư để hoàn tất đăng ký.",
                null, request.getUsername(), "CUSTOMER");
    }

    // ── Đăng ký bước 2: xác thực OTP -> tạo tài khoản ───────────────────────────
    @Override
    public AuthResponse verifyRegister(String email, String otpCode) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Thiếu email xác thực!");
        }
        String key = email.trim().toLowerCase();

        if (!otpService.validateOtp(key, otpCode)) {
            throw new RuntimeException("Mã OTP không chính xác hoặc đã hết hạn!");
        }

        RegisterRequest request = pendingRegistrations.get(key);
        if (request == null) {
            throw new RuntimeException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại!");
        }

        // Kiểm tra lại lần cuối phòng trường hợp có người vừa đăng ký trùng
        if (userRepository.existsByUsername(request.getUsername())) {
            pendingRegistrations.remove(key);
            throw new RuntimeException("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            pendingRegistrations.remove(key);
            throw new RuntimeException("Email đã được sử dụng!");
        }

        Customer customer = new Customer();
        customer.setUsername(request.getUsername());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setAuthProvider("LOCAL");
        customerRepository.save(customer);

        pendingRegistrations.remove(key);

        return new AuthResponse("Đăng ký thành công!", null, customer.getUsername(), "CUSTOMER");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("Username hoặc email không tồn tại!"));

        if (user.isGoogleAccount()) {
            throw new RuntimeException("Tài khoản này đăng nhập bằng Google. Vui lòng dùng nút \"Đăng nhập với Google\".");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng!");
        }

        String role = user.getRole().name();
        String token = jwtUtil.generateToken(user.getUsername(), role);

        return new AuthResponse("Đăng nhập thành công!", token, user.getUsername(), role);
    }

    // ── Đăng nhập bằng Google ───────────────────────────────────────────────────
    @Override
    public AuthResponse loginWithGoogle(String credential) {
        GoogleProfile profile = verifyGoogleToken(credential);

        User user = userRepository.findByEmail(profile.email()).orElse(null);

        if (user != null && !user.isGoogleAccount()) {
            // Tài khoản đã đăng ký bằng form local -> không cho đăng nhập qua Google
            throw new RuntimeException(
                    "Email này đã được đăng ký bằng tài khoản thường. "
                  + "Vui lòng đăng nhập bằng username và mật khẩu.");
        }

        if (user == null) {
            // Lần đầu đăng nhập Google -> tự tạo tài khoản khách hàng
            Customer customer = new Customer();
            customer.setUsername(generateUniqueUsername(profile.email(), profile.name()));
            customer.setEmail(profile.email());
            customer.setAvatar(profile.picture());
            // Tài khoản Google không dùng mật khẩu -> đặt mật khẩu ngẫu nhiên không thể đoán
            customer.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            customer.setAuthProvider("GOOGLE");
            customerRepository.save(customer);
            user = customer;
        } else {
            // Đã có tài khoản Google -> cập nhật avatar mới nhất từ Google
            if (profile.picture() != null && !profile.picture().isBlank()) {
                user.setAvatar(profile.picture());
                userRepository.save(user);
            }
        }

        String role = user.getRole().name();
        String token = jwtUtil.generateToken(user.getUsername(), role);

        return new AuthResponse("Đăng nhập Google thành công!", token, user.getUsername(), role);
    }

    /**
     * Xác thực ID token của Google qua endpoint tokeninfo và kiểm tra aud (client_id) hợp lệ.
     */
    private GoogleProfile verifyGoogleToken(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new RuntimeException("Thiếu credential từ Google!");
        }

        Map<?, ?> payload;
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;
            payload = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn!");
        }
        if (payload == null) {
            throw new RuntimeException("Không xác thực được tài khoản Google!");
        }

        Object aud = payload.get("aud");
        if (googleClientId != null && !googleClientId.isBlank()
                && !googleClientId.equals(String.valueOf(aud))) {
            throw new RuntimeException("Token Google không dành cho ứng dụng này!");
        }

        Object emailVerified = payload.get("email_verified");
        if (!"true".equalsIgnoreCase(String.valueOf(emailVerified))) {
            throw new RuntimeException("Email Google chưa được xác thực!");
        }

        Object email = payload.get("email");
        if (email == null) {
            throw new RuntimeException("Không lấy được email từ tài khoản Google!");
        }

        return new GoogleProfile(
                String.valueOf(email).toLowerCase(),
                payload.get("name") != null ? String.valueOf(payload.get("name")) : "",
                payload.get("picture") != null ? String.valueOf(payload.get("picture")) : ""
        );
    }

    private String generateUniqueUsername(String email, String name) {
        String base;
        if (name != null && !name.isBlank()) {
            // Chuẩn hóa Unicode -> tách dấu -> xóa dấu -> giữ lại a-z và số
            String normalized = java.text.Normalizer.normalize(name.trim(), java.text.Normalizer.Form.NFD);
            normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            // Xử lý thêm ký tự đặc biệt tiếng Việt: đ -> d
            normalized = normalized.replace('đ', 'd').replace('Đ', 'D');
            base = normalized.toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int counter = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + counter;
            counter++;
        }
        return candidate;
    }

    private record GoogleProfile(String email, String name, String picture) {
    }

    @Override
    public void forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email không tồn tại trong hệ thống!");
        }
        String otp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public boolean verifyOtp(String email, String otpCode) {
        return otpService.validateOtp(email, otpCode);
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

        if (user.isGoogleAccount()) {
            throw new RuntimeException("Tài khoản Google không hỗ trợ đặt lại mật khẩu!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
