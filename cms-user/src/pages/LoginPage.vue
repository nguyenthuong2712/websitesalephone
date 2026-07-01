<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthUser } from '../models/AuthUser.ts'
import { authService } from "../service/AuthService.ts"
import { toast } from "vue3-toastify"
import "vue3-toastify/dist/index.css"
import {
  ShieldCheck,
  Truck,
  Headphones,
  User,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  Smartphone
} from '@lucide/vue'

const router = useRouter()

const email = ref('')
const password = ref('')
const remember = ref(false)
const showPassword = ref(false)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const handleLogin = async () => {
  if (!email.value || !password.value) {
    toast.error("Bạn chưa nhập đủ thông tin.")
    return
  }

  try {
    const request = new AuthUser(email.value, password.value)
    const res = await authService.login(request)
    if (res.data.code === 0) {
      toast.success("Đăng nhập thành công! 🎉")

      const token = res.data?.data?.accessToken ?? ''
      const role = res.data?.data?.role ?? ''
      authService.saveToken(token)
      authService.saveRole(role)

      setTimeout(() => {
        if (role === 'CUSTOMER') {
          router.push('/customer/home')
        } else if (role === 'PARTNER') {
          router.push('/admin/product')
        } else {
          router.push('/admin/dashboard')
        }
      }, 800)
    } else {
      toast.error(res.data.message)
    }

  } catch (err: any) {
    toast.error(err?.response?.data?.message || "Đăng nhập thất bại.")
  }
}
</script>

<template>
  <main class="login-wrapper">
    <!-- Ambient floating glow background elements -->
    <div class="ambient-glow bg-glow-left"></div>
    <div class="ambient-glow bg-glow-right"></div>

    <div class="login-split-container">
      
      <!-- Left Column: Promo Panel -->
      <section class="promo-panel">
        <div class="floating-circle"></div>
        
        <header class="brand-header">
          <div class="brand-logo">
            <Smartphone :size="20" class="brand-logo-icon" />
          </div>
          <span class="brand-name">Phone Store</span>
        </header>

        <div class="promo-content">
          <div class="promo-tag">Cửa hàng điện thoại uy tín</div>
          <h1 class="promo-title">
            Công nghệ <br />
            <span class="gradient-text">trong tầm tay</span>
          </h1>
          <p class="promo-desc">
            Khám phá hàng ngàn sản phẩm chính hãng với giá tốt nhất và dịch vụ tận tâm.
          </p>

          <div class="features-list">
            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <ShieldCheck :size="18" class="feature-icon" />
              </div>
              <div class="feature-text">
                <h3 class="feature-title">Chính hãng</h3>
                <p class="feature-subtitle">100% sản phẩm chính hãng</p>
              </div>
            </div>

            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <Truck :size="18" class="feature-icon" />
              </div>
              <div class="feature-text">
                <h3 class="feature-title">Giao nhanh</h3>
                <p class="feature-subtitle">Giao hàng toàn quốc siêu tốc</p>
              </div>
            </div>

            <div class="feature-item">
              <div class="feature-icon-wrapper">
                <Headphones :size="18" class="feature-icon" />
              </div>
              <div class="feature-text">
                <h3 class="feature-title">Hỗ trợ 24/7</h3>
                <p class="feature-subtitle">Đội ngũ hỗ trợ chuyên nghiệp</p>
              </div>
            </div>
          </div>
        </div>

        <div class="promo-illustration-container">
          <img src="../assets/login_promo.png" alt="Phone Store Products" class="promo-image" />
        </div>
      </section>

      <!-- Right Column: Login Card Panel -->
      <section class="login-card-panel">
        <div class="login-card">
          <!-- Card Lock Badge -->
          <div class="card-lock-badge-container">
            <div class="card-lock-badge">
              <Lock :size="24" class="lock-badge-icon" />
            </div>
          </div>

          <h2 class="card-title">Chào mừng trở lại! 👋</h2>
          <p class="card-subtitle">Đăng nhập để tiếp tục mua sắm</p>

          <form class="login-form" @submit.prevent="handleLogin">
            
            <div class="form-group">
              <label for="email" class="form-label">Email hoặc số điện thoại</label>
              <div class="input-wrapper">
                <User :size="18" class="input-icon" />
                <input 
                  type="text" 
                  id="email" 
                  class="form-input"
                  placeholder="Nhập email hoặc số điện thoại"
                  v-model="email" 
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
                  placeholder="Nhập mật khẩu"
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
            </div>

            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" id="remember" v-model="remember">
                <span class="checkmark"></span>
                <span class="remember-text">Ghi nhớ đăng nhập</span>
              </label>
              <a href="#" class="forgot-password">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="login-btn">
              <span>Đăng nhập</span>
              <ArrowRight :size="18" class="btn-arrow-icon" />
            </button>
          </form>

          <div class="social-divider">
            <span class="divider-text">hoặc</span>
          </div>

          <!-- Social Logins row -->
          <div class="social-login-container">
            <button class="social-btn" aria-label="Đăng nhập bằng Google">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22c-.22-.67-.35-1.37-.35-2.09z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
              </svg>
            </button>
            <button class="social-btn" aria-label="Đăng nhập bằng Facebook">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" fill="#1877F2"/>
              </svg>
            </button>
            <button class="social-btn" aria-label="Đăng nhập bằng Apple">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 4.17c.66-.81 1.11-1.93.99-3.06-1 .04-2.22.67-2.94 1.52-.63.73-1.18 1.87-1.03 2.97 1.12.09 2.27-.56 2.98-1.43z" fill="#000000"/>
              </svg>
            </button>
          </div>

          <div class="signup-footer">
            Chưa có tài khoản? <router-link to="register" class="signup-link">Đăng ký ngay</router-link>
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

.login-wrapper {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f6f5fa;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Ambient glow backgrounds */
.ambient-glow {
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
  z-index: 0;
  pointer-events: none;
}

.bg-glow-left {
  background: radial-gradient(circle, rgba(124, 58, 237, 0.15) 0%, rgba(255, 255, 255, 0) 70%);
  top: -10%;
  left: -10%;
}

.bg-glow-right {
  background: radial-gradient(circle, rgba(236, 72, 153, 0.12) 0%, rgba(255, 255, 255, 0) 70%);
  bottom: -10%;
  right: -10%;
}

.login-split-container {
  width: 100%;
  max-width: 1200px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
  padding: 40px 20px;
  z-index: 1;
}

/* Left Column: Promo Panel */
.promo-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-right: 20px;
  position: relative;
}

.floating-circle {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.08) 0%, rgba(236, 72, 153, 0.08) 100%);
  filter: blur(6px);
  right: 5%;
  top: 30%;
  z-index: -1;
  pointer-events: none;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-12px);
  }
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
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
  color: #1f2937;
}

.promo-tag {
  background: #ede9fe;
  color: #6d28d9;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
  width: fit-content;
  margin-bottom: 24px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.promo-title {
  font-size: 3rem;
  font-weight: 800;
  line-height: 1.25;
  color: #111827;
  margin-bottom: 20px;
}

.gradient-text {
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.promo-desc {
  font-size: 1rem;
  color: #4b5563;
  margin-bottom: 40px;
  max-width: 440px;
  line-height: 1.6;
}

.features-list {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.feature-icon-wrapper {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #ede9fe;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c3aed;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(124, 58, 237, 0.1);
}

.feature-icon {
  stroke-width: 2.2px;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: #1f2937;
}

.feature-subtitle {
  font-size: 0.75rem;
  color: #6b7280;
  line-height: 1.3;
}

.promo-illustration-container {
  display: flex;
  justify-content: flex-start;
  align-items: flex-end;
  margin-top: auto;
}

.promo-image {
  max-height: 290px;
  object-fit: contain;
  width: auto;
  filter: drop-shadow(0 20px 30px rgba(124, 58, 237, 0.1));
}

/* Right Column: Login Card Panel */
.login-card-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 480px;
  background: #ffffff;
  border-radius: 32px;
  padding: 48px 40px;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.card-lock-badge-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.card-lock-badge {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #ede9fe;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c3aed;
  box-shadow: 0 10px 20px rgba(124, 58, 237, 0.1);
}

.lock-badge-icon {
  stroke-width: 2px;
}

.card-title {
  font-size: 1.6rem;
  font-weight: 800;
  text-align: center;
  color: #1f2937;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
}

.card-subtitle {
  font-size: 0.9rem;
  text-align: center;
  color: #6b7280;
  margin-bottom: 32px;
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
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
  padding: 13px 16px 13px 44px;
  border: 1.5px solid #f3f4f6;
  border-radius: 12px;
  font-size: 0.95rem;
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  font-size: 0.85rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  color: #4b5563;
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

.forgot-password {
  color: #7c3aed;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s ease;
}

.forgot-password:hover {
  color: #da458f;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  color: #ffffff;
  border: none;
  border-radius: 14px;
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
  margin: 24px 0;
}

.social-divider::before,
.social-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #f3f4f6;
}

.divider-text {
  padding: 0 16px;
  font-size: 0.8rem;
  color: #9ca3af;
}

/* Social login row */
.social-login-container {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}

.social-btn {
  flex: 1;
  padding: 12px;
  border: 1.5px solid #f3f4f6;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.social-btn:hover {
  border-color: #e5e7eb;
  background: #fafafa;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
}

.social-btn svg {
  display: block;
}

/* Footer register link */
.signup-footer {
  text-align: center;
  font-size: 0.88rem;
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
  .login-split-container {
    grid-template-columns: 1fr;
    max-width: 520px;
    padding: 30px 20px;
  }

  .promo-panel {
    display: none; /* Hide promo sidebar on mobile/tablet */
  }

  .login-card-panel {
    width: 100%;
  }

  .login-card {
    padding: 40px 28px;
    border-radius: 28px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 20px;
    border-radius: 24px;
    box-shadow: none;
    border: none;
    background: transparent;
  }
  
  .login-wrapper {
    background-color: #ffffff;
  }
  
  .ambient-glow {
    display: none;
  }
}
</style>