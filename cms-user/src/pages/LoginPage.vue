<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthUser } from '../models/AuthUser.ts'
import { authService } from "../service/AuthService.ts"
import { toast } from "vue3-toastify"
import "vue3-toastify/dist/index.css"

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
    <div class="login-container"><!-- Logo Section -->
      <div class="logo-section">
        <div class="logo">
          📱
        </div>
        <h1 class="brand-name">Phone Store</h1>
        <p class="welcome-text">Chào mừng bạn trở lại!</p>
      </div><!-- Error/Success Messages -->
      <div class="error-message" id="errorMessage"><span>⚠️</span> <span id="errorText"></span>
      </div>
      <div class="success-message" id="successMessage"><span>✓</span> <span id="successText"></span>
      </div><!-- Login Form -->
      <form class="login-form" id="loginForm" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email" class="form-label">Email hoặc Số điện thoại</label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input type="text" id="email" class="form-input"
                   placeholder="Nhập email hoặc số điện thoại"
                   v-model="email" required>
          </div>
        </div>

        <div class="form-group">
          <label for="password" class="form-label">Mật khẩu</label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input :type="showPassword ? 'text' : 'password'"
                   id="password"
                   class="form-input"
                   placeholder="Nhập mật khẩu"
                   v-model="password"
                   required>

            <button type="button"
                    class="password-toggle"
                    @click="togglePassword"> 👁️
            </button>
          </div>
        </div>

        <div class="form-options">
          <div class="remember-me">
            <input type="checkbox" id="remember" v-model="remember">
            <label for="remember">Ghi nhớ đăng nhập</label>
          </div>
          <a href="#" class="forgot-password">Quên mật khẩu?</a>
        </div>

        <button type="submit" class="login-btn">
          Đăng nhập <span>→</span>
        </button>
      </form>
      <!-- Divider -->
      <div class="signup-link">
        Chưa có tài khoản?
        <router-link to="register">Đăng ký ngay</router-link>
      </div>
    </div>
  </main>
</template>

<style scoped>
body {
  box-sizing: border-box;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #1a1a2e;
  line-height: 1.6;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  background: white;
  border-radius: 20px;
  padding: 45px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease;
  width: 450px;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 35px;
}

.logo {
  font-size: 4em;
  margin-bottom: 15px;
  animation: bounce 1s ease;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.brand-name {
  font-size: 1.8em;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.welcome-text {
  color: #666;
  font-size: 0.95em;
}

.login-form {
  margin-top: 30px;
}

.form-group {
  margin-bottom: 25px;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 10px;
  font-size: 0.95em;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2em;
  color: #999;
}

.form-input {
  width: 100%;
  padding: 14px 15px 14px 45px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1em;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #bbb;
}

.password-toggle {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2em;
  color: #999;
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: #667eea;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 0.9em;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #667eea;
}

.remember-me label {
  color: #666;
  cursor: pointer;
  user-select: none;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #764ba2;
}

.login-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.05em;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.divider {
  display: flex;
  align-items: center;
  margin: 30px 0;
  color: #999;
  font-size: 0.9em;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e0e0e0;
}

.divider::before {
  margin-right: 15px;
}

.divider::after {
  margin-left: 15px;
}

.social-login {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 25px;
}

.social-btn {
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95em;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.social-btn:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.signup-link {
  text-align: center;
  margin-top: 25px;
  color: #666;
  font-size: 0.95em;
}

.signup-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 700;
  transition: color 0.3s ease;
}

.signup-link a:hover {
  color: #764ba2;
}

.error-message {
  background: #ffe0e0;
  color: #d32f2f;
  padding: 12px 15px;
  border-radius: 10px;
  font-size: 0.9em;
  margin-bottom: 20px;
  display: none;
  align-items: center;
  gap: 8px;
}

.error-message.show {
  display: flex;
}

.success-message {
  background: #e0ffe0;
  color: #2e7d32;
  padding: 12px 15px;
  border-radius: 10px;
  font-size: 0.9em;
  margin-bottom: 20px;
  display: none;
  align-items: center;
  gap: 8px;
}

.success-message.show {
  display: flex;
}

/* Responsive */
@media (max-width: 480px) {
  .login-container {
    padding: 35px 25px;
  }

  .brand-name {
    font-size: 1.5em;
  }

  .social-login {
    grid-template-columns: 1fr;
  }
}
</style>