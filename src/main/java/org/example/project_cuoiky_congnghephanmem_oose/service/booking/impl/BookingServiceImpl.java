package org.example.project_cuoiky_congnghephanmem_oose.service.booking.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.CreateBookingRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.CreateBookingResponse;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.example.project_cuoiky_congnghephanmem_oose.entity.BookingDetails;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Rooms;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IBookingRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ICustomerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IMembershipTierRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IRoomRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.booking.IBookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingServiceImpl implements IBookingService {

    private static final long HOLD_MINUTES = 15;

    private final IBookingRepository bookingRepository;
    private final ICustomerRepository customerRepository;
    private final IRoomRepository roomRepository;
    private final IMembershipTierRepository membershipTierRepository;

    public BookingServiceImpl(IBookingRepository bookingRepository,
                              ICustomerRepository customerRepository,
                              IRoomRepository roomRepository,
                              IMembershipTierRepository membershipTierRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.roomRepository = roomRepository;
        this.membershipTierRepository = membershipTierRepository;
    }

    @Override
    @Transactional
    public CreateBookingResponse createBooking(String username, CreateBookingRequest request) {
        validateRequest(request);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Lấy discount rate theo hạng thành viên
        double discountRate = getCustomerDiscountRate(customer);

        List<Rooms> selectedRooms = roomRepository.findAllByRoomIds(request.getRoomIds());
        if (selectedRooms.size() != request.getRoomIds().size()) {
            throw new RuntimeException("Một hoặc nhiều phòng không tồn tại");
        }
        if (selectedRooms.size() != request.getNumberOfRooms()) {
            throw new RuntimeException("Số phòng đã chọn không khớp với số lượng phòng yêu cầu");
        }

        int guestsPerRoom = (int) Math.ceil((double) request.getGuests() / request.getNumberOfRooms());

        List<Rooms> availableRooms = roomRepository.findAvailableRooms(
                request.getCheckin(), request.getCheckout(), null, guestsPerRoom);

        Set<Integer> availableRoomIds = new HashSet<>();
        for (Rooms room : availableRooms) {
            availableRoomIds.add(room.getRoomID());
        }

        for (Rooms room : selectedRooms) {
            if (room.isOutOfServiceForStay(request.getCheckin(), request.getCheckout())) {
                throw new RuntimeException("Phòng " + room.getRoomNumber() + " đang bảo trì/ngừng hoạt động trong khoảng ngày này");
            }
            if (room.getRoomType() == null || room.getRoomType().getOccupancy() < guestsPerRoom) {
                throw new RuntimeException("Phòng " + room.getRoomNumber() + " không đủ sức chứa");
            }
            if (!availableRoomIds.contains(room.getRoomID())) {
                throw new RuntimeException("Phòng " + room.getRoomNumber() + " vừa được người khác đặt hoặc không còn trống");
            }
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckin(), request.getCheckout());

        // Tính tổng tiền SAU khi áp dụng giảm giá
        double totalPrice = 0.0;
        List<BookingDetails> bookingDetails = new ArrayList<>();

        for (Rooms room : selectedRooms) {
            double originalPrice = room.getEffectivePrice();
            double discountedPricePerNight = originalPrice * (1 - discountRate);
            double subTotal = discountedPricePerNight * nights;

            BookingDetails detail = BookingDetails.builder()
                    .booking(null) // sẽ set sau
                    .room(room)
                    .checkinDate(request.getCheckin())
                    .checkoutDate(request.getCheckout())
                    .subTotal(subTotal)
                    .build();

            bookingDetails.add(detail);
            totalPrice += subTotal;
        }

        Booking booking = Booking.builder()
                .bookingDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .status("pending")
                .expiredAt(LocalDateTime.now().plusMinutes(HOLD_MINUTES))
                .customer(customer)
                .build();

        for (BookingDetails detail : bookingDetails) {
            detail.setBooking(booking);
        }
        booking.setBookingDetails(bookingDetails);

        bookingRepository.save(booking);

        String discountInfo = discountRate > 0 
                ? " (Đã áp dụng giảm giá " + Math.round(discountRate * 100) + "%)" 
                : "";

        return CreateBookingResponse.builder()
                .bookingID(booking.getBookingID())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .expiredAt(booking.getExpiredAt())
                .message("Tạo booking thành công. Vui lòng thanh toán trong " + HOLD_MINUTES + " phút để giữ phòng.")
                .discountRate(discountRate)
                .discountInfo(discountRate > 0 ? "Giảm " + Math.round(discountRate * 100) + "% theo hạng thành viên" : "Không có giảm giá")
                .build();
    }

    private double getCustomerDiscountRate(Customer customer) {
        if (customer.getMembershipTier() != null) {
            return customer.getMembershipTier().getDiscountRate();
        }

        // Refresh tier theo điểm hiện tại
        return membershipTierRepository
                .findTopByMinPointLessThanEqualOrderByMinPointDesc(customer.getPoint())
                .map(tier -> {
                    customer.setMembershipTier(tier);
                    return tier.getDiscountRate();
                })
                .orElse(0.0);
    }

    private void validateRequest(CreateBookingRequest request) {
        if (request.getCheckin() == null || request.getCheckout() == null) {
            throw new RuntimeException("Thiếu ngày nhận phòng hoặc trả phòng");
        }
        if (!request.getCheckout().isAfter(request.getCheckin())) {
            throw new RuntimeException("Ngày trả phòng phải sau ngày nhận phòng");
        }
        if (request.getCheckin().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày nhận phòng không được ở trong quá khứ");
        }
        if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            throw new RuntimeException("Bạn chưa chọn phòng");
        }
        if (request.getNumberOfRooms() <= 0) {
            throw new RuntimeException("Số lượng phòng phải lớn hơn 0");
        }
        if (request.getGuests() <= 0) {
            throw new RuntimeException("Số lượng khách phải lớn hơn 0");
        }
    }
}