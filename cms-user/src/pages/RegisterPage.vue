<script setup lang="ts">
import {ref} from "vue";
import {authService} from "../service/AuthService.ts";
import {RegisterRequest} from "../models/RegisterRequest.ts";

// Input refs
const fullName = ref("");
const email = ref("");
const phone = ref("");
const password = ref("");
const confirmPassword = ref("");

// Error UI refs
const emailError = ref(false);
const phoneError = ref(false);
const confirmPassError = ref(false);

const errorMessage = ref("");
const successMessage = ref("");

// Regex truyền thống, không màu mè
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phoneRegex = /^(0[0-9]{9,10})$/;

// Submit handler
import { toast } from "vue3-toastify";

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

  const req = new RegisterRequest(
      fullName.value,
      phone.value,
      password.value,
      email.value
  );

  try {
    const res = await authService.register(req);

    if (res.data?.code === 0) {
      toast.success("Đăng ký thành công!");

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
    <div class="signup-container"><!-- Logo Section -->
      <div class="logo-section">
        <div class="logo">
          📱
        </div>
        <h1 class="brand-name">Phone Store</h1>
        <p class="welcome-text">Tạo tài khoản mới</p>
      </div><!-- Error/Success Messages -->
      <div class="error-message" id="errorMessage"><span>⚠️</span> <span id="errorText"></span>
      </div>
      <div class="success-message" id="successMessage"><span>✓</span> <span id="successText"></span>
      </div><!-- Signup Form -->
      <form class="signup-form" @submit="submitForm">
        <div class="form-group">
          <label for="firstName" class="form-label">Họ Và Tên <span class="required">*</span></label>
          <div class="input-wrapper">
            <span class="input-icon">👤</span>
            <input type="text" id="firstName"
                   class="form-input"
                   placeholder="Nguyễn Văn A"
                   required v-model="fullName">
          </div>
        </div>
        <div class="form-group">
          <label for="email" class="form-label">Email <span class="required">*</span></label>
          <div class="input-wrapper">
            <span class="input-icon">✉️</span>
            <input type="email" id="email"
                   class="form-input"
                   placeholder="example@email.com" required v-model="email">
          </div>
        </div>
        <div class="form-group">
          <label for="phone" class="form-label">Số điện thoại <span class="required">*</span></label>
          <div class="input-wrapper">
            <span class="input-icon">📞</span>
            <input type="tel" id="phone" class="form-input"
                   placeholder="0123456789" v-model="phone" required>
          </div>
        </div>
        <div class="form-group">
          <label for="password" class="form-label">Mật khẩu <span class="required">*</span></label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input type="password" id="password"
                   class="form-input"
                   placeholder="Tối thiểu 6 ký tự" v-model="password" required>
            <button type="button" class="password-toggle" id="togglePassword"> 👁️</button>
          </div>
          <div class="password-strength-text" id="strengthText"></div>
        </div>
        <div class="form-group">
          <label for="confirmPassword" class="form-label">Xác nhận mật khẩu <span class="required">*</span></label>
          <div class="input-wrapper">
            <span class="input-icon">🔒</span>
            <input type="password" id="confirmPassword"
                   class="form-input"
                   placeholder="Nhập lại mật khẩu" v-model="confirmPassword" required>
            <button type="button" class="password-toggle" id="toggleConfirmPassword"> 👁️</button>
          </div>
        </div>
        <button type="submit" class="signup-btn"> Đăng ký <span>→</span></button>
      </form><!-- Divider -->
      <div class="login-link">
        Đã có tài khoản?
        <router-link to="login">Đăng nhập ngay</router-link>
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
  min-height: 100%;
}

.signup-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.signup-container {
  background: white;
  border-radius: 20px;
  padding: 45px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease;
  width: 550px;
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
  font-size: 3.5em;
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

.signup-form {
  margin-top: 30px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
  font-size: 0.9em;
}

.required {
  color: #e74c3c;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.1em;
  color: #999;
}

.form-input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 0.95em;
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

.form-input.error {
  border-color: #e74c3c;
}

.form-input.success {
  border-color: #27ae60;
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

.password-strength {
  margin-top: 8px;
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
}

.password-strength-bar {
  height: 100%;
  width: 0;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.password-strength-text {
  font-size: 0.8em;
  margin-top: 5px;
  font-weight: 600;
}

.strength-weak {
  background: #e74c3c;
}

.strength-medium {
  background: #f39c12;
}

.strength-strong {
  background: #27ae60;
}

.field-hint {
  font-size: 0.8em;
  color: #999;
  margin-top: 5px;
}

.field-error {
  font-size: 0.8em;
  color: #e74c3c;
  margin-top: 5px;
  display: none;
}

.field-error.show {
  display: block;
}

.terms-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 25px 0;
  cursor: pointer;
}

.terms-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-top: 2px;
  cursor: pointer;
  accent-color: #667eea;
}

.terms-checkbox label {
  color: #666;
  font-size: 0.9em;
  cursor: pointer;
  user-select: none;
}

.terms-checkbox a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.terms-checkbox a:hover {
  color: #764ba2;
}

.signup-btn {
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

.signup-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.signup-btn:active {
  transform: translateY(0);
}

.signup-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.divider {
  display: flex;
  align-items: center;
  margin: 25px 0;
  color: #999;
  font-size: 0.85em;
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

.social-signup {
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
  font-size: 0.9em;
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

.login-link {
  text-align: center;
  margin-top: 25px;
  color: #666;
  font-size: 0.95em;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 700;
  transition: color 0.3s ease;
}

.login-link a:hover {
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
@media (max-width: 580px) {
  .signup-container {
    padding: 35px 25px;
  }

  .brand-name {
    font-size: 1.5em;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .social-signup {
    grid-template-columns: 1fr;
  }
}
</style>