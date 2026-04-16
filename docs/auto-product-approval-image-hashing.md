# Đề xuất duyệt sản phẩm tự động bằng Image Hashing

## Mục tiêu
Giảm tải cho admin bằng cách tự động phát hiện ảnh sản phẩm:
- Trùng lặp với ảnh đã có trong hệ thống.
- Có khả năng sao chép từ ảnh mạng/kho dữ liệu tham chiếu.

## Luồng đề xuất
1. Khi shop upload ảnh sản phẩm, backend tạo hash ảnh (pHash/dHash).
2. So khớp hash với:
   - Kho ảnh sản phẩm nội bộ.
   - Kho ảnh blacklist tham chiếu (ảnh mạng/ảnh vi phạm đã lưu).
3. Tính khoảng cách Hamming giữa hash mới và hash cũ.
4. Gán điểm rủi ro:
   - `distance <= 5`: gần như trùng, tự động chuyển trạng thái `REVIEW_REQUIRED`.
   - `distance 6-10`: cảnh báo mềm, vẫn cho lên chờ duyệt.
   - `distance > 10`: coi là khác biệt lớn.
5. Lưu log kiểm duyệt để admin có bằng chứng khi từ chối.

## Thiết kế kỹ thuật gợi ý
- Bảng `product_image_hashes`:
  - `id`, `product_image_id`, `hash_algo`, `hash_value`, `source` (`INTERNAL`, `REFERENCE`), `created_at`.
- Service `ImageHashService`:
  - `String computePHash(InputStream image)`
  - `int hammingDistance(String hashA, String hashB)`
- Ngưỡng hash cần tinh chỉnh bằng dữ liệu thật theo từng danh mục sản phẩm.

## Lưu ý vận hành
- Không nên tự động từ chối hoàn toàn chỉ bằng hash; nên đưa về hàng chờ duyệt.
- Kết hợp thêm OCR/logo detection để phát hiện watermark từ nguồn khác.
- Với ảnh QR thanh toán, cần whitelist vì đặc thù dễ giống nhau.
