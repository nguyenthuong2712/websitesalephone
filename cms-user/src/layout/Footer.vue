<script setup lang="ts">

</script>

<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-section">
        <h3>Về Chúng Tôi</h3>
        <p>Phone Store - Hệ thống bán lẻ điện thoại di động uy tín hàng đầu Việt Nam với hơn 100 cửa hàng trên toàn quốc.</p>
        <div class="social-links"><a href="#" class="social-btn" aria-label="Facebook">📘</a> <a href="#" class="social-btn" aria-label="Instagram">📷</a> <a href="#" class="social-btn" aria-label="Youtube">▶️</a> <a href="#" class="social-btn" aria-label="Zalo">💬</a>
        </div>
      </div>
      <div class="footer-section">
        <h3>Danh Mục</h3>
        <ul>
          <li><a href="#iphone">iPhone</a></li>
          <li><a href="#samsung">Samsung</a></li>
          <li><a href="#xiaomi">Xiaomi</a></li>
          <li><a href="#oppo">OPPO</a></li>
          <li><a href="#accessories">Phụ kiện</a></li>
        </ul>
      </div>
      <div class="footer-section">
        <h3>Hỗ Trợ</h3>
        <ul>
          <li><a href="#warranty">Chính sách bảo hành</a></li>
          <li><a href="#return">Đổi trả sản phẩm</a></li>
          <li><a href="#payment">Hướng dẫn thanh toán</a></li>
          <li><a href="#shipping">Vận chuyển &amp; Giao nhận</a></li>
          <li><a href="#faq">Câu hỏi thường gặp</a></li>
        </ul>
      </div>
      <div class="footer-section">
        <h3>Liên Hệ</h3>
        <ul>
          <li>📞 Hotline: 0123 456 789</li>
          <li>📧 Email: support@phonestore.com</li>
          <li>📍 Địa chỉ: 123 Đường ABC, Quận 1, TP.HCM</li>
          <li>⏰ Giờ làm việc: 8:00 - 22:00</li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      <p>© 2024 Phone Store. All rights reserved. Designed with ❤️</p>
    </div>
  </footer>
</template>

<style scoped>
/* Footer */
.footer {
  background: #1a1a2e;
  color: white;
  padding: 60px 30px 30px;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 50px;
  margin-bottom: 40px;
}
.footer-section h3 {
  font-size: 1.4em;
  margin-bottom: 20px;
  color: #fff;
}

.footer-section ul {
  list-style: none;
}

.footer-section ul li {
  margin-bottom: 12px;
}

.footer-section a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s ease;
}

.footer-section a:hover {
  color: white;
  padding-left: 5px;
}

.footer-bottom {
  text-align: center;
  padding-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
}

.social-links {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.social-btn {
  width: 45px;
  height: 45px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3em;
  transition: all 0.3s ease;
  text-decoration: none;
  color: white;
}

.social-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-5px);
}
</style>