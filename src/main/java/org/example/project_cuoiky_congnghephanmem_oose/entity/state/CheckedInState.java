package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * Trạng thái "đã nhận phòng" — khách đang lưu trú.
 * Vẫn được tính vào doanh thu. Hệ thống tự chuyển sang trả phòng khi qua ngày check-out.
 */
public final class CheckedInState implements BookingState {

    public static final CheckedInState INSTANCE = new CheckedInState();

    private CheckedInState() {
    }

    @Override
    public String getStatusName() {
        return BookingStatus.CHECKED_IN;
    }

    @Override
    public String getDisplayName() {
        return "Đã nhận phòng";
    }

    @Override
    public boolean isPaid() {
        return true;
    }

    @Override
    public BookingState checkOut() {
        return CheckedOutState.INSTANCE;
    }
}
