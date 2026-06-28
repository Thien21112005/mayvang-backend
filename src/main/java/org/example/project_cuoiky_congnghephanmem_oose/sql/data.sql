use hotel;
INSERT INTO Beds (bedName, capacity) VALUES
                                         ('Single', 1),
                                         ('Double', 2),
                                         ('Queen', 2),
                                         ('King', 2),
                                         ('Twin', 1),
                                         ('Super King', 2),
                                         ('Sofa Bed', 2),
                                         ('Bunk Bed (2 tầng)', 4),
                                         ('Extra Bed (gấp gọn)', 1);
INSERT INTO MembershipTier (tierName, minPoint, discountRate, benefits) VALUES
                                                                            ('Bronze',      0,     0.00, 'Giá gốc, hỗ trợ cơ bản'),
                                                                            ('Silver',    800,     0.05, 'Giảm 5%, ưu tiên check-in 15 phút'),
                                                                            ('Gold',     2500,     0.10, 'Giảm 10%, check-out muộn đến 14:00, nước uống chào mừng'),
                                                                            ('Platinum', 6000,     0.15, 'Giảm 15%, nâng hạng phòng miễn phí (nếu có), check-out muộn đến 16:00, quà tặng chào mừng');
INSERT INTO RoomTypes (typeName, priceRoom, occupancy) VALUES
                                                           ('Basic',             680000, 2),
                                                           ('Superior',         1050000, 3),
                                                           ('Deluxe',           1480000, 3),
                                                           ('Royal',            2450000, 4),
                                                           ('Junior Suite',     3850000, 4),
                                                           ('Family Suite',     4200000, 6),
                                                           ('Standard',          480000, 1),
                                                           ('Presidential Suite', 7900000, 6);
INSERT INTO RoomTypeBeds (quantity, typeID, bedID) VALUES
                                                       (2, 1, 1),   -- Basic: 2 Single
                                                       (1, 1, 2),   -- Basic: 1 Double
                                                       (1, 2, 3),   -- Superior: 1 Queen
                                                       (1, 2, 7),   -- Superior: + Sofa bed
                                                       (1, 3, 4),   -- Deluxe: 1 King
                                                       (2, 3, 3),   -- Deluxe: 2 Queen
                                                       (1, 4, 6),   -- Royal: 1 Super King
                                                       (1, 4, 7),   -- Royal: + Sofa bed
                                                       (1, 5, 6),   -- Junior Suite: Super King
                                                       (1, 5, 2),   -- Junior Suite: + Double
                                                       (1, 6, 4),   -- Family Suite: 1 King
                                                       (2, 6, 1),   -- Family Suite: 2 Single
                                                       (1, 6, 7),   -- Family Suite: + Sofa bed
                                                       (1, 7, 5),   -- Standard: 1 Twin
                                                       (1, 8, 6),   -- Presidential: Super King
                                                       (1, 8, 3),   -- Presidential: + Queen (phòng ngủ phụ)
                                                       (1, 8, 7);   -- Presidential: + Sofa bed
INSERT INTO Rooms (roomNumber, status, description, typeID) VALUES
                                                                ('B101', 'available', 'View phố, yên tĩnh', 1),
                                                                ('B102', 'available', 'View phố', 1),
                                                                ('B103', 'maintenance', 'View phố, ban công nhỏ', 1),
                                                                ('B104', 'available', '', 1),
                                                                ('B105', 'available', 'View vườn nhỏ, thoáng mát', 1),
                                                                ('B106', 'available', '', 1),
                                                                ('B107', 'available', 'Gần thang máy, tiện di chuyển', 1),
                                                                ('B108', 'available', '', 1),
                                                                ('B109', 'available', 'Góc yên tĩnh, cách âm tốt', 1),
                                                                ('B110', 'available', '', 1),
                                                                ('B201', 'available', 'Tầng 2, view sân trong', 1),
                                                                ('B202', 'available', '', 1),
                                                                ('B203', 'available', 'Cửa sổ lớn đón nắng sớm', 1),
                                                                ('B204', 'available', '', 1),
                                                                ('B205', 'available', '', 1),
                                                                ('B206', 'available', 'Gần khu vực tiện ích, năng động', 1),
                                                                ('B207', 'available', '', 1),
                                                                ('B208', 'available', '', 1),
                                                                ('B209', 'available', 'View công viên nhỏ', 1),
                                                                ('B210', 'available', '', 1),
                                                                ('B301', 'available', 'Tầng 3, view thành phố ban đêm', 1),
                                                                ('B302', 'available', '', 1),
                                                                ('B303', 'available', '', 1),
                                                                ('B304', 'available', 'Gần lối thoát hiểm, an toàn', 1),
                                                                ('B305', 'available', '', 1),
                                                                ('B306', 'available', '', 1),
                                                                ('B307', 'available', 'View mái nhà cổ khu phố', 1),
                                                                ('B308', 'available', '', 1),
                                                                ('B309', 'available', '', 1),
                                                                ('B310', 'inactive', 'Tầng 3, view sân trong yên tĩnh', 1),
                                                                ('B401', 'available', 'Tầng 4, view toàn cảnh khu trung tâm', 1),
                                                                ('B402', 'available', '', 1),
                                                                ('B403', 'available', '', 1),
                                                                ('B404', 'available', 'Cách âm tốt, phù hợp nghỉ ngơi', 1),
                                                                ('B405', 'available', '', 1),
                                                                ('B406', 'available', '', 1),
                                                                ('B407', 'available', 'Gần khu vực ăn sáng', 1),
                                                                ('B408', 'available', '', 1),
                                                                ('B409', 'available', '', 1),
                                                                ('B410', 'available', 'View đường phố sáng đèn về đêm', 1),
                                                                ('B501', 'available', 'Tầng cao nhất khu B, view rộng', 1),
                                                                ('B502', 'available', '', 1),
                                                                ('B503', 'available', 'Ban công nhỏ hướng Đông, đón nắng sớm', 1),
                                                                ('B504', 'available', '', 1),
                                                                ('B505', 'available', '', 1),
                                                                ('S601', 'available', 'View sông, ban công nhỏ', 2),
                                                                ('S602', 'available', 'View sông', 2),
                                                                ('S603', 'maintenance', 'View hồ bơi, ban công riêng', 2),
                                                                ('S604', 'available', 'View hồ bơi, gần khu BBQ', 2),
                                                                ('S605', 'available', '', 2),
                                                                ('S606', 'available', 'Ban công riêng hướng sông', 2),
                                                                ('S607', 'available', '', 2),
                                                                ('S608', 'available', '', 2),
                                                                ('S609', 'available', 'Góc yên tĩnh, view vườn cây', 2),
                                                                ('S610', 'available', '', 2),
                                                                ('S701', 'available', 'Tầng 7, view sông toàn cảnh', 2),
                                                                ('S702', 'available', '', 2),
                                                                ('S703', 'available', 'Gần spa & gym', 2),
                                                                ('S704', 'available', '', 2),
                                                                ('S705', 'maintenance', 'Tầng 7, view thành phố và sông', 2),
                                                                ('S706', 'available', '', 2),
                                                                ('S707', 'available', 'View hồ bơi vô cực', 2),
                                                                ('S708', 'available', '', 2),
                                                                ('S709', 'available', '', 2),
                                                                ('S710', 'available', 'Cửa sổ lớn đón nắng sớm', 2),
                                                                ('S801', 'available', 'Tầng 8, view sông và thành phố', 2),
                                                                ('S802', 'available', '', 2),
                                                                ('S803', 'available', 'Ban công riêng hướng Đông', 2),
                                                                ('S804', 'available', '', 2),
                                                                ('S805', 'available', '', 2),
                                                                ('D901', 'available', 'Ban công lớn, view thành phố', 3),
                                                                ('D902', 'available', 'View thành phố', 3),
                                                                ('D903', 'available', '', 3),
                                                                ('D904', 'available', 'View núi, không khí trong lành', 3),
                                                                ('D905', 'available', '', 3),
                                                                ('D906', 'available', 'Tầng cao, toàn cảnh hoàng hôn', 3),
                                                                ('D907', 'available', '', 3),
                                                                ('D908', 'available', '', 3),
                                                                ('D909', 'available', 'View sân vườn Nhật Bản', 3),
                                                                ('D910', 'available', '', 3),
                                                                ('D1001', 'available', 'Tầng 10, view toàn cảnh thành phố', 3),
                                                                ('D1002', 'available', '', 3),
                                                                ('D1003', 'available', 'Ban công lớn, view sông', 3),
                                                                ('D1004', 'available', '', 3),
                                                                ('D1005', 'available', '', 3),
                                                                ('D1101', 'available', 'View núi và rừng cây xanh mát', 3),
                                                                ('D1102', 'available', '', 3),
                                                                ('D1103', 'available', 'Gần spa & gym, tiện nghi', 3),
                                                                ('D1104', 'available', '', 3),
                                                                ('D1105', 'maintenance', 'View núi, không gian thoáng đãng', 3),
                                                                ('R1201', 'available', 'Royal góc – view panorama', 4),
                                                                ('R1202', 'available', 'Royal trung tâm', 4),
                                                                ('R1203', 'available', 'View biển từ ban công riêng', 4),
                                                                ('R1204', 'available', '', 4),
                                                                ('R1301', 'available', 'Tầng 13, view toàn cảnh thành phố về đêm', 4),
                                                                ('R1302', 'available', '', 4),
                                                                ('R1303', 'available', 'Phòng góc, 2 mặt cửa sổ view núi và sông', 4),
                                                                ('R1304', 'available', '', 4),
                                                                ('R1401', 'available', 'Tầng cao nhất khu Royal, view 360 độ', 4),
                                                                ('R1402', 'available', '', 4),
                                                                ('JS1501', 'available', 'Junior Suite – phòng khách riêng', 5),
                                                                ('JS1502', 'available', 'View hồ bơi vô cực và thành phố', 5),
                                                                ('JS1503', 'available', '', 5),
                                                                ('JS1504', 'available', 'Ban công lớn hướng biển', 5),
                                                                ('FS1601', 'available', 'Family Suite – 2 phòng ngủ', 6),
                                                                ('FS1602', 'available', 'View vườn nhiệt đới, sân chơi trẻ em gần kề', 6),
                                                                ('FS1603', 'available', '', 6),
                                                                ('FS1604', 'available', 'Tầng cao, toàn cảnh núi và thành phố', 6),
                                                                ('FS1605', 'inactive', 'Family Suite – view hồ bơi và sân vườn', 6),
                                                                ('B111', 'available', '', 1),
                                                                ('B112', 'available', 'View công viên, không gian xanh', 1),
                                                                ('B113', 'available', '', 1),
                                                                ('B114', 'available', '', 1),
                                                                ('B115', 'available', 'Gần khu vực lễ tân, tiện check-in', 1),
                                                                ('B116', 'available', '', 1),
                                                                ('B117', 'available', '', 1),
                                                                ('B118', 'available', 'View đường phố nhộn nhịp', 1),
                                                                ('B119', 'available', '', 1),
                                                                ('B120', 'available', '', 1),
                                                                ('S611', 'available', 'View sông, ban công lớn', 2),
                                                                ('S612', 'available', '', 2),
                                                                ('S613', 'available', '', 2),
                                                                ('S614', 'available', 'Góc yên tĩnh, gần cầu thang bộ', 2),
                                                                ('S615', 'available', '', 2),
                                                                ('D913', 'available', 'View thành phố, tầng 9 góc Đông', 3),
                                                                ('D914', 'available', '', 3),
                                                                ('D915', 'available', 'View hồ bơi và vườn cảnh', 3),
                                                                ('D916', 'available', '', 3),
                                                                ('D917', 'available', 'Ban công riêng, view sông buổi sáng', 3),
                                                                ('R1205', 'available', 'View vịnh biển, ban công lớn', 4),
                                                                ('R1206', 'available', '', 4),
                                                                ('JS1505', 'available', 'View thành phố, phòng khách rộng rãi', 5),
                                                                ('FS1606', 'available', '', 6),
                                                                ('FS1607', 'available', 'View núi và hồ bơi, sân vườn riêng', 6),
                                                                ('B121', 'available', '', 1),
                                                                ('B122', 'available', '', 1),
                                                                ('B123', 'available', 'View vườn hoa nội khu', 1),
                                                                ('B124', 'available', '', 1),
                                                                ('B125', 'available', '', 1),
                                                                ('S616', 'available', '', 2),
                                                                ('S617', 'available', 'View sông và cầu, ban công rộng', 2),
                                                                ('S618', 'available', '', 2),
                                                                ('D911', 'available', '', 3),
                                                                ('D912', 'available', 'Tầng 9, view toàn cảnh khu trung tâm', 3),
                                                                ('R1207', 'available', '', 4),
                                                                ('R1208', 'available', 'Phòng góc, view 2 mặt thoáng', 4),
                                                                ('ST201', 'available', 'Phòng nhỏ gọn, view sân trong', 7),
                                                                ('ST202', 'available', '', 7),
                                                                ('ST203', 'available', 'Gần khu vực tiện ích chung', 7),
                                                                ('ST204', 'available', '', 7),
                                                                ('ST205', 'available', 'View đường phố, tiện đi lại', 7),
                                                                ('ST206', 'available', '', 7),
                                                                ('ST207', 'available', '', 7),
                                                                ('ST208', 'available', 'Phù hợp khách du lịch một mình', 7),
                                                                ('PS1701', 'available', 'Presidential – view toàn cảnh thành phố và sông, ban công riêng cực lớn', 8),
                                                                ('PS1702', 'available', 'Presidential – view biển từ phòng khách riêng biệt', 8),
                                                                ('PS1703', 'available', 'Presidential – tầng cao nhất khách sạn, toàn cảnh hoàng hôn', 8),
                                                                ('PS1704', 'available', 'Presidential – phòng góc 2 mặt thoáng, view núi và thành phố', 8);

-- Phụ phí theo view/vị trí: phòng cùng loại nhưng view khác nhau sẽ có giá khác nhau
UPDATE Rooms SET priceExtra = 100000 WHERE roomNumber IN
    ('B101','B102','B105','B118','B123','B201','B209','B301','B307','B410','B503','ST201','ST205');

UPDATE Rooms SET priceExtra = 200000 WHERE roomNumber IN
    ('S601','S602','S604','S609','S611','S617','D902','D904','D909','D913','D915','D917','D1101','R1208');

UPDATE Rooms SET priceExtra = 300000 WHERE roomNumber IN
    ('B401','B501','S606','S701','S707','S801','S803','D901','D906','D1001','D1003','D912',
     'R1201','R1203','R1205','R1301','R1303','R1401','JS1502','JS1504','JS1505','FS1602','FS1604','FS1607');

UPDATE Rooms SET priceExtra = 400000 WHERE roomNumber = 'PS1701';
UPDATE Rooms SET priceExtra = 600000 WHERE roomNumber = 'PS1702';
UPDATE Rooms SET priceExtra = 500000 WHERE roomNumber = 'PS1703';
UPDATE Rooms SET priceExtra = 450000 WHERE roomNumber = 'PS1704';

-- Lý do bảo trì / ngừng kinh doanh: lưu riêng vào statusNote để không đè mất ngoại cảnh trong description
UPDATE Rooms SET statusNote = 'Đang sửa điều hòa' WHERE roomNumber = 'B103';
UPDATE Rooms SET statusNote = 'Ngừng kinh doanh tạm thời' WHERE roomNumber = 'B310';
UPDATE Rooms SET statusNote = 'Sửa cửa sổ' WHERE roomNumber = 'S603';
UPDATE Rooms SET statusNote = 'Sửa hệ thống điện' WHERE roomNumber = 'S705';
UPDATE Rooms SET statusNote = 'Thay thảm sàn' WHERE roomNumber = 'D1105';
UPDATE Rooms SET statusNote = 'Đang cải tạo' WHERE roomNumber = 'FS1605';

-- ==========================================
-- TẠO INDEX TỐI ƯU HÓA TRUY VẤN (TỰ ĐỘNG THÊM VÀO SAU KHI INSERT)
-- ==========================================

-- 1. Bảng Users (Do dùng InheritanceType.JOINED nên username, email, phone nằm ở bảng User)
CREATE UNIQUE INDEX idx_user_username ON User (username);
CREATE UNIQUE INDEX idx_user_email ON User (email);
CREATE UNIQUE INDEX idx_user_phone ON User (phone);

-- 2. Bảng Booking
CREATE INDEX idx_booking_user_id ON Booking (userID);
CREATE INDEX idx_booking_status ON Booking (status);
CREATE INDEX idx_booking_date ON Booking (bookingDate);
CREATE INDEX idx_booking_status_expired ON Booking (status, expiredAt);
CREATE INDEX idx_booking_status_date ON Booking (status, bookingDate);

-- 3. Bảng BookingDetails (Quan trọng nhất cho query tìm phòng)
CREATE INDEX idx_bd_room_id ON BookingDetails (roomID);
CREATE INDEX idx_bd_booking_id ON BookingDetails (bookingID);
CREATE INDEX idx_bd_room_dates ON BookingDetails (roomID, checkinDate, checkoutDate);

-- 4. Bảng Rooms
CREATE INDEX idx_room_type_id ON Rooms (typeID);
CREATE INDEX idx_room_status ON Rooms (status);
CREATE INDEX idx_room_status_maintenance ON Rooms (status, maintenanceEnd);

-- 5. Bảng Payment
CREATE UNIQUE INDEX idx_payment_txn_code ON Payment (transactionCode);
CREATE INDEX idx_payment_booking_id ON Payment (bookingID);

-- 6. Bảng Review
CREATE INDEX idx_review_customer_id ON Review (customerID);

-- 7. Bảng MembershipTier
CREATE INDEX idx_tier_min_point ON MembershipTier (minPoint);

CREATE UNIQUE INDEX idx_review_booking_id ON Review (bookingID);
