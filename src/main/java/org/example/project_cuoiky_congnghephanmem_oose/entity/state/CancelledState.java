package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * Trạng thái "đã hủy" — trạng thái kết thúc (do hết hạn giữ phòng, khách tự hủy,
 * hoặc thanh toán thất bại và hết hạn). Không tính doanh thu, không chuyển tiếp.
 */
public final class CancelledState implements BookingState {

    public static final CancelledState INSTANCE = new CancelledState();

    private CancelledState() {
    }

    @Override
    public String getStatusName() {
        return BookingStatus.CANCELLED;
    }

    @Override
    public String getDisplayName() {
        return "Đã hủy";
    }

    @Override
    public boolean isFinal() {
        return true;
    }
}
