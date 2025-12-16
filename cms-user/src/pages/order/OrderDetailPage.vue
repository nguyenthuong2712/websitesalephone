<script setup lang="ts">
import {ref, onMounted, computed} from 'vue';
import {useRoute} from 'vue-router';
import {orderService} from '@/service/OrderService';
import {formatCurrency} from "@/utils/Constant.ts";
import {OrderRequest} from "@/models/OrderRequest.ts";
import {toast} from "vue3-toastify";

const route = useRoute();

const orderId = ref(route.params.id as string);
const orderDetail = ref<any>(null);
const loading = ref(false);
const error = ref('');
const description = ref('');
const shippingFee = ref(<number>0);
const listHistory = ref<any>();

const TIMELINE_ORDER = [
  {status: 'PENDING', label: 'CHỜ XỬ LÝ', icon: '📝'},
  {status: 'CONFIRMED', label: 'ĐÃ XÁC NHẬN', icon: '✅'},
  {status: 'SHIPPING', label: 'ĐANG GIAO', icon: '📦'},
  {status: 'DELIVERED', label: 'ĐÃ GIAO', icon: '🏠'},
  {status: 'COMPLETED', label: 'HOÀN THÀNH', icon: '⭐'},
  {status: 'CANCELLED', label: 'ĐÃ HỦY', icon: '❌'}
];

/**
 * Tạo computed timeline CHUNG
 * – Luôn dựa theo orderDetail hiện tại
 */
const timeline = computed(() => {
  if (!orderDetail.value) return [];
  return TIMELINE_ORDER.map(step => {
    const match = orderDetail.value.orderHistoryStatusResponses
        ?.find((s: any) => s.status === step.status);
    console.log("match", match)
    return {
      ...step,
      time: match?.createdAt ?? null,
      completed: !!match
    };
  });
});

const fetchOrderDetail = async () => {
  loading.value = true;
  try {
    const res = await orderService.detail(orderId.value);
    orderDetail.value = res.data.data;
  } catch (err: any) {
    error.value = 'Không lấy được chi tiết đơn hàng';
  } finally {
    loading.value = false;
  }
};

const fetchHistory = async () => {
  loading.value = true;
  try {
    const res = await orderService.getListHistory(orderId.value);
    listHistory.value = res.data.data;
  } catch (err: any) {
    error.value = 'Không lấy được chi tiết đơn hàng';
  } finally {
    loading.value = false;
  }
};

const updateOrderStatus = async (newStatus: string) => {
  if (!orderDetail.value) return;

  const requestPayload = new OrderRequest(
      newStatus,
      shippingFee.value,
      description.value,
      orderId.value
  );

  try {
    await orderService.update(requestPayload);
    toast.success("Cập nhật trạng thái thành công");
    await fetchOrderDetail();
    await fetchHistory();
    shippingFee.value = 0;
    description.value = ""
  } catch (err) {
    toast.error("Cập nhật trạng thái thất bại");
  }
};

/**
 * Những computed này mới dùng được timeline
 * vì timeline đã nằm ngoài fetch()
 */
const firstIncompleteIndex = computed(() =>
    timeline.value.findIndex(t => !t.completed)
);

const progressPercent = computed(() => {
  const steps = timeline.value.filter(s => s.status !== 'CANCELLED');
  const total = steps.length;
  const completed = steps.filter(s => s.completed).length;
  return ((completed - 1) / total) * 100;
});

async function handleDownload() {
  try {
    const res = await orderService.downloadPdf(orderId.value);

    const blob = new Blob([res.data], { type: "application/pdf" });
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "file.pdf");
    document.body.appendChild(link);
    link.click();

    window.URL.revokeObjectURL(url);
    link.remove();
  } catch (error) {
    console.error("Lỗi tải PDF:", error);
  }
}

onMounted(() => {
  fetchOrderDetail();
  fetchHistory();
});
</script>
<template>
  <div class="modal fade" id="history" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content shadow-lg history-modal">

        <div class="modal-header border-0 pb-0">
          <h5 class="modal-title fw-bold text-primary">Lịch sử cập nhật</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>

        <div class="modal-body">
          <table class="table table-hover align-middle history-table">
            <thead>
            <tr>
              <th>Trạng thái</th>
              <th>Thời gian</th>
              <th>Người xác nhận</th>
              <th>Ghi chú</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in listHistory" :key="item.time">
              <td>{{ item?.status }}</td>
              <td>{{ item?.time }}</td>
              <td>{{ item?.nameStaff }}</td>
              <td>{{ item?.description }}</td>
            </tr>
            </tbody>
          </table>
        </div>

        <div class="modal-footer border-0">
          <button type="button" class="btn btn-dark px-4" data-bs-dismiss="modal">
            Đóng
          </button>
        </div>

      </div>
    </div>
  </div>
  <div
      class="modal fade"
      id="exampleModal"
      data-bs-backdrop="static" tabindex="-1" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-md">
      <div class="modal-content rounded-4 shadow-lg">
        <!-- Body -->
        <div class="modal-body pt-2">
          <label class="form-label fw-semibold mb-2">Mô tả</label>
          <textarea
              v-model="description"
              class="form-control form-control-lg"
              rows="5"
              placeholder="Nhập ghi chú...">
         </textarea>
        </div>

        <!-- Footer -->
        <div class="modal-footer border-0 pt-3">
          <button
              type="button"
              class="btn btn-outline-secondary rounded-3 px-4"
              data-bs-dismiss="modal"
          >
            Đóng
          </button>
          <button
              type="button"
              class="btn btn-primary rounded-3 px-4 fw-semibold"
              @click="updateOrderStatus('CANCELLED')"
              data-bs-dismiss="modal"
          >
            Lưu
          </button>
        </div>

      </div>
    </div>
  </div>
  <div
      class="modal fade"
      id="exampleModal1"
      tabindex="-1"
      aria-labelledby="exampleModalLabel"
      aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-centered modal-md">
      <div class="modal-content rounded-4 shadow-lg">
        <!-- Body -->
        <div class="modal-body pt-2">
          <label class="form-label fw-semibold mb-2">Mô tả</label>
          <textarea
              v-model="description"
              class="form-control form-control-lg"
              rows="5"
              placeholder="Nhập ghi chú..."
          ></textarea>
          <div v-if="['PENDING'].includes(orderDetail?.status)">
            <label class="form-label fw-semibold mt-3 mb-2">Phí ship</label>
            <input
                v-model="shippingFee"
                type="number"
                class="form-control form-control-lg"
                placeholder="Nhập phí ship..."/>
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer border-0 pt-3">
          <button
              type="button"
              class="btn btn-outline-secondary rounded-3 px-4"
              data-bs-dismiss="modal"
          >
            Đóng
          </button>
          <button
              type="button"
              class="btn btn-primary rounded-3 px-4 fw-semibold"
              data-bs-dismiss="modal"
              @click="updateOrderStatus('')"
          >
            Lưu
          </button>
        </div>

      </div>
    </div>
  </div>
  <header class="order-header">
    <div class="header-left">
      <h1>📦 Chi Tiết Đơn Hàng</h1>
      <div class="order-code">
        Mã đơn hàng: <strong>{{ orderDetail?.orderCode }}</strong>
      </div>
      <div class="order-date">
        Ngày đặt: {{ orderDetail?.createdAt }}
      </div>
    </div>
    <div class="header-right">
      <div>
        <button class="print-btn" @click="handleDownload">🖨️ In đơn hàng</button>
      </div>
    </div>
  </header><!-- Status Timeline -->
  <section class="status-timeline">
    <h2 class="timeline-title">🚚 Trạng Thái Đơn Hàng</h2>
    <div class="timeline-container">
      <div class="timeline-line">
        <div class="timeline-progress"
             v-if="orderDetail?.status !== 'CANCELLED'"
             :style="{ width: progressPercent + '%' }">
        </div>

        <div class="timeline-progress-cancel"
             v-else>
        </div>
      </div>
      <div
          class="timeline-step"
          v-for="(step, idx) in timeline"
          :key="idx"
          :class="{
      completed: step.completed,
      active: !step.completed && idx === firstIncompleteIndex
    }"
      >
        <div class="step-icon">{{ step.icon }}</div>

        <div>
          <div class="step-title">{{ step.label }}</div>
          <div class="step-time">{{ step.time }}</div>
        </div>
      </div>
    </div>
    <div class="actions mt-3 d-flex gap-2">
      <button
          class="btn btn-success"
          data-bs-toggle="modal"
          data-bs-target="#exampleModal1"
          v-if="['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED'].includes(orderDetail?.status)"
      >
        Xác nhận đơn
      </button>

      <button
          class="btn btn-danger"
          data-bs-toggle="modal"
          data-bs-target="#exampleModal"
          v-if="['PENDING', 'CONFIRMED'].includes(orderDetail?.status)"
      >
        Hủy đơn
      </button>

      <button class="btn btn-primary" data-bs-toggle="modal"
              data-bs-target="#history">
        Lịch sử hóa đơn
      </button>
    </div>
  </section><!-- Main Grid -->
  <div class="content-grid">
    <div><!-- Products Table -->
      <section class="products-section">
        <h2 class="section-title">📱 Danh Sách Sản Phẩm</h2>
        <table class="products-table">
          <thead>
          <tr>
            <th>Sản phẩm</th>
            <th>Đơn giá</th>
            <th>Số lượng</th>
            <th>Thành tiền</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="p in orderDetail?.productOrderResponses" :key="p?.id">
            <td>
              <div class="product-cell">
                <div class="product-image-small">
                  <img :src="p.image">
                </div>
                <div class="product-details">
                  <div class="product-name-table">
                    {{ p?.productName }}
                  </div>
                  <div class="product-variant">
                    256GB - Xanh Titan
                  </div>
                </div>
              </div>
            </td>
            <td class="price-cell">{{ formatCurrency(p?.productPrice) }}</td>
            <td class="quantity-cell">{{ p?.quantity }}</td>
            <td class="total-cell">{{ formatCurrency(p?.intoMoney) }}</td>
          </tr>
          </tbody>
        </table>
      </section><!-- IMEI Table -->
    </div><!-- Order Summary Sidebar -->
    <aside class="order-summary-sidebar">
      <div class="summary-section">
        <h3>👤 Thông Tin Khách Hàng</h3>
        <div class="customer-name">
          {{ orderDetail?.fullName }}
        </div>
        <div class="customer-detail">
          📞 {{ orderDetail?.phoneNumber }}
        </div>
        <div class="customer-detail">
          📧 {{ orderDetail?.email }}
        </div>
      </div>
      <div class="summary-section">
        <h3>📍 Địa Chỉ Giao Hàng</h3>
        <div class="customer-detail">
          {{ orderDetail?.address}}
        </div>
      </div>
      <div class="summary-section">
        <h3>💳 Thanh Toán</h3>
        <div class="info-row"><span class="info-label">Phương thức:</span> <span class="info-value">{{ orderDetail?.methodTransaction }}</span>
        </div>
        <div class="info-row"><span class="info-label">Trạng thái:</span> <span class="info-value"
                                                                                style="color: #43e97b;">✓ Đã thanh toán</span>
        </div>
      </div>
      <div class="total-section">
        <div class="total-row"><span class="info-label">Tạm tính:</span> <span class="info-value">{{formatCurrency(orderDetail?.totalPrice)}}</span>
        </div>
        <div class="total-row"><span class="info-label">Phí vận chuyển:</span> <span class="info-value"
                                                                                     style="color: black;">{{formatCurrency(orderDetail?.shippingFee)}}</span>
        </div>
        <div class="total-row"><span class="total-label">Tổng cộng:</span> <span
            class="total-amount">{{formatCurrency(orderDetail?.totalOrderAmount)}}</span>
        </div>
      </div>
    </aside>
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
  padding: 40px 20px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* Header */
.order-header {
  background: white;
  padding: 35px 40px;
  border-radius: 20px;
  margin-bottom: 30px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left h1 {
  font-size: 2.5em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-code {
  font-size: 1.1em;
  color: #667eea;
  font-weight: 700;
}

.order-date {
  color: #666;
  font-size: 0.95em;
  margin-top: 5px;
}

.header-right {
  text-align: right;
}

.status-badge {
  display: inline-block;
  padding: 12px 25px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  border-radius: 12px;
  font-weight: 700;
  font-size: 1.1em;
  margin-bottom: 10px;
}

.print-btn {
  padding: 10px 20px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.print-btn:hover {
  background: #667eea;
  color: white;
}

/* Status Timeline */
.status-timeline {
  background: white;
  padding: 40px;
  border-radius: 20px;
  margin-bottom: 30px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
}

.timeline-title {
  font-size: 1.8em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 35px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.timeline-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  padding: 20px 0;
}

.timeline-line {
  position: absolute;
  top: 55px;
  left: 10%;
  right: 10%;
  height: 4px;
  background: #e0e0e0;
  z-index: 1;
}

.timeline-progress {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%);
  border-radius: 2px;
  transition: width 0.5s ease;
}

.timeline-progress-cancel {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  background: red;
  border-radius: 2px;
  transition: width 0.5s ease;
}

.timeline-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  z-index: 2;
}

.step-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5em;
  margin-bottom: 15px;
  border: 4px solid #e0e0e0;
  transition: all 0.3s ease;
}

.timeline-step.completed .step-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  border-color: #43e97b;
}

.timeline-step.active .step-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  //animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.7);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 15px rgba(102, 126, 234, 0);
  }
}

.step-title {
  font-size: 1.05em;
  font-weight: 700;
  color: #666;
  margin-bottom: 5px;
}

.timeline-step.completed .step-title,
.timeline-step.active .step-title {
  color: #1a1a2e;
}

.step-time {
  font-size: 0.9em;
  color: #999;
}

.timeline-step.completed .step-time,
.timeline-step.active .step-time {
  color: #667eea;
  font-weight: 600;
}

/* Main Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
  align-items: start;
}

/* Products Table */
.products-section {
  background: white;
  border-radius: 20px;
  padding: 35px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.section-title {
  font-size: 1.6em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.products-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  overflow: hidden;
}

.products-table thead {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.products-table th {
  padding: 18px 15px;
  text-align: left;
  color: white;
  font-weight: 700;
  font-size: 0.95em;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.products-table th:first-child {
  border-radius: 12px 0 0 0;
}

.products-table th:last-child {
  border-radius: 0 12px 0 0;
}

.products-table tbody tr {
  border-bottom: 2px solid #f0f0f0;
  transition: all 0.3s ease;
}

.products-table tbody tr:hover {
  background: #f9f9f9;
}

.products-table td {
  padding: 20px 15px;
  vertical-align: middle;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image-small {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  flex-shrink: 0;
}

.product-image-small img {
  width: 110px;
}

.product-details {
  flex: 1;
}

.product-name-table {
  font-weight: 700;
  color: #1a1a2e;
  font-size: 1.05em;
  margin-bottom: 4px;
}

.product-variant {
  font-size: 0.9em;
  color: #666;
}

.price-cell {
  font-weight: 700;
  color: #667eea;
  font-size: 1.1em;
}

.quantity-cell {
  text-align: center;
  font-weight: 700;
  color: #1a1a2e;
  font-size: 1.05em;
}

.total-cell {
  font-weight: 800;
  color: #1a1a2e;
  font-size: 1.2em;
}

/* IMEI Table */
.imei-section {
  background: white;
  border-radius: 20px;
  padding: 35px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
}

.imei-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  overflow: hidden;
}

.imei-table thead {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.imei-table th {
  padding: 16px 15px;
  text-align: left;
  color: white;
  font-weight: 700;
  font-size: 0.9em;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.imei-table th:first-child {
  border-radius: 12px 0 0 0;
}

.imei-table th:last-child {
  border-radius: 0 12px 0 0;
}

.imei-table tbody tr {
  border-bottom: 2px solid #f0f0f0;
  transition: all 0.3s ease;
}

.imei-table tbody tr:hover {
  background: #fff5f5;
}

.imei-table td {
  padding: 16px 15px;
  vertical-align: middle;
}

.imei-code {
  font-family: 'Courier New', monospace;
  font-weight: 700;
  color: #1a1a2e;
  font-size: 1em;
  background: #f9f9f9;
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
}

.imei-status {
  display: inline-block;
  padding: 6px 14px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.85em;
}

/* Order Summary Sidebar */
.order-summary-sidebar {
  background: white;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 20px;
}

.summary-section {
  margin-bottom: 25px;
  padding-bottom: 25px;
  border-bottom: 2px solid #f0f0f0;
}

.summary-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.summary-section h3 {
  font-size: 1.2em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 0.95em;
}

.info-label {
  color: #666;
  font-weight: 600;
}

.info-value {
  color: #1a1a2e;
  font-weight: 700;
  text-align: right;
}

.customer-name {
  font-size: 1.1em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.customer-detail {
  color: #666;
  margin-bottom: 6px;
  font-size: 0.95em;
}

.total-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  border-radius: 12px;
  margin-top: 20px;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.total-row:last-child {
  margin-bottom: 0;
  padding-top: 15px;
  border-top: 2px solid rgba(255, 255, 255, 0.5);
}

.total-label {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
}

.total-amount {
  font-size: 1.8em;
  font-weight: 600;
  background: black;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 25px;
}

.btn-action {
  padding: 14px 20px;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 1em;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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
  background: #667eea;
  color: white;
}

/* Responsive */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .order-summary-sidebar {
    position: relative;
  }
}

@media (max-width: 968px) {
  .timeline-container {
    flex-direction: column;
    gap: 30px;
  }

  .timeline-line {
    display: none;
  }

  .timeline-step {
    flex-direction: row;
    width: 100%;
    text-align: left;
    gap: 20px;
  }

  .step-icon {
    width: 70px;
    height: 70px;
    font-size: 2em;
    flex-shrink: 0;
  }

  .products-table,
  .imei-table {
    font-size: 0.9em;
  }

  .products-table th,
  .products-table td,
  .imei-table th,
  .imei-table td {
    padding: 12px 10px;
  }
}

@media (max-width: 640px) {
  body {
    padding: 20px 10px;
  }

  .order-header {
    padding: 25px 20px;
  }

  .order-header h1 {
    font-size: 1.8em;
  }

  .status-timeline,
  .products-section,
  .imei-section,
  .order-summary-sidebar {
    padding: 25px 20px;
  }

  .product-cell {
    flex-direction: column;
    align-items: flex-start;
  }

  .products-table,
  .imei-table {
    display: block;
    overflow-x: auto;
  }
}
</style>