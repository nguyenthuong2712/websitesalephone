<script setup lang="ts">
import Header from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import {orderService} from "@/service/OrderService.ts";
import {useUserStore} from "@/userStore.ts";
import {onMounted, ref} from "vue";
import {formatCurrency} from "@/utils/Constant.ts";

const searchText = ref('');
const from = ref(null);
const to = ref(null);
const order = ref<any>(null);
const activeStatus = ref(null);
const countAll = ref(0);
const countPending = ref(0);
const countConfirmed = ref(0);
const countShipping = ref(0);
const countDelivered = ref(0);
const countCompleted = ref(0);
const countCancelled = ref(0);

const countMap = {
  ALL: countAll,
  PENDING: countPending,
  CONFIRMED: countConfirmed,
  SHIPPING: countShipping,
  DELIVERED: countDelivered,
  COMPLETED: countCompleted,
  CANCELLED: countCancelled
};


const countOrderByUser = async (status: string) => {
  const req = {
    userId: userStore.user.id,
    status: status
  };

  try {
    const res = await orderService.countOrderByUser(req);

    if (countMap[status]) {
      countMap[status].value = res.data.data;
    }

  } catch (err: any) {
    console.log(err);
  }
};

const getListOrderByUser = async (status: string) => {
  activeStatus.value = status;
  const req: OrderByUserRequest = {
    id: userStore.user.id,
    searchText: searchText.value,
    status,
    fromDate: toIso(from.value),
    toDate: toIso(to.value),
  };
  try {
    const res = await orderService.getListOrderByUser(req);
    order.value = res.data.data;
  } catch (err: any) {
    console.log(err)
  }
};

function toIso(date: any) {
  if (date === null) return null;
  return `${date.value}T00:00:00+07:00`;
}

const userStore = useUserStore();
const user = userStore.user;

onMounted(async () => {
  await userStore.getUserByLoginId();
  await getListOrderByUser(null)
  await countOrderByUser("ALL");
  await countOrderByUser("PENDING");
  await countOrderByUser("CONFIRMED");
  await countOrderByUser("SHIPPING");
  await countOrderByUser("DELIVERED");
  await countOrderByUser("COMPLETED");
  await countOrderByUser("CANCELLED");
});

</script>

<template>
  <Header/>
  <div class="page-wrapper">
    <div class="container"><!-- Header -->
      <header class="page-header">
        <h1>📦 Đơn Hàng Của Tôi</h1>
      </header><!-- Search & Filter -->
      <div class="filter-bar">
        <div class="search-box"><input type="text" id="searchInput"
                                       placeholder="Tìm kiếm theo mã đơn hàng hoặc tên sản phẩm..."> <span
            class="search-icon">🔍</span>
        </div>
      </div><!-- Tabs -->
      <div class="tabs-container">
        <div class="tabs">
          <button class="tab" :class="{ active: activeStatus === null }" data-status="all" @click="getListOrderByUser(null)">
            Tất cả <span class="tab-badge">{{countAll}}</span>
          </button>
          <button class="tab" :class="{ active: activeStatus === 'PENDING' }" @click="getListOrderByUser('PENDING')">
          Chờ xử lý <span class="tab-badge">{{countPending}}</span>
          </button>
          <button class="tab" :class="{ active: activeStatus === 'CONFIRMED' }" @click="getListOrderByUser('CONFIRMED')">
          Đã xác nhận <span class="tab-badge">{{countConfirmed}}</span>
          </button>
          <button class="tab" :class="{ active: activeStatus === 'SHIPPING' }"  @click="getListOrderByUser('SHIPPING')">
          Đang giao <span class="tab-badge">{{countShipping}}</span>
          </button>
          <button class="tab" :class="{ active: activeStatus === 'DELIVERED' }"  @click="getListOrderByUser('DELIVERED')">
          Đã giao <span class="tab-badge">{{countDelivered}}</span>
          </button>
          <button class="tab" :class="{ active: activeStatus === 'COMPLETED' }" @click="getListOrderByUser('COMPLETED')">
          Hoàn thành <span class="tab-badge">{{countCompleted}}</span>
          </button>
          <button class="tab" :class="{ active: activeStatus === 'CANCELLED' }" @click="getListOrderByUser('CANCELLED')">
          Đã hủy <span class="tab-badge">{{countCancelled}}</span>
          </button>
        </div>
      </div><!-- Orders List -->
      <div class="orders-list" id="ordersList"><!-- Order Card 1 -->
        <article class="order-card" data-status="shipping" v-for="p in order" :key="p?.id">
          <div class="order-header">
            <div class="order-shop">
              📦 Đơn hàng: <strong>{{ p.orderCode }}</strong>
            </div>
            <div class="order-status">
              <span class="status-badge status-pending" v-if="p.status === 'PENDING'">⏳ Chờ xử lý</span>
              <span class="status-badge status-confirmed" v-if="p.status === 'CONFIRMED'">📄 Đã xác nhận</span>
              <span class="status-badge status-shipping" v-if="p.status === 'SHIPPING'">🚚 Đang giao</span>
              <span class="status-badge status-delivered" v-if="p.status === 'DELIVERED'">📦 Đã giao</span>
              <span class="status-badge status-completed" v-if="p.status === 'COMPLETED'">✅ Hoàn thành</span>
              <span class="status-badge status-cancelled" v-if="p.status === 'CANCELLED'">❌ Đã hủy</span></div>
          </div>
          <div class="order-item">
            <div class="order-body">
              <div class="order-info-row">
                <span class="label">Ngày đặt:</span>
                <span class="value">{{ p.createdAt }}</span>
              </div>
              <div class="order-info-row">
                <span class="label">Số sản phẩm:</span>
                <span class="value">{{ p.quantity }}</span>
              </div>
              <div class="order-info-row">
                <span class="label">Tổng tiền:</span>
                <span class="value total">{{ formatCurrency(p.totalOrderAmount) }}</span>
              </div>
            </div>

            <div class="order-footer">
              <router-link :to="`/customer/order-detail/${p.order_id}`" class="btn btn-primary">📄 Xem chi tiết</router-link>
            </div>
          </div>
        </article><!-- Order Card 2 -->
      </div><!-- Empty State (hidden by default) -->
      <div class="empty-state" id="emptyState" style="display: none;">
        <div class="empty-icon">
          📦
        </div>
        <div class="empty-title">
          Không tìm thấy đơn hàng
        </div>
        <div class="empty-text">
          Bạn chưa có đơn hàng nào phù hợp với tiêu chí tìm kiếm
        </div>
      </div>
    </div>
  </div>
  <Footer/>
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
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #1a1a2e;
  line-height: 1.6;
}

.page-wrapper {
  width: 100%;
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  width: 100%;
}

/* Header */
.page-header {
  background: white;
  padding: 25px 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.page-header h1 {
  font-size: 2em;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Search & Filter Bar */
.filter-bar {
  background: white;
  padding: 20px 25px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: center;
}

.search-box {
  flex: 1;
  min-width: 250px;
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 12px 45px 12px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 0.95em;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2em;
  color: #999;
}

/* Tabs */
.tabs-container {
  background: white;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.tabs {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  padding: 12px 30px;
  background: transparent;
  border: none;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  font-size: 1em;
}

.tab:hover {
  color: #667eea;
  background: #f9f9ff;
}

.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f9f9ff;
}

.tab-badge {
  display: inline-block;
  background: #ff6b6b;
  color: white;
  font-size: 0.75em;
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: 6px;
  font-weight: 700;
}

.tab.active .tab-badge {
  background: #667eea;
}

/* ===== ORDER CARD CLEAN UI ===== */

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* Card */
.order-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 0;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  transition: 0.25s ease;
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

/* Header */
.order-header {
  padding: 18px 22px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

/* Order Code */
.order-shop {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Status badge group */
.order-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Badges */
.status-badge {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

/* Example active for shipping */
.status-shipping.active {
  display: inline-block;
  background: #e0f2fe;
  color: #0369a1;
}

.status-processing.active {
  display: inline-block;
  background: #e2e8f0;
  color: #334155;
}

.status-pending.active {
  display: inline-block;
  background: #fef9c3;
  color: #854d0e;
}

.status-delivered.active {
  display: inline-block;
  background: #dcfce7;
  color: #166534;
}

.status-cancelled.active {
  display: inline-block;
  background: #fee2e2;
  color: #991b1b;
}

/* Body */
.order-body {
  padding: 18px 22px;
}

.order-info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.order-info-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 14px;
  color: #6b7280;
}

.value {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.value.total {
  font-size: 16px;
  color: #dc2626;
  font-weight: 700;
}

/* Footer */
.order-footer {
  padding: 18px 22px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  text-align: right;
}

.btn-primary {
  background: #2563eb;
  padding: 10px 18px;
  color: white;
  font-size: 14px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.btn-primary:hover {
  background: #1d4ed8;
}

/* Mobile */
@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-info-row {
    font-size: 13px;
  }

  .btn-primary {
    width: 100%;
    text-align: center;
  }
}

</style>