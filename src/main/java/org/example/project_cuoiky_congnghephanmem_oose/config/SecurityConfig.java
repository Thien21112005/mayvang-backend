package org.example.project_cuoiky_congnghephanmem_oose.config;

import org.example.project_cuoiky_congnghephanmem_oose.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/register/verify",
                                "/api/auth/google",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-otp",
                                "/api/auth/reset-password",
                                "/api/rooms",
                                "/api/rooms/search",
                                "/api/room-types",
                                "/api/payments/vnpay-return",
                                "/api/reviews/public",
                                "/api/scheduler/stats"
                        ).permitAll()

                        .requestMatchers(
                                "/api/manager/**",
                                "/api/rooms/*" // PUT /api/rooms/{id} chỉ dành cho MANAGER
                        ).hasRole("MANAGER")

                        // Phân quyền cho phần Reply của Review (Chỉ Manager mới được phản hồi)
                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT, "/api/reviews/*/reply"
                        ).hasRole("MANAGER")
                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE, "/api/reviews/*/reply"
                        ).hasRole("MANAGER")

                        .requestMatchers(
                                "/api/bookings/**",
                                "/api/payments/**",
                                "/api/users/**",
                                "/api/reviews/**" // Các endpoint còn lại của reviews (POST, PUT, DELETE review, /me)
                        ).hasRole("CUSTOMER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}