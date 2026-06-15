package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * Trạng thái "đã trả phòng" — trạng thái kết thúc của một booking thành công.
 * Vẫn được tính vào doanh thu, không chuyển tiếp được nữa.
 */
public final class CheckedOutState implements BookingState {

    public static final CheckedOutState INSTANCE = new CheckedOutState();

    private CheckedOutState() {
    }

    @Override
    public String getStatusName() {
        return BookingStatus.CHECKED_OUT;
    }

    @Override
    public String getDisplayName() {
        return "Đã trả phòng";
    }

    @Override
    public boolean isPaid() {
        return true;
    }

    @Override
    public boolean isFinal() {
        return true;
    }
}
