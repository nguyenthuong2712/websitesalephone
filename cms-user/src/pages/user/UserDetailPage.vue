<script setup lang="ts">
import {ref, onMounted} from "vue";
import {userService} from "@/service/UserService";
import {toast} from "vue3-toastify";

import {useRoute} from "vue-router";
import {CreateUserDto} from "@/models/CreateUserDto.ts";
import router from "@/router.ts";
import {useUserStore} from "@/userStore.ts";
const route = useRoute();
const loginId = route.params.id as string;
const isUpdate = ref(!!loginId);

// form state
const form = ref<CreateUserDto>(
    new CreateUserDto({
      id: "",
      loginId: "",
      password: "",
      fullName: "",
      profileImg: "",
      email: "",
      telNo: "",
      gender: "male",
      userCode: "",
      note: "",
      role: "",
      address: "",
      isDeleted: false
    })
);

// Load user nếu đang update
const loadUserDetail = async () => {
  if (!loginId) return;

  try {
    const res = await userService.getUserByLoginId(loginId);
    const data = res.data.data;
    form.value = new CreateUserDto({
      id: loginId,
      loginId: data.loginId,
      password: "",
      fullName: data.fullName,
      profileImg: data.profileImg,
      email: data.email,
      telNo: data.telNo,
      gender: data.gender,
      userCode: data.userCode,
      note: data.note,
      role: data.role,
      address: data.address,
      isDeleted: data.deleted
    });

  } catch (e) {
    console.log(e)
    toast.error("Không tải được thông tin người dùng");
  }
};

// Submit form (create + update chung)
const submitForm = async () => {
  try {
    if (isUpdate.value) {
      await userService.updateUser(form.value.toPayload());
      loadUserDetail();
      toast.success("Cập nhật người dùng thành công!");
    } else {
      await userService.createUser(form.value.toPayload());
      toast.success("Tạo người dùng mới thành công!");
      router.push("/admin/user")
    }

  } catch (e: any) {
    console.log(e)
    toast.error(e?.response?.data?.message || "Lỗi xử lý người dùng");
  }
};

onMounted(() => {
  loadUserDetail();
});

// toggle active
const toggleActive = () => {
  form.value.active = !form.value.active;
};

// cancel
const cancel = () => {
  router.push("/admin/user");
};
</script>

<template>
  <main class="form-card">
    <form id="createUserForm" @submit.prevent="submitForm">
      <!-- Personal Info -->
      <section class="form-section">
        <h2 class="section-title">📋 Thông Tin Cá Nhân</h2>

        <div class="form-grid">
          <div class="form-group">
            <label for="fullName">Họ và tên *</label>
            <input type="text" id="fullName" v-model="form.fullName" required />
          </div>

          <div class="form-group">
            <label for="phone">Số điện thoại *</label>
            <input type="tel" id="phone" v-model="form.telNo" required />
          </div>

          <div class="form-group">
            <label for="email">Email *</label>
            <input type="email" id="email" v-model="form.email" required />
          </div>

          <div class="form-group">
            <label>Giới tính *</label>
            <div class="radio-group">
              <label class="radio-option">
                <input type="radio" value="male" v-model="form.gender" />
                <span>👨 Nam</span>
              </label>

              <label class="radio-option">
                <input type="radio" value="female" v-model="form.gender" />
                <span>👩 Nữ</span>
              </label>

              <label class="radio-option">
                <input type="radio" value="other" v-model="form.gender" />
                <span>⚧️ Khác</span>
              </label>
            </div>
          </div>
        </div>
      </section>

      <!-- Account Info -->
      <section class="form-section">
        <h2 class="section-title">🔐 Thông Tin Tài Khoản</h2>

        <div class="form-grid">
          <div class="form-group">
            <label for="username">Tên đăng nhập *</label>
            <input type="text" id="username" v-model="form.loginId" required />
          </div>

          <div class="form-group">
            <label for="role">Vai trò *</label>
            <select id="role" v-model="form.role" required>
              <option value="">-- Chọn vai trò --</option>
              <option value="ADMIN">👑 Admin</option>
              <option value="STAFF">👔 Staff</option>
              <option value="CUSTOMER">🛍 Customer</option>
            </select>
          </div>

          <div class="form-group full-width">
            <label>Trạng thái *</label>
            <div class="toggle-container">
              <div class="toggle-switch" :class="{ active: !form.isDeleted }" @click="toggleActive">
                <div class="toggle-slider"></div>
              </div>
              <span class="toggle-label" :class="{ active: !form.isDeleted }">
                {{ form.isDeleted ? "✕ Không hoạt động" : "✓ Hoạt động" }}
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- Additional Info -->
      <section class="form-section">
        <h2 class="section-title">📝 Thông Tin Bổ Sung</h2>

        <div class="form-grid single-column">
          <div class="form-group">
            <label for="address">Địa chỉ</label>
            <input type="text" id="address" v-model="form.address" />
          </div>

          <div class="form-group">
            <label for="notes">Ghi chú</label>
            <input type="text" id="notes" v-model="form.note" />
          </div>
        </div>
      </section>

      <!-- Actions -->
      <div class="form-actions">
        <button type="submit" class="btn btn-primary">
          {{ isUpdate ? "✓ Cập nhật User" : "✓ Tạo User" }}
        </button>

        <button type="button" @click="cancel" class="btn btn-secondary">
          ✕ Hủy bỏ
        </button>
      </div>
    </form>
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
  padding: 40px 20px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

/* Header */
.page-header {
  background: white;
  padding: 35px 40px;
  border-radius: 20px;
  margin-bottom: 30px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
}

.page-header h1 {
  font-size: 2.5em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb {
  color: #666;
  font-size: 1em;
  margin-top: 10px;
}

.breadcrumb a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

/* Form Card */
.form-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
}

.form-section {
  margin-bottom: 35px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 1.4em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 3px solid #f0f0f0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 25px;
}

.form-grid.single-column {
  grid-template-columns: 1fr;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

label {
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 10px;
  font-size: 1em;
  display: flex;
  align-items: center;
  gap: 5px;
}

.required {
  color: #ff6b6b;
  font-weight: 700;
}

input[type="text"],
input[type="tel"],
input[type="email"],
select {
  padding: 14px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  font-size: 1em;
  font-weight: 600;
  transition: all 0.3s ease;
  font-family: inherit;
}

input[type="text"]:focus,
input[type="tel"]:focus,
input[type="email"]:focus,
select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

input::placeholder {
  color: #999;
  font-weight: 500;
}

/* Radio Group */
.radio-group {
  display: flex;
  gap: 25px;
  margin-top: 10px;
}

.radio-option {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 12px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  transition: all 0.3s ease;
  font-weight: 600;
}

.radio-option:hover {
  border-color: #667eea;
  background: #f9f9ff;
}

.radio-option input[type="radio"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
  accent-color: #667eea;
}

.radio-option input[type="radio"]:checked + .radio-label {
  color: #667eea;
}

.radio-option:has(input[type="radio"]:checked) {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

/* Select Styling */
select {
  cursor: pointer;
  background: white;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 16 16'%3E%3Cpath fill='%23667eea' d='M8 11L3 6h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 15px center;
  padding-right: 45px;
}

select option {
  padding: 10px;
}

/* Toggle Switch */
.toggle-container {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.toggle-switch {
  position: relative;
  width: 60px;
  height: 32px;
  background: #e0e0e0;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.toggle-switch.active {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.toggle-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 24px;
  height: 24px;
  background: white;
  border-radius: 50%;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-switch.active .toggle-slider {
  left: 32px;
}

.toggle-label {
  font-weight: 700;
  font-size: 1.05em;
}

.toggle-label.active {
  color: #43e97b;
}

.toggle-label.inactive {
  color: #999;
}

/* Help Text */
.help-text {
  font-size: 0.9em;
  color: #666;
  margin-top: 6px;
  font-weight: 500;
}

/* Action Buttons */
.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 3px solid #f0f0f0;
}

.btn {
  padding: 16px 35px;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 1.05em;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #f9f9ff;
}

/* Success Message */
.success-message {
  display: none;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  padding: 20px 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-weight: 700;
  font-size: 1.1em;
  align-items: center;
  gap: 12px;
  box-shadow: 0 5px 15px rgba(67, 233, 123, 0.3);
}

.success-message.show {
  display: flex;
}

/* Avatar Upload */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 25px;
  margin-top: 10px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3em;
  border: 4px solid #e0e0e0;
}

.upload-btn {
  padding: 12px 24px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-btn:hover {
  background: #667eea;
  color: white;
}

/* Responsive */
@media (max-width: 768px) {
  body {
    padding: 20px 10px;
  }

  .page-header,
  .form-card {
    padding: 25px 20px;
  }

  .page-header h1 {
    font-size: 2em;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .radio-group {
    flex-direction: column;
    gap: 12px;
  }

  .form-actions {
    flex-direction: column;
  }

  .avatar-upload {
    flex-direction: column;
    text-align: center;
  }
}
</style>