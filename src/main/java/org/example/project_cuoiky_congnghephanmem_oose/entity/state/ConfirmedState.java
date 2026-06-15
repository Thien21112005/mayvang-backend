package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * Trạng thái "đã xác nhận" — sau khi thanh toán thành công.
 * Được tính vào doanh thu. Hệ thống tự chuyển sang nhận phòng/trả phòng theo ngày.
 */
public final class ConfirmedState implements BookingState {

    public static final ConfirmedState INSTANCE = new ConfirmedState();

    private ConfirmedState() {
    }

    @Override
    public String getStatusName() {
        return BookingStatus.CONFIRMED;
    }

    @Override
    public String getDisplayName() {
        return "Đã xác nhận";
    }

    @Override
    public boolean isPaid() {
        return true;
    }

    @Override
    public BookingState checkIn() {
        return CheckedInState.INSTANCE;
    }

    @Override
    public BookingState checkOut() {
        return CheckedOutState.INSTANCE;
    }
}
