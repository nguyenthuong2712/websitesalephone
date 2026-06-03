<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { authService } from "@/service/AuthService.ts";
import {toast} from "vue3-toastify";
import { Search, ShoppingCart, User, Smartphone } from '@lucide/vue';
import { useCartStore } from "@/cartStore";

const router = useRouter();
const showMenu = ref(false);
const cartStore = useCartStore();
const cartCount = computed(() => cartStore.cartCount);

// computed reactive: tự cập nhật khi token thay đổi
const isAuth = computed(() => authService.isAuthenticated());
const role = computed(() => authService.getRole());

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
  cartStore.fetchCartCount();
});

</script>

<template>
  <header class="header">
    <nav class="nav-container">
      <div class="logo-menu-wrapper">
        <div class="logo">
          <Smartphone :size="20" class="logo-icon" />
          <span>Phone Store</span>
        </div>

        <ul class="nav-menu">
          <li><router-link to="/customer/home">Trang chủ</router-link></li>
          <li><router-link to="/customer/product-home">Sản phẩm</router-link></li>
        </ul>
      </div>

      <div class="nav-actions">
        <!-- SEARCH -->
        <button class="action-btn" aria-label="Tìm kiếm">
          <Search :size="18" />
        </button>

        <!-- CART -->
        <router-link :to="{ name: 'cart' }" class="action-btn cart-btn">
          <ShoppingCart :size="18" />
          <span class="cart-badge">{{ cartCount }}</span>
        </router-link>

        <!-- LOGIN / USER MENU -->
        <router-link
            v-if="!isAuth"
            to="/login"
            class="action-btn user-btn"
            aria-label="Đăng nhập"
        >
          <User :size="18" />
        </router-link>

        <!-- USER MENU -->
        <div v-else class="user-menu-wrapper">
          <button class="action-btn user-btn" @click.stop="toggleMenu" aria-label="Tài khoản">
            <User :size="18" />
          </button>

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
  background: transparent;
  padding: 16px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1400px;
  width: calc(100% - 40px);
  margin: 0 auto;
  padding: 12px 32px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  border-radius: 24px;
  border: 1px solid rgba(229, 231, 235, 0.6);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.03);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.logo {
  font-size: 1.25rem;
  font-weight: 800;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-menu-wrapper {
  display: flex;
  align-items: center;
  gap: 48px;
}

.logo-icon {
  color: #7c3aed;
  stroke-width: 2.5px;
}

.nav-menu {
  display: flex;
  gap: 32px;
  list-style: none;
}

.nav-menu a {
  color: #4b5563;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s ease;
  position: relative;
  padding: 4px 0;
}

.nav-menu a:hover {
  color: #7c3aed;
}

.nav-menu a::after {
  content: "";
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2.5px;
  background: #7c3aed;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.nav-menu a:hover::after {
  width: 100%;
}

.nav-menu a.router-link-active {
  color: #7c3aed;
}

.nav-menu a.router-link-active::after {
  width: 100%;
}

.nav-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* Action button styling */
.action-btn {
  background: #ffffff;
  color: #4b5563;
  border: 1.5px solid #f3f4f6;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  border-color: #e5e7eb;
  color: #7c3aed;
  background: #fafafa;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02);
}

.cart-btn {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #7c3aed;
  color: white;
  font-size: 0.65rem;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(124, 58, 237, 0.3);
}

/* USER DROPDOWN */
.user-menu-wrapper {
  position: relative;
}

.dropdown {
  position: absolute;
  right: 0;
  top: 50px;
  background: white;
  color: #1f2937;
  list-style: none;
  padding: 8px 0;
  width: 190px;
  border-radius: 12px;
  border: 1px solid rgba(229, 231, 235, 0.6);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  animation: fadeIn 0.2s ease;
}

.dropdown li {
  padding: 10px 16px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.88rem;
  transition: background-color 0.2s ease;
}

.dropdown li:hover {
  background: #f9fafb;
  color: #7c3aed;
}

.dropdown a {
  color: inherit;
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

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
  .nav-container {
    padding: 10px 20px;
    width: calc(100% - 24px);
    border-radius: 16px;
  }
}
</style>
