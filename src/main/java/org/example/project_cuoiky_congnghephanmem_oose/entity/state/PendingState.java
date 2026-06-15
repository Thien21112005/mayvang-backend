package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * Trạng thái "chờ thanh toán" — trạng thái khởi điểm của mọi booking mới tạo.
 * Cho phép: thanh toán (→ {@link ConfirmedState}) hoặc hủy (→ {@link CancelledState}).
 */
public final class PendingState implements BookingState {

    public static final PendingState INSTANCE = new PendingState();

    private PendingState() {
    }

    @Override
    public String getStatusName() {
        return BookingStatus.PENDING;
    }

    @Override
    public String getDisplayName() {
        return "Chờ thanh toán";
    }

    @Override
    public boolean isPending() {
        return true;
    }

    @Override
    public boolean canBePaid() {
        return true;
    }

    @Override
    public boolean canBeCancelled() {
        return true;
    }

    @Override
    public BookingState confirmPayment() {
        return ConfirmedState.INSTANCE;
    }

    @Override
    public BookingState cancel() {
        return CancelledState.INSTANCE;
    }
}
