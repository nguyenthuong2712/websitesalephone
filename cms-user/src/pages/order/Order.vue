<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import {orderService} from "@/service/OrderService";
import { Search } from '@/models/Search';
import { formatCurrency } from '@/utils/Constant';

const orders = ref<any>();
const page = ref<number>(1);
const size = ref<number>(10);
const totalPages = ref<number>(1);
const activeStatus = ref('');
const searchText = ref('');

const countAll = ref(0);
const countPending = ref(0);
const countConfirmed = ref(0);
const countShipping = ref(0);
const countDelivered = ref(0);
const countCompleted = ref(0);
const countCancelled = ref(0);

type OrderStatus = 'ALL' | 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'COMPLETED' | 'CANCELLED'

const countMap: Record<OrderStatus, typeof countAll> = {
  ALL: countAll,
  PENDING: countPending,
  CONFIRMED: countConfirmed,
  SHIPPING: countShipping,
  DELIVERED: countDelivered,
  COMPLETED: countCompleted,
  CANCELLED: countCancelled,
};

const countOrderByStaff = async (status: OrderStatus) => {
  const req = {
    userId: '',
    status: status
  };

  try {
    const res = await orderService.countOrderByStaff(req);
    countMap[status].value = Number(res.data.data ?? 0);
  } catch (err) {
    console.error('Count order by staff error', err);
  }
};

const callSearch = async (status: '' | OrderStatus) => {
  activeStatus.value = status;
  const search = new Search(page.value, size.value, searchText.value, status);
  const res = await orderService.search(search);
  orders.value = res.data.data;
  totalPages.value = Number(res.data.data?.totalPages ?? 1);
};

const onPageChange = (newPage: number) => {
  newPage = Number(newPage);
  if (newPage < 1) {
    newPage = 1;
  }

  if (newPage > totalPages.value) {
    newPage = totalPages.value;
  }

  if (newPage === page.value) {
    return;
  }

  page.value = newPage;
  callSearch('');
};

onMounted(async () => {
  await callSearch('');
  await countOrderByStaff("ALL");
  await countOrderByStaff("PENDING");
  await countOrderByStaff("CONFIRMED");
  await countOrderByStaff("SHIPPING");
  await countOrderByStaff("DELIVERED");
  await countOrderByStaff("COMPLETED");
  await countOrderByStaff("CANCELLED");
});

watch(searchText, () => {
  page.value = 1;
  callSearch('');
});
</script>

<template>
  <section class="content-card" id="orders">
    <div class="row">
      <div class="col-lg-4"><h2 class="card-title">📦 Quản Lý Đơn Hàng</h2></div>
      <div class="col-lg-6"><input type="text" v-model="searchText" placeholder="Tìm kiếm ... (Mã DH, SDT, Tên Khách)"
                                   class="input-search"/>
      </div>
    </div>
    <div class="tabs">
      <button
          class="tab"
          :class="{ active: activeStatus === '' }"
          @click="callSearch('')"
      >
        Tất Cả <span class="tab-badge">{{countAll}}</span>
      </button>
      <button
          class="tab"
          :class="{ active: activeStatus === 'PENDING' }"
          @click="callSearch('PENDING')"
      >
        Chờ Xử Lý <span class="tab-badge">{{countPending}}</span>
      </button>
      <button
          class="tab"
          :class="{ active: activeStatus === 'CONFIRMED' }"
          @click="callSearch('CONFIRMED')"
      >
        Đã xác nhận <span class="tab-badge">{{countConfirmed}}</span>
      </button>
      <button
          class="tab"
          :class="{ active: activeStatus === 'SHIPPING' }"
          @click="callSearch('SHIPPING')"
      >
        Đang giao hàng <span class="tab-badge">{{countShipping}}</span>
      </button>
      <button
          class="tab"
          :class="{ active: activeStatus === 'DELIVERED' }"
          @click="callSearch('DELIVERED')"
      >
        Đã giao <span class="tab-badge">{{countDelivered}}</span>
      </button>
      <button
          class="tab"
          :class="{ active: activeStatus === 'COMPLETED' }"
          @click="callSearch('COMPLETED')"
      >
        Hoàn thành <span class="tab-badge">{{countCompleted}}</span>
      </button>
      <button
          class="tab"
          :class="{ active: activeStatus === 'CANCELLED' }"
          @click="callSearch('CANCELLED')"
      >
        Đã Hủy <span class="tab-badge">{{countCancelled}}</span>
      </button>
    </div>
    <div class="table-wrapper">
      <table>
        <thead>
        <tr>
          <th>Mã ĐH</th>
          <th>Tên Khách</th>
          <th>SĐT Khách</th>
          <th>Mã nhân viên</th>
          <th>Ngày tạo</th>
          <th>Ngày thanh toán</th>
          <th>Trạng Thái</th>
          <th>Số lượng</th>
          <th>Tổng tiền sản phẩm</th>
          <th>Phí vận chuyển</th>
          <th>Tổng tiền hóa đơn</th>
          <th class="sticky-col">Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="o in orders?.content" :key="o.order_id">
          <td>{{ o.orderCode }}</td>
          <td>{{ o.userName }}</td>
          <td>{{ o.phone }}</td>
          <td>{{ o.codeStaff }}</td>
          <td>{{ o.createdAt }}</td>
          <td>{{ o.dateTimeCheckout }}</td>
          <td>{{ o.status }}</td>
          <td>{{ o.quantity }}</td>
          <td>{{ formatCurrency(o.totalPrice) }}</td>
          <td>{{ formatCurrency(o.shippingFee) }}</td>
          <td>{{ formatCurrency(o.totalOrderAmount) }}</td>
          <td class="sticky-col">
            <router-link :to="`/admin/orders-detail/${o.order_id}`" class="action-btn btn-edit">✏️</router-link>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
    <div class="pagination">
      <button class="page-btn" :disabled="page === 1" @click="onPageChange(page - 1)">«</button>
      <button
          class="page-btn active"
      >
        {{ page }} / {{ totalPages + 1 }}
      </button>
      <button class="page-btn" :disabled="page >= totalPages" @click="onPageChange(page + 1)">»</button>
    </div>
  </section><!-- Products Table -->
</template>

<style scoped>
/* Reset */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

body {
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #1a1a2e;
  line-height: 1.6;
}

/* Layout */
.row {
  padding: 20px 5px;
}

.nav-menu li {
  margin-bottom: 5px;
}

.nav-menu a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-weight: 600;
  font-size: 1.05em;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.nav-menu a:hover,
.nav-menu a.active {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-left-color: #fee140;
}

.breadcrumb a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

/* Input */
.input-search {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.25s ease;
  background-color: #fff;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 10px;
  padding: 25px 30px 0;
  border-bottom: 2px solid #f0f0f0;
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

.tab:hover,
.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
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

/* Card */
.content-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  overflow: hidden;
}

.card-title {
  font-size: 1.6em;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Table Wrapper */
.table-wrapper {
  overflow-x: auto;
  width: 100%;
  position: relative;
}

.table-wrapper::-webkit-scrollbar {
  height: 6px;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 10px;
}

/* Sticky Column */
th.sticky-col,
td.sticky-col {
  position: sticky;
  right: 0;
  background: white;
  z-index: 20;
}

th.sticky-col {
  background-color: #e2f0ff !important;
  font-weight: 700;
  color: #003e7e;
}

td.sticky-col {
  background-color: #f5faff;
  border-left: 2px solid #d0e7ff;
  text-align: center;
}

tbody tr:hover td.sticky-col {
  background-color: #eaf4ff;
}

/* Table */
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  table-layout: auto;
}

thead {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

th, td {
  white-space: nowrap;
  padding: 15px 20px;
  text-align: center;
  border-right: 1px solid #e5e7eb;
}

th {
  font-weight: 700;
  color: #1a1a2e;
  font-size: 1em;
  border-bottom: 2px solid #e0e0e0;
  padding: 20px 25px;
}

td {
  border-bottom: 1px solid #f0f0f0;
  font-size: 0.98em;
  padding: 20px 25px;
}

td:last-child,
th:last-child {
  border-right: none;
}

/* Hover row */
tbody tr {
  transition: all 0.3s ease;
}

tbody tr:hover {
  background: #f9f9f9;
  transform: scale(1.01);
}

/* Action Buttons */
.action-btn {
  width: 35px;
  height: 35px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.1em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  transform: scale(1.15);
}

.btn-edit {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 30px;
}

.page-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #f0f0f0;
  color: #1a1a2e;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover,
.page-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
}

.page-btn:disabled,
.page-btn:disabled:hover {
  background: #ddd !important;
  color: #888 !important;
  cursor: not-allowed !important;
  transform: none !important;
}
</style>
