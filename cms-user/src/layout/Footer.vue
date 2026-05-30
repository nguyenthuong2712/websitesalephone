<script setup lang="ts">
import { ArrowUp, PhoneCall, MessageCircle } from '@lucide/vue';

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
};
</script>

<template>
  <footer class="footer-alt">
    <div class="footer-container">
      
      <!-- Grid Columns -->
      <div class="footer-grid">
        
        <!-- Column 1: Brand and Hotlines -->
        <div class="footer-col brand-col">
          <div class="footer-logo-block">
            <span class="logo-text">PHONE STORE</span>
            <div class="reseller-badge">
              <svg viewBox="0 0 170 170" width="16" height="16" fill="currentColor" class="apple-logo-svg"><path d="M150.37 130.25c-2.45 5.66-5.35 10.87-8.71 15.66-4.58 6.53-8.33 11.05-11.22 13.56-4.48 4.12-9.28 6.23-14.42 6.35-3.69 0-8.14-1.05-13.32-3.18-5.19-2.12-9.97-3.17-14.34-3.17-4.58 0-9.49 1.05-14.75 3.17-5.26 2.13-9.5 3.24-12.74 3.35-4.34.13-9.13-1.85-14.36-5.94-2.92-2.39-6.67-6.86-11.25-13.43-5.06-7.38-9.45-16.14-13.17-26.27-3.73-10.13-5.62-20.01-5.62-29.63 0-14.86 3.96-26.83 11.87-35.88 7.91-9.05 17.65-13.58 29.21-13.58 5.62 0 11.5 1.48 17.66 4.43 6.15 2.95 10.66 4.43 13.5 4.43 2.11 0 6.64-1.57 13.62-4.71 6.97-3.13 12.87-4.59 17.69-4.38 12.87.62 22.84 5.27 29.93 13.97-10.96 6.66-16.34 15.66-16.14 26.97.2 9.05 3.53 16.69 9.99 22.89 6.46 6.21 14.15 9.7 23.05 10.49-2.4 6.78-5.06 13.25-7.97 19.38zm-30.82-120.73c0 7.82-2.73 14.8-8.19 20.93-5.46 6.13-12.01 9.77-19.64 10.92.11-1.67.22-3.44.22-5.32 0-7.3-2.88-14.49-8.63-20.77-4.88-5.32-11.08-8.87-18.62-9.91 0-1.25.1-2.61.1-4.07 0 7.72 2.89 14.73 8.66 21 5.77 6.26 12.28 9.6 19.51 10.02z"/></svg>
              <div class="reseller-text-block">
                <span class="reseller-title">Authorized</span>
                <span class="reseller-sub">Reseller</span>
              </div>
            </div>
          </div>
          
          <p class="brand-desc">
            Năm 2020, Phone Store trở thành đại lý ủy quyền của Apple. Chúng tôi phát triển chuỗi cửa hàng tiêu chuẩn và Apple Mono Store nhằm mang đến trải nghiệm tốt nhất về sản phẩm và dịch vụ của Apple cho người dùng Việt Nam.
          </p>

          <!-- Hotlines list -->
          <div class="hotlines-list">
            <div class="hotline-item">
              <span class="hotline-label">Hotline mua hàng:</span>
              <a href="tel:19006626" class="hotline-number">1900.6626</a>
            </div>
            <div class="hotline-item">
              <span class="hotline-label">Bán hàng doanh nghiệp:</span>
              <a href="tel:0822688688" class="hotline-number">0822.688.688</a>
            </div>
            <div class="hotline-item">
              <span class="hotline-label">Hotline SDCARE:</span>
              <a href="tel:19008036" class="hotline-number">1900.8036</a>
            </div>
            <div class="hotline-item">
              <span class="hotline-label">Hotline Hesman Care:</span>
              <a href="tel:0986988750" class="hotline-number">0986.988.750</a>
            </div>
          </div>

          <!-- Social links -->
          <div class="social-links-row">
            <a href="#" class="social-circle" aria-label="Facebook">
              <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24"><path d="M9 8H7v3h2v9h3v-9h3.6l.4-3h-4V6.5C16 5.67 16.5 5 17.5 5H19V2h-3c-3 0-5 2-5 5v1z"/></svg>
            </a>
            <a href="#" class="social-circle" aria-label="TikTok">
              <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24"><path d="M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.02 1.59 4.23.86 1.08 2.05 1.81 3.37 2.11v3.72c-1.21-.17-2.35-.74-3.25-1.57-.45-.42-.83-.9-1.15-1.42v7.71c.08 1.48-.32 2.97-1.14 4.21-.83 1.25-2.07 2.17-3.48 2.58-1.53.47-3.21.36-4.67-.32-1.46-.68-2.64-1.92-3.28-3.45-.69-1.56-.7-3.34-.05-4.91a7.48 7.48 0 0 1 3.86-4.05c1.1-.53 2.33-.74 3.54-.6v3.74c-.87-.04-1.74.22-2.45.74-.75.54-1.25 1.39-1.39 2.31-.15.93.13 1.89.77 2.58.64.69 1.55 1.05 2.49 1.01.99.01 1.91-.52 2.4-1.38.38-.66.52-1.43.49-2.19V0z"/></svg>
            </a>
            <a href="#" class="social-circle text-circle" aria-label="Zalo">
              <span class="zalo-text">Zalo</span>
            </a>
            <a href="#" class="social-circle" aria-label="YouTube">
              <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24"><path d="M23.498 6.163a3.003 3.003 0 0 0-2.11-2.11C19.518 3.545 12 3.545 12 3.545s-7.518 0-9.388.508a3.003 3.003 0 0 0-2.11 2.11C0 8.033 0 12 0 12s0 3.967.502 5.837a3.003 3.003 0 0 0 2.11 2.11c1.87.508 9.388.508 9.388.508s7.518 0 9.388-.508a3.003 3.003 0 0 0 2.11-2.11C24 15.967 24 12 24 12s0-3.967-.502-5.837zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/></svg>
            </a>
          </div>
        </div>

        <!-- Column 2: Thông tin -->
        <div class="footer-col">
          <h3 class="col-title">Thông tin</h3>
          <ul class="col-links">
            <li><a href="#">Newsfeed</a></li>
            <li><a href="#">Giới thiệu</a></li>
            <li><a href="#">Check IMEI</a></li>
            <li><a href="#">Phương thức thanh toán</a></li>
            <li><a href="#">Thuê điểm bán lẻ</a></li>
            <li><a href="#">Bảo hành và sửa chữa</a></li>
            <li><a href="#">Tuyển dụng</a></li>
            <li><a href="#">Đánh giá chất lượng, khiếu nại</a></li>
            <li><a href="#">Tra cứu hoá đơn điện tử</a></li>
          </ul>
        </div>

        <!-- Column 3: Chính sách -->
        <div class="footer-col">
          <h3 class="col-title">Chính sách</h3>
          <ul class="col-links">
            <li><a href="#">Thu cũ đổi mới</a></li>
            <li><a href="#">Giao hàng</a></li>
            <li><a href="#">Giao hàng (ZaloPay)</a></li>
            <li><a href="#">Huỷ giao dịch</a></li>
            <li><a href="#">Đổi trả</a></li>
            <li><a href="#">Bảo hành</a></li>
            <li><a href="#">Dịch vụ</a></li>
            <li><a href="#">Giải quyết khiếu nại</a></li>
            <li><a href="#">Bảo mật thông tin</a></li>
            <li><a href="#">Hướng dẫn thanh toán qua VNPAY</a></li>
          </ul>
        </div>

        <!-- Column 4: Địa chỉ & Liên hệ -->
        <div class="footer-col address-col">
          <h3 class="col-title">Địa chỉ & Liên hệ</h3>
          <ul class="col-links">
            <li><a href="#">Tài khoản của tôi</a></li>
            <li><a href="#">Đơn đặt hàng</a></li>
            <li><a href="#">Tìm Store trên Google Map</a></li>
            <li><a href="#">Hệ thống cửa hàng</a></li>
          </ul>
          
          <div class="address-contact-details">
            <p class="contact-purchase">Mua hàng: <a href="tel:19006626">1900.6626</a></p>
            <p class="contact-branch">Nhánh 1: khu vực Hà Nội và các tỉnh phía Bắc</p>
            <p class="contact-branch">Nhánh 2: khu vực Hồ Chí Minh và các tỉnh phía Nam</p>
            <p class="contact-branch">Nhánh 3: Khiếu nại và góp ý</p>
            <p class="contact-business">Bán hàng doanh nghiệp</p>
          </div>
        </div>

      </div>

      <!-- Bottom Bar -->
      <div class="footer-bottom-bar">
        <div class="bottom-left-info">
          <p>© 2016 Công ty Cổ phần PHONE STORE Việt Nam GPDKKD: 0107465657 do Sở KH & ĐT TP. Hà Nội cấp ngày 08/06/2016.</p>
          <p>Địa chỉ: Số 76 Thái Hà, phường Trung Liệt, quận Đống Đa, thành phố Hà Nội, Việt Nam</p>
          <p>Đại diện pháp luật: PHẠM MẠNH HÒA | ĐT: 0247.305.9999 | Email: lienhe@phonestore.com.vn</p>
        </div>
        <div class="bottom-right-badge">
          <!-- BCT Badge -->
          <a href="#" class="bct-badge-link" aria-label="Bộ Công Thương">
            <svg width="150" height="50" viewBox="0 0 150 50" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="150" height="50" rx="8" fill="#0284c7"/>
              <circle cx="28" cy="25" r="12" fill="#ffffff"/>
              <path d="M24 25L27 28L33 21" stroke="#0284c7" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              <text x="48" y="22" fill="#ffffff" font-size="10" font-weight="800" font-family="-apple-system, sans-serif">ĐÃ THÔNG BÁO</text>
              <text x="48" y="34" fill="#ffffff" font-size="9" font-weight="700" font-family="-apple-system, sans-serif" letter-spacing="0.05em">BỘ CÔNG THƯƠNG</text>
            </svg>
          </a>
        </div>
      </div>

    </div>

    <!-- Floating Action Toolbar (Bottom Right) -->
    <div class="floating-toolbar">
      <button class="float-btn scroll-top-btn" @click="scrollToTop" aria-label="Lên đầu trang">
        <ArrowUp :size="20" />
      </button>
      <a href="tel:19006626" class="float-btn hotline-btn" aria-label="Gọi hotline">
        <PhoneCall :size="18" class="hotline-icon" />
        <span class="hotline-hover-text">HOTLINE</span>
      </a>
      <button class="float-btn chat-btn" aria-label="Chat hỗ trợ">
        <MessageCircle :size="20" />
      </button>
    </div>
  </footer>
</template>

<style scoped>
/* Scoped Dark Footer Redesign */
.footer-alt {
  width: 100%;
  background: #191919;
  color: #a3a3a3;
  padding: 80px 0 40px 0;
  position: relative;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  border-top: 1px solid #262626;
}

.footer-container {
  max-width: 1400px;
  width: calc(100% - 60px);
  margin: 0 auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr 0.85fr 1.15fr;
  gap: 40px;
  margin-bottom: 60px;
}

.footer-col {
  display: flex;
  flex-direction: column;
}

/* Brand col */
.footer-logo-block {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.05em;
}

.reseller-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  border-left: 1px solid #404040;
  padding-left: 12px;
  color: #ffffff;
}

.apple-logo-svg {
  flex-shrink: 0;
}

.reseller-text-block {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.reseller-title {
  font-size: 0.58rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #ffffff;
}

.reseller-sub {
  font-size: 0.55rem;
  font-weight: 400;
  color: #a3a3a3;
}

.brand-desc {
  font-size: 0.82rem;
  line-height: 1.6;
  color: #a3a3a3;
  margin-bottom: 28px;
}

/* Hotlines list */
.hotlines-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 30px;
}

.hotline-item {
  display: flex;
  gap: 8px;
  font-size: 0.82rem;
  align-items: baseline;
}

.hotline-label {
  font-weight: 600;
  color: #ffffff;
}

.hotline-number {
  color: #3b82f6;
  font-weight: 700;
  text-decoration: none;
  font-size: 0.88rem;
  transition: color 0.2s ease;
}

.hotline-number:hover {
  color: #60a5fa;
  text-decoration: underline;
}

/* Social links row */
.social-links-row {
  display: flex;
  gap: 10px;
}

.social-circle {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid #404040;
  background: transparent;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  text-decoration: none;
}

.social-circle:hover {
  background: #ffffff;
  color: #191919;
  border-color: #ffffff;
  transform: translateY(-2px);
}

.text-circle {
  font-size: 0.65rem;
  font-weight: 800;
}

.zalo-text {
  line-height: 1;
}

/* Middle links columns */
.col-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 24px;
}

.col-links {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.col-links a {
  color: #a3a3a3;
  text-decoration: none;
  font-size: 0.82rem;
  font-weight: 500;
  transition: all 0.2s ease;
  width: fit-content;
}

.col-links a:hover {
  color: #ffffff;
  padding-left: 4px;
}

/* Address details */
.address-contact-details {
  margin-top: 24px;
  border-top: 1px solid #262626;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.8rem;
  line-height: 1.5;
  color: #737373;
}

.contact-purchase {
  font-size: 0.85rem;
  font-weight: 700;
  color: #ffffff;
}

.contact-purchase a {
  color: #3b82f6;
  text-decoration: none;
}

.contact-branch {
  color: #737373;
}

.contact-business {
  font-weight: 600;
  color: #ffffff;
  margin-top: 4px;
}

/* Bottom Bar */
.footer-bottom-bar {
  border-top: 1px solid #262626;
  padding-top: 36px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 40px;
}

.bottom-left-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.72rem;
  line-height: 1.6;
  color: #737373;
  max-width: 900px;
}

.bottom-right-badge {
  flex-shrink: 0;
}

.bct-badge-link {
  display: block;
  transition: transform 0.2s ease;
}

.bct-badge-link:hover {
  transform: translateY(-2px);
}

/* Floating Action Toolbar (Bottom Right) */
.floating-toolbar {
  position: fixed;
  right: 24px;
  bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
}

.float-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(40, 40, 40, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  position: relative;
}

.float-btn:hover {
  background: #ffffff;
  color: #191919;
  transform: scale(1.08);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

.hotline-btn {
  background: rgba(40, 40, 40, 0.85);
  overflow: hidden;
  width: 44px;
  transition: width 0.3s ease, background-color 0.3s ease;
  justify-content: flex-start;
  padding-left: 12px;
}

.hotline-btn:hover {
  width: 120px;
  background: #ffffff;
  border-radius: 22px;
}

.hotline-hover-text {
  font-size: 0.72rem;
  font-weight: 800;
  margin-left: 10px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s ease 0.1s;
}

.hotline-btn:hover .hotline-hover-text {
  opacity: 1;
}

/* Responsiveness */
@media (max-width: 1024px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 40px;
  }
  .footer-bottom-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
}

@media (max-width: 640px) {
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  .footer-logo-block {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .reseller-badge {
    border-left: none;
    padding-left: 0;
  }
  .footer-alt {
    padding: 60px 0 30px 0;
  }
}
</style>