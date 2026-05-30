<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue3-toastify'
import { authService } from '@/service/AuthService'

const router = useRouter()
const showMenu = ref(false)

const isAuth = computed(() => authService.isAuthenticated())

// toggle menu user
const toggleMenu = () => {
  showMenu.value = !showMenu.value;
};

// logout
const logout = () => {
  authService.logout();
  showMenu.value = false;
  toast.success("Đăng xuất thành công!");
  router.push("/login");
};

// đóng menu khi click ngoài
const handleClickOutside = (event: MouseEvent) => {
  const menu = document.querySelector(".user-menu-wrapper");
  if (menu && !menu.contains(event.target as Node)) {
    showMenu.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

</script>

<template>
  <header class="header">
    <nav class="nav-container">
      <div class="logo">📱 Phone Store</div>

      <ul class="nav-menu">
        <li><router-link to="/customer/home">Trang Chủ</router-link></li>
        <li><router-link to="/customer/product-home">Sản Phẩm</router-link></li>
        <li><router-link to="/customer/home">Liên Hệ</router-link></li>
      </ul>

      <div class="nav-actions">
        <!-- SEARCH -->
        <button class="icon-btn" aria-label="Tìm kiếm">🔍</button>

        <!-- CART -->
        <router-link :to="{ name: 'cart' }" class="icon-btn cart-btn">
          🛒
        </router-link>

        <!-- LOGIN / USER MENU -->
        <router-link
            v-if="!isAuth"
            to="/login"
            class="login-btn"
        >
          Đăng nhập
        </router-link>

        <!-- USER MENU -->
        <div v-else class="user-menu-wrapper">
          <button class="icon-btn user-btn" @click.stop="toggleMenu">👤</button>

          <ul v-if="showMenu" class="dropdown">
            <li>
              <router-link to="/customer/user-profile">Thông tin cá nhân</router-link>
            </li>
            <li>
              <router-link to="/customer/order-by-user">Đơn hàng của tôi</router-link>
            </li>
            <li @click="logout">Đăng xuất</li>
          </ul>
        </div>
      </div>
    </nav>
  </header>
</template>

<style scoped>
/* Header */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 2em;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-menu {
  display: flex;
  gap: 40px;
  list-style: none;
}

.nav-menu a {
  color: white;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.05em;
  transition: all 0.3s ease;
  position: relative;
}

.nav-menu a::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 0;
  height: 3px;
  background: white;
  transition: width 0.3s ease;
}

.nav-menu a:hover::after {
  width: 100%;
}

.nav-actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

/* Icon Button */
.icon-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  width: 45px;
  height: 45px;
  border-radius: 50%;
  font-size: 1.3em;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.28);
  transform: scale(1.1);
}

/* Cart Badge */
.cart-btn {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -3px;
  right: -3px;
  background: #ff4d4d;
  color: white;
  font-size: 0.75em;
  padding: 2px 6px;
  border-radius: 50%;
  font-weight: 700;
}

/* Login Button */
.login-btn {
  padding: 10px 18px;
  background: rgba(255, 255, 255, 0.25);
  color: white;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

/* USER DROPDOWN */
.user-menu-wrapper {
  position: relative;
}

.dropdown {
  position: absolute;
  right: 0;
  top: 55px;
  background: white;
  color: #333;
  list-style: none;
  padding: 10px 0;
  width: 180px;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.2s ease;
}

.dropdown li {
  padding: 12px 18px;
  cursor: pointer;
  font-weight: 500;
}

.dropdown li:hover {
  background: #f2f2f2;
}

.dropdown a {
  color: #333;
  text-decoration: none;
  display: block;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
