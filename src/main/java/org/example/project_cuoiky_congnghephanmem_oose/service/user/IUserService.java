package org.example.project_cuoiky_congnghephanmem_oose.service.user;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.UpdateProfileRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.BookingHistoryResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.UserProfileResponse;

import java.util.List;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.ChangePasswordRequest;

public interface IUserService {
    UserProfileResponse getMyProfile(String username);
    UserProfileResponse updateMyProfile(String username, UpdateProfileRequest request);
    List<BookingHistoryResponse> getMyBookingHistory(String username);
    void changePassword(String username, ChangePasswordRequest request);
    void cancelBooking(String username, Integer bookingId);
}