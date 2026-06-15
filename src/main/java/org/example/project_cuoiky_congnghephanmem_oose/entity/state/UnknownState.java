package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * Trạng thái dự phòng cho giá trị status null hoặc không nằm trong tập hợp lệ.
 *
 * <p>Mọi guard đều trả về {@code false} (giống hệt cách code cũ xử lý: một status
 * lạ sẽ không khớp "pending"/"confirmed"... nên các điều kiện đều sai) và mọi
 * transition đều ném lỗi. Nhờ vậy việc đưa State Pattern vào KHÔNG làm phát sinh
 * ngoại lệ mới ở những chỗ code cũ vốn chỉ so sánh chuỗi.
 */
public final class UnknownState implements BookingState {

    public static final UnknownState INSTANCE = new UnknownState();

    private UnknownState() {
    }

    @Override
    public String getStatusName() {
        return "unknown";
    }

    @Override
    public String getDisplayName() {
        return "Không xác định";
    }
}
