<script setup lang="ts">
import { ref, onMounted } from "vue";
import {orderService} from "@/service/OrderService.ts";

const totalOrder = ref(0);
const totalProduct = ref(0);
const totalCustomer = ref(0);
const totalRevenue = ref(0);
const totalCancelled = ref(0);

async function loadDashboard() {
  totalOrder.value = (await orderService.getDashboard("ORDER")).data.data;
  totalProduct.value = (await orderService.getDashboard("PRODUCT")).data.data;
  totalCustomer.value = (await orderService.getDashboard("CUSTOMER")).data.data;
  totalRevenue.value = (await orderService.getDashboard("REVENUE")).data.data;
  totalCancelled.value = (await orderService.getDashboard("CANCELLED")).data.data;
}

onMounted(() => {
  loadDashboard();
});
</script>


<template>
  <div class="admin-wrapper">
    <main class="main-content">
      <section class="stats-grid">

        <!-- Tổng Đơn Hàng -->
        <div class="stat-card">
          <div class="stat-icon">💰</div>
          <div class="stat-value">{{ totalOrder }}</div>
          <div class="stat-label">Tổng Đơn Hàng</div>
        </div>

        <!-- Tổng Sản Phẩm -->
        <div class="stat-card">
          <div class="stat-icon">📱</div>
          <div class="stat-value">{{ totalProduct }}</div>
          <div class="stat-label">Sản Phẩm</div>
        </div>

        <!-- Khách Hàng -->
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-value">{{ totalCustomer }}</div>
          <div class="stat-label">Khách Hàng</div>
        </div>

        <!-- Doanh Thu -->
        <div class="stat-card">
          <div class="stat-icon">💵</div>
          <div class="stat-value">{{ totalRevenue.toLocaleString() }} VNĐ</div>
          <div class="stat-label">Doanh Thu (VNĐ)</div>
        </div>

        <!-- Đơn Hủy -->
        <div class="stat-card">
          <div class="stat-icon">🗑️</div>
          <div class="stat-value">{{ totalCancelled }}</div>
          <div class="stat-label">Đơn Hủy</div>
        </div>

      </section>
    </main>
  </div>
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
}

.admin-wrapper {
  display: flex;
  min-height: 100%;
}

/* Main */
.main-content {
  flex: 1;
  padding: 40px;
  overflow-y: auto;
}

/* Stats Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.12);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(30%, -30%);
}

.stat-icon {
  font-size: 3em;
  margin-bottom: 15px;
}

.stat-value {
  font-size: 2.2em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 1.05em;
  font-weight: 600;
}

.stat-trend {
  margin-top: 10px;
  font-size: 0.9em;
  font-weight: 600;
}

.trend-up {
  color: #43e97b;
}

.trend-down {
  color: #ff6b6b;
}
</style>
