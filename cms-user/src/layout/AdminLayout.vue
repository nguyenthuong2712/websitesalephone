<script setup lang="ts">
import {authService} from "@/service/AuthService.ts";
import {toast} from "vue3-toastify";
import {ref} from "vue";
import router from "@/router.ts";
const showMenu = ref(false);

const logout = () => {
  authService.logout();
  showMenu.value = false;
  toast.success("Đăng xuất thành công!");
  router.push("/login");
};
</script>

<template>
  <div class="container"><!-- Sidebar Navigation -->
    <aside class="sidebar">
      <nav class="nav-menu">
        <router-link class="nav-item" data-page="dashboard" to="/admin/dashboard">
          <span class="nav-icon">📊</span>
          <span id="nav-item-1">Dashboard</span>
        </router-link>
        <router-link href="#" class="nav-item" data-page="analytics" to="/admin/order">
          <span class="nav-icon">📈</span>
          <span id="nav-item-2">Order</span>
        </router-link>
        <router-link href="#" class="nav-item" data-page="projects" to="/admin/product">
          <span class="nav-icon">📁</span>
          <span id="nav-item-3">Product</span>
        </router-link>
        <router-link href="#" class="nav-item" data-page="team" to="/admin/user">
          <span class="nav-icon">👥</span>
          <span id="nav-item-4">User</span>
        </router-link>
      </nav>
    </aside><!-- Main Content -->
    <main><!-- Header -->
      <header class="header">
        <div class="header-left">
        </div>
        <div class="header-right">
          <div class="search-box">
          </div>
          <div class="user-profile">
            <div class="user-avatar" id="user-avatar">
              JD
            </div>

            <div class="user-info">
              <div class="user-name" id="user-name">
                John Doe
              </div>
              <div class="user-role">
                Administrator
              </div>
            </div>

            <button class="logout-btn" @click="logout">
              Logout
            </button>
          </div>
        </div>
      </header><!-- Content Area -->
      <div class="content-area">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Ẩn nút logout lúc đầu */
.logout-btn {
  padding: 6px 14px;
  border: 1px solid #d9534f;
  background-color: #fff;
  color: #d9534f;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  opacity: 0;
  pointer-events: none;
  transition: all 0.25s ease;
}

/* Khi hover: hiện lên */
.user-profile:hover .logout-btn {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}
body {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  height: 100%;
  overflow: hidden;
}

* {
  box-sizing: border-box;
}

html {
  height: 100%;
}

.container {
  display: flex;
  height: 100%;
  width: 100%;
}

/* Sidebar Navigation */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  padding: 24px 0;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 100;
  height: 929px;
  max-height: 1050px;
}

.logo-section {
  padding: 0 24px 32px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.5px;
}

.nav-menu {
  flex: 1;
  padding: 0 16px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  margin-bottom: 6px;
  border-radius: 10px;
  color: #cbd5e1;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  transform: translateX(4px);
}

.nav-item.active,
.nav-item:focus {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.nav-icon {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Main Content Area */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Header */
.header {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  padding: 20px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 50;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  font-size: 14px;
  color: #64748b;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  position: relative;
}

.search-input {
  padding: 10px 16px 10px 40px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  width: 280px;
  font-size: 14px;
  transition: all 0.3s ease;
  background: #ffffff;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.notification-btn {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #f8fafc;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.notification-btn:hover {
  background: #e2e8f0;
  transform: scale(1.05);
}

.notification-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid #ffffff;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 10px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-profile:hover {
  background: #e2e8f0;
  transform: scale(1.02);
}

.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-weight: 600;
  font-size: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.user-role {
  font-size: 12px;
  color: #64748b;
}

/* Content Area */
.content-area {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 80px;
  width: 1575px;
}

.content-placeholder {
  background: #ffffff;
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;
  color: #64748b;
}

.content-placeholder h2 {
  font-size: 24px;
  color: #1e293b;
  margin-bottom: 12px;
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar {
    width: 70px;
    padding: 20px 0;
  }

  .logo-section {
    padding: 0 12px 20px 12px;
  }

  .logo-text {
    display: none;
  }

  .logo-icon {
    margin: 0 auto;
  }

  .nav-menu {
    padding: 0 8px;
  }

  .nav-item {
    justify-content: center;
    padding: 14px 8px;
  }

  .nav-item span {
    display: none;
  }

  .header {
    padding: 16px 20px;
  }

  .search-box {
    display: none;
  }

  .header-title {
    font-size: 20px;
  }

  .user-info {
    display: none;
  }
}
/* Sidebar Navigation */
.sidebar {
  position: fixed; /* cố định bên trái */
  top: 0;
  left: 0;
  height: 100%;
  z-index: 100;
}

/* Header */
.header {
  position: fixed; /* cố định phía trên */
  top: 0;
  left: 260px; /* bằng width sidebar */
  right: 0;
  z-index: 50;
}

/* Main Content Area */
.main-content {
  margin-left: 260px; /* tránh sidebar */
  margin-top: 80px;   /* bằng height header */
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Content Area */
.content-area {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.5);
  margin-top: 80px;
  width: 1575px;
}

</style>