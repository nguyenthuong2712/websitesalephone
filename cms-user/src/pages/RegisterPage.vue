<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { authService } from "../service/AuthService.ts";
import { RegisterRequest } from "../models/RegisterRequest.ts";
import { toast } from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import {
  ShieldCheck,
  User,
  Mail,
  Phone,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  Smartphone,
  Tag,
  Star,
  Sparkles
}
from '@lucide/vue';

// Initialize router
const router = useRouter();

// Input refs
const fullName = ref("");
const email = ref("");
const phone = ref("");
const password = ref("");
const confirmPassword = ref("");
const agreeToTerms = ref(true);

// Password visibility refs
const showPassword = ref(false);
const showConfirmPassword = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

// Error UI refs
const emailError = ref(false);
const phoneError = ref(false);
const confirmPassError = ref(false);

// Regex definitions
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegex = /^(0[0-9]{9,10})$/;

// Password strength indicator logic
const passwordStrength = computed(() => {
  const pass = password.value;
  if (!pass) return { score: 0, text: '', class: '' };
  if (pass.length < 6) return { score: 1, text: 'Quá ngắn', class: 'weak' };
  
  let score = 1;
  const hasLower = /[a-z]/.test(pass);
  const hasUpper = /[A-Z]/.test(pass);
  const hasDigit = /[0-9]/.test(pass);
  const hasSpecial = /[^A-Za-z0-9]/.test(pass);
  
  if (hasLower && hasDigit) score = 2;
  if (hasLower && hasUpper && hasDigit) score = 3;
  if (hasLower && hasUpper && hasDigit && hasSpecial) score = 4;
  
  if (score === 1) return { score: 1, text: 'Yếu', class: 'weak' };
  if (score === 2) return { score: 2, text: 'Trung bình', class: 'medium' };
  if (score === 3) return { score: 3, text: 'Mạnh', class: 'strong' };
  return { score: 4, text: 'Rất mạnh', class: 'very-strong' };
});

// Submit handler
const submitForm = async (e: Event) => {
  e.preventDefault();

  // Reset flags
  emailError.value = false;
  phoneError.value = false;
  confirmPassError.value = false;

  // Validations
  if (!emailRegex.test(email.value)) {
    emailError.value = true;
    toast.error("Email không hợp lệ.");
    return;
  }

  if (!phoneRegex.test(phone.value)) {
    phoneError.value = true;
    toast.error("Số điện thoại không hợp lệ.");
    return;
  }

  if (password.value !== confirmPassword.value) {
    confirmPassError.value = true;
    toast.error("Mật khẩu xác nhận không khớp.");
    return;
  }

  if (!agreeToTerms.value) {
    toast.error("Bạn phải đồng ý với điều khoản và chính sách.");
    return;
  }

  const req = new RegisterRequest(
      fullName.value,
      phone.value,
      password.value,
      email.value
  );

  try {
    const res = await authService.register(req);

    if (res.data?.code === 0) {
      toast.success("Đăng ký thành công! 🎉");

      // Clear input
      fullName.value = "";
      email.value = "";
      phone.value = "";
      password.value = "";
      confirmPassword.value = "";

      setTimeout(() => {
        router.push("/login");
      }, 1200);

    } else {
      toast.error(res.data?.message || "Có lỗi xảy ra.");
    }
  } catch (err) {
    toast.error("Hệ thống bận, thử lại sau.");
  }
};
</script>

<template>
  <main class="signup-wrapper">
    <div class="signup-split-container">
      
      <!-- Left Column: Dark Promo Side -->
      <section class="promo-panel">
        <header class="brand-header">
          <div class="brand-logo">
            <Smartphone :size="20" class="brand-logo-icon" />
          </div>
          <span class="brand-name">Phone Store</span>
        </header>

        <div class="promo-content">
          <div class="promo-tag">
            <Sparkles :size="12" class="tag-icon" />
            <span>Công nghệ đỉnh cao – Trải nghiệm xứng tầm</span>
          </div>
          
          <h1 class="promo-title">
            Khám phá thế giới <br />
            <span class="gradient-text">smartphone</span> <br />
            đỉnh cao
          </h1>
          
          <p class="promo-desc">
            Đăng ký ngay để nhận ưu đãi độc quyền, theo dõi đơn hàng và trải nghiệm mua sắm tuyệt vời cùng Phone Store.
          </p>

          <div class="features-list">
            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <Tag :size="18" class="feature-icon" />
              </div>
              <div class="feature-text">
                <h3 class="feature-title">Ưu đãi độc quyền</h3>
                <p class="feature-subtitle">Giảm giá sốc dành riêng cho thành viên</p>
              </div>
            </div>

            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <ShieldCheck :size="18" class="feature-icon" />
              </div>
              <div class="feature-text">
                <h3 class="feature-title">Bảo hành chính hãng</h3>
                <p class="feature-subtitle">An tâm tuyệt đối với sản phẩm chính hãng</p>
              </div>
            </div>

            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <Truck :size="18" class="feature-icon" />
              </div>
              <div class="feature-text">
                <h3 class="feature-title">Giao hàng nhanh chóng</h3>
                <p class="feature-subtitle">Giao tận nơi – Kiểm tra trước khi thanh toán</p>
              </div>
            </div>
          </div>

          <!-- Bottom Customer Review Badge -->
          <div class="reviews-badge">
            <div class="avatar-group">
              <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=64&h=64&q=80" alt="Avatar 1" class="avatar-img" />
              <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=64&h=64&q=80" alt="Avatar 2" class="avatar-img" />
              <img src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=64&h=64&q=80" alt="Avatar 3" class="avatar-img" />
              <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=64&h=64&q=80" alt="Avatar 4" class="avatar-img" />
            </div>
            <div class="reviews-info">
              <div class="stars">
                <Star v-for="i in 5" :key="i" :size="12" class="star-icon" />
              </div>
              <span class="reviews-text">10.000+ khách hàng đã tin chọn</span>
            </div>
          </div>
        </div>

       
      </section>

      <!-- Right Column: Register Form Side -->
      <section class="register-card-panel">
        <!-- Floating Security Badge above card -->
        <div class="security-badge-header">
          <ShieldCheck :size="14" class="security-icon" />
          <span>Mua sắm an toàn & bảo mật</span>
        </div>

        <div class="register-card">
          <!-- Card Header Avatar Badge -->
          <div class="card-avatar-badge-container">
            <div class="card-avatar-badge">
              <User :size="24" class="avatar-badge-icon" />
            </div>
          </div>

          <h2 class="card-title">Tạo <span class="gradient-text-alt">tài khoản</span></h2>
          <p class="card-subtitle">Điền thông tin để bắt đầu hành trình cùng chúng tôi</p>

          <form class="signup-form" @submit="submitForm">
            
            <div class="form-group">
              <label for="fullName" class="form-label">Họ và tên</label>
              <div class="input-wrapper">
                <User :size="18" class="input-icon" />
                <input 
                  type="text" 
                  id="fullName" 
                  class="form-input"
                  placeholder="Nhập họ và tên của bạn"
                  v-model="fullName" 
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label for="email" class="form-label">Email</label>
              <div class="input-wrapper">
                <Mail :size="18" class="input-icon" />
                <input 
                  type="email" 
                  id="email" 
                  class="form-input"
                  :class="{ error: emailError }"
                  placeholder="Nhập email của bạn"
                  v-model="email" 
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label for="phone" class="form-label">Số điện thoại</label>
              <div class="input-wrapper">
                <Phone :size="18" class="input-icon" />
                <input 
                  type="tel" 
                  id="phone" 
                  class="form-input"
                  :class="{ error: phoneError }"
                  placeholder="Nhập số điện thoại"
                  v-model="phone" 
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label for="password" class="form-label">Mật khẩu</label>
              <div class="input-wrapper">
                <Lock :size="18" class="input-icon" />
                <input 
                  :type="showPassword ? 'text' : 'password'"
                  id="password"
                  class="form-input"
                  placeholder="Tối thiểu 6 ký tự"
                  v-model="password"
                  required
                />
                <button 
                  type="button"
                  class="password-toggle"
                  @click="togglePassword"
                  aria-label="Toggle password visibility"
                >
                  <EyeOff v-if="showPassword" :size="18" />
                  <Eye v-else :size="18" />
                </button>
              </div>
              
              <!-- Password Strength Bar Indicator -->
              <div class="strength-indicator-wrapper" v-if="password">
                <div class="strength-meta">
                  <span class="strength-label">Độ mạnh mật khẩu:</span>
                  <span :class="['strength-text-badge', passwordStrength.class]">{{ passwordStrength.text }}</span>
                </div>
                <div class="strength-bars">
                  <div class="strength-bar" :class="{ active: passwordStrength.score >= 1, 'weak-bg': passwordStrength.score === 1, 'medium-bg': passwordStrength.score === 2, 'strong-bg': passwordStrength.score === 3, 'very-strong-bg': passwordStrength.score === 4 }"></div>
                  <div class="strength-bar" :class="{ active: passwordStrength.score >= 2, 'medium-bg': passwordStrength.score === 2, 'strong-bg': passwordStrength.score === 3, 'very-strong-bg': passwordStrength.score === 4 }"></div>
                  <div class="strength-bar" :class="{ active: passwordStrength.score >= 3, 'strong-bg': passwordStrength.score === 3, 'very-strong-bg': passwordStrength.score === 4 }"></div>
                  <div class="strength-bar" :class="{ active: passwordStrength.score >= 4, 'very-strong-bg': passwordStrength.score === 4 }"></div>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
              <div class="input-wrapper">
                <Lock :size="18" class="input-icon" />
                <input 
                  :type="showConfirmPassword ? 'text' : 'password'"
                  id="confirmPassword"
                  class="form-input"
                  :class="{ error: confirmPassError }"
                  placeholder="Nhập lại mật khẩu"
                  v-model="confirmPassword"
                  required
                />
                <button 
                  type="button"
                  class="password-toggle"
                  @click="toggleConfirmPassword"
                  aria-label="Toggle password visibility"
                >
                  <EyeOff v-if="showConfirmPassword" :size="18" />
                  <Eye v-else :size="18" />
                </button>
              </div>
            </div>

            <!-- Agree to terms check -->
            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" id="agree" v-model="agreeToTerms">
                <span class="checkmark"></span>
                <span class="remember-text">
                  Tôi đồng ý với <a href="#" class="inline-link">điều khoản</a> và <a href="#" class="inline-link">chính sách</a>
                </span>
              </label>
            </div>

            <button type="submit" class="login-btn">
              <span>Tạo tài khoản</span>
              <ArrowRight :size="18" class="btn-arrow-icon" />
            </button>
          </form>

          <div class="social-divider">
            <span class="divider-text">hoặc đăng ký với</span>
          </div>

          <!-- Social Logins row -->
          <div class="social-login-container">
            <button class="social-btn" aria-label="Đăng ký bằng Google">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22c-.22-.67-.35-1.37-.35-2.09z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
              </svg>
              <span class="social-text">Google</span>
            </button>
            <button class="social-btn" aria-label="Đăng ký bằng Facebook">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" fill="#1877F2"/>
              </svg>
              <span class="social-text">Facebook</span>
            </button>
            <button class="social-btn" aria-label="Đăng ký bằng Apple">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 4.17c.66-.81 1.11-1.93.99-3.06-1 .04-2.22.67-2.94 1.52-.63.73-1.18 1.87-1.03 2.97 1.12.09 2.27-.56 2.98-1.43z" fill="#000000"/>
              </svg>
              <span class="social-text">Apple</span>
            </button>
          </div>

          <div class="signup-footer">
            Đã có tài khoản? <router-link to="login" class="signup-link">Đăng nhập</router-link>
          </div>

        </div>
      </section>

    </div>
  </main>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.signup-wrapper {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: stretch;
  background-color: #fcfbfe;
  position: relative;
  overflow-x: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.signup-split-container {
  width: 100%;
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  z-index: 1;
}

/* Left Column: Promo Side */
.promo-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 60px 80px;
  position: relative;
  background: linear-gradient(135deg, #070614 0%, #15092a 50%, #080211 100%);
  overflow: hidden;
}

/* Glowing Neon portal circle in the left promo bg */
.neon-portal-glow {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  border: 1px solid rgba(124, 58, 237, 0.15);
  box-shadow: 0 0 100px rgba(124, 58, 237, 0.1), inset 0 0 100px rgba(236, 72, 153, 0.05);
  right: -100px;
  bottom: -50px;
  z-index: 0;
  pointer-events: none;
  animation: pulse 8s ease-in-out infinite alternate;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    opacity: 0.7;
  }
  100% {
    transform: scale(1.05);
    opacity: 1;
  }
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 50px;
  z-index: 2;
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 10px rgba(124, 58, 237, 0.25);
}

.brand-logo-icon {
  stroke-width: 2.5px;
}

.brand-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #ffffff;
}

.promo-content {
  display: flex;
  flex-direction: column;
  z-index: 2;
  margin-top: 20px;
}

.promo-tag {
  background: rgba(124, 58, 237, 0.15);
  border: 1px solid rgba(124, 58, 237, 0.25);
  color: #c084fc;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  width: fit-content;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 6px;
  backdrop-filter: blur(10px);
}

.tag-icon {
  color: #da458f;
}

.promo-title {
  font-size: 3.25rem;
  font-weight: 800;
  line-height: 1.2;
  color: #ffffff;
  margin-bottom: 24px;
  letter-spacing: -0.01em;
}

.gradient-text {
  background: linear-gradient(135deg, #9b51e0 0%, #ec4899 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.promo-desc {
  font-size: 0.95rem;
  color: #9ca3af;
  margin-bottom: 40px;
  max-width: 460px;
  line-height: 1.7;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 45px;
  max-width: 460px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.feature-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.feature-item:hover .feature-icon-wrapper {
  background: rgba(124, 58, 237, 0.2);
  border-color: rgba(124, 58, 237, 0.4);
  color: #ffffff;
  box-shadow: 0 0 15px rgba(124, 58, 237, 0.3);
}

.feature-icon {
  stroke-width: 2px;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: #f3f4f6;
}

.feature-subtitle {
  font-size: 0.8rem;
  color: #9ca3af;
  line-height: 1.3;
}

/* Bottom Customer Reviews Badge */
.reviews-badge {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  padding: 12px 20px;
  border-radius: 20px;
  width: fit-content;
  backdrop-filter: blur(10px);
}

.avatar-group {
  display: flex;
  align-items: center;
}

.avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #0f0b20;
  margin-left: -8px;
}

.avatar-img:first-child {
  margin-left: 0;
}

.reviews-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stars {
  display: flex;
  gap: 2px;
}

.star-icon {
  fill: #f59e0b;
  color: #f59e0b;
}

.reviews-text {
  font-size: 0.75rem;
  color: #d1d5db;
  font-weight: 500;
}

.promo-illustration-container {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
  position: absolute;
  right: -40px;
  bottom: 0;
  height: 60%;
  width: 50%;
  z-index: 1;
}

.promo-image {
  height: 105%;
  max-height: 520px;
  object-fit: contain;
  width: auto;
  filter: drop-shadow(0 25px 45px rgba(0, 0, 0, 0.6));
}

/* Right Column: Register Card Panel */
.register-card-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
}

.security-badge-header {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f0fdf4;
  border: 1px solid #dcfce7;
  color: #166534;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 24px;
  box-shadow: 0 4px 10px rgba(22, 101, 52, 0.03);
}

.security-icon {
  stroke-width: 2.2px;
}

.register-card {
  width: 100%;
  max-width: 480px;
  background: #ffffff;
  border-radius: 28px;
  padding: 40px;
  box-shadow: 0 20px 40px -15px rgba(124, 58, 237, 0.05);
  border: 1px solid rgba(243, 244, 246, 0.8);
}

.card-avatar-badge-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.card-avatar-badge {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.1);
}

.avatar-badge-icon {
  stroke-width: 2.2px;
}

.card-title {
  font-size: 1.75rem;
  font-weight: 800;
  text-align: center;
  color: #1f2937;
  margin-bottom: 6px;
  letter-spacing: -0.02em;
}

.gradient-text-alt {
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-subtitle {
  font-size: 0.85rem;
  text-align: center;
  color: #6b7280;
  margin-bottom: 30px;
}

.signup-form {
  width: 100%;
}

.form-group {
  margin-bottom: 18px;
}

.form-label {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: #9ca3af;
  pointer-events: none;
  stroke-width: 2.2px;
}

.form-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  border: 1.5px solid #f3f4f6;
  border-radius: 12px;
  font-size: 0.9rem;
  color: #1f2937;
  background: #fafafa;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #7c3aed;
  box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.08);
  background: #ffffff;
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-input.error {
  border-color: #ef4444;
  background-color: #fef2f2;
}

.form-input.error:focus {
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.08);
}

.password-toggle {
  position: absolute;
  right: 16px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #7c3aed;
}

/* Password Strength Meter Styling */
.strength-indicator-wrapper {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.strength-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.strength-label {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.strength-text-badge {
  font-size: 0.75rem;
  font-weight: 700;
}

.strength-text-badge.weak { color: #ef4444; }
.strength-text-badge.medium { color: #f97316; }
.strength-text-badge.strong { color: #10b981; }
.strength-text-badge.very-strong { color: #059669; }

.strength-bars {
  display: flex;
  gap: 6px;
  height: 4px;
}

.strength-bar {
  flex: 1;
  background: #e5e7eb;
  border-radius: 999px;
  transition: background-color 0.3s ease;
}

.strength-bar.active.weak-bg { background-color: #ef4444; }
.strength-bar.active.medium-bg { background-color: #f97316; }
.strength-bar.active.strong-bg { background-color: #10b981; }
.strength-bar.active.very-strong-bg { background-color: #059669; }

.form-options {
  margin: 22px 0;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  color: #4b5563;
  font-size: 0.82rem;
  line-height: 1.4;
}

.remember-me input {
  display: none;
}

.remember-me .checkmark {
  width: 18px;
  height: 18px;
  border: 1.5px solid #d1d5db;
  border-radius: 6px;
  display: inline-block;
  position: relative;
  transition: all 0.2s ease;
  background: #fff;
  flex-shrink: 0;
}

.remember-me input:checked ~ .checkmark {
  border-color: #7c3aed;
  background: #7c3aed;
}

.remember-me input:checked ~ .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.remember-text {
  font-weight: 500;
}

.inline-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s ease;
}

.inline-link:hover {
  color: #7c3aed;
}

.login-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 10px 20px -8px rgba(124, 58, 237, 0.4);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px -6px rgba(124, 58, 237, 0.5);
}

.login-btn:active {
  transform: translateY(1px);
}

.btn-arrow-icon {
  transition: transform 0.2s ease;
}

.login-btn:hover .btn-arrow-icon {
  transform: translateX(4px);
}

/* Social Divider */
.social-divider {
  display: flex;
  align-items: center;
  margin: 22px 0;
}

.social-divider::before,
.social-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #f3f4f6;
}

.divider-text {
  padding: 0 14px;
  font-size: 0.78rem;
  color: #9ca3af;
}

/* Social Login row */
.social-login-container {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
}

.social-btn {
  flex: 1;
  padding: 10px 12px;
  border: 1.5px solid #f3f4f6;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.social-btn:hover {
  border-color: #e5e7eb;
  background: #fafafa;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
}

.social-text {
  font-size: 0.8rem;
  font-weight: 600;
  color: #374151;
}

.social-btn svg {
  display: block;
}

/* Footer register link */
.signup-footer {
  text-align: center;
  font-size: 0.85rem;
  color: #6b7280;
  font-weight: 500;
}

.signup-link {
  color: #7c3aed;
  text-decoration: none;
  font-weight: 700;
  transition: color 0.2s ease;
  margin-left: 4px;
}

.signup-link:hover {
  color: #da458f;
}

/* Responsive Styles */
@media (max-width: 1024px) {
  .signup-split-container {
    grid-template-columns: 1fr;
    max-width: 540px;
    margin: 40px auto;
  }

  .promo-panel {
    display: none; /* Hide promo sidebar on mobile/tablet */
  }

  .register-card-panel {
    padding: 0 20px;
    width: 100%;
  }

  .register-card {
    padding: 40px 28px;
    border-radius: 28px;
  }
}

@media (max-width: 480px) {
  .signup-split-container {
    margin: 0;
  }
  
  .register-card {
    padding: 32px 20px;
    border-radius: 24px;
    box-shadow: none;
    border: none;
    background: transparent;
  }
  
  .signup-wrapper {
    background-color: #ffffff;
  }
  
  .security-badge-header {
    margin-top: 20px;
  }
}
</style>