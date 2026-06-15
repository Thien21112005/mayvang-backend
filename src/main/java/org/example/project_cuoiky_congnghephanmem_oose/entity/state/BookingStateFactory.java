package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

import java.util.Locale;

/**
 * Factory ánh xạ chuỗi status (lưu trong DB) sang đối tượng {@link BookingState} tương ứng.
 *
 * <p>So khớp không phân biệt hoa thường (giống {@code equalsIgnoreCase} ở code cũ).
 * Giá trị null hoặc không hợp lệ trả về {@link UnknownState} thay vì ném lỗi, để
 * giữ nguyên hành vi an toàn của code cũ.
 */
public final class BookingStateFactory {

    private BookingStateFactory() {
    }

    public static BookingState from(String status) {
        if (status == null) {
            return UnknownState.INSTANCE;
        }
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case BookingStatus.PENDING -> PendingState.INSTANCE;
            case BookingStatus.CONFIRMED -> ConfirmedState.INSTANCE;
            case BookingStatus.CHECKED_IN -> CheckedInState.INSTANCE;
            case BookingStatus.CHECKED_OUT -> CheckedOutState.INSTANCE;
            case BookingStatus.CANCELLED -> CancelledState.INSTANCE;
            default -> UnknownState.INSTANCE;
        };
    }
}
