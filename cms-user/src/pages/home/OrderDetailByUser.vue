<script setup lang="ts">
import Footer from "../../layout/Footer.vue";
import Header from "../../layout/Header.vue";
import {useRoute} from "vue-router";
import {computed, onMounted, ref} from "vue";
import {orderService} from "@/service/OrderService.ts";
import {formatCurrency, getContrastColor} from "../../utils/Constant.ts";
import {OrderRequest} from "@/models/OrderRequest.ts";
import {toast} from "vue3-toastify";

const route = useRoute();
const orderDetail = ref<any>(null);
const orderId = ref(route.params.id as string);
const description = ref('');

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

const firstIncompleteIndex = computed(() =>
    timeline.value.findIndex(t => !t.completed)
);

const progressPercent = computed(() => {
  const steps = timeline.value.filter(s => s.status !== 'CANCELLED');
  const total = steps.length;
  const completed = steps.filter(s => s.completed).length;
  return ((completed - 1) / total) * 100;
});

const fetchOrderDetail = async () => {
  try {
    const res = await orderService.detail(orderId.value);
    orderDetail.value = res.data.data;
  } catch (err: any) {
    error.value = 'Không lấy được chi tiết đơn hàng';
  }
};

const updateOrderStatus = async (newStatus: string) => {
  if (!orderDetail.value) return;

  const requestPayload = new OrderRequest(
      newStatus,
      0,
      description.value,
      orderId.value
  );

  try {
    await orderService.update(requestPayload);
    toast.success("Cập nhật trạng thái thành công");
    await fetchOrderDetail();
  } catch (err) {
    toast.error("Cập nhật trạng thái thất bại");
  }
}

onMounted(() => {
  fetchOrderDetail();
});
</script>

<template>
  <Header/>
  <div class="page-wrapper">
    <div class="container"><!-- Header -->
      <header class="page-header">
        <div class="header-left">
          <h1>📋 Chi Tiết Đơn Hàng</h1>
          <div class="order-id">
            Mã đơn hàng: {{ orderDetail?.orderCode }}
          </div>
        </div>
        <router-link to="/customer/order-by-user" class="back-btn">← Quay lại</router-link>
      </header><!-- Order Status Timeline -->
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
      <section class="timeline-card">
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
        <div class="col-md-3">
          <button
              class="btn btn-danger"
              data-bs-toggle="modal"
              data-bs-target="#exampleModal"
              v-if="['PENDING', 'CONFIRMED'].includes(orderDetail?.status)"
          >
            Hủy đơn
          </button>
        </div>
      </section><!-- Main Content Grid -->
      <div class="content-grid"><!-- Left Column: Order Details -->
        <div>
          <article class="info-card">
            <h2 class="card-title">🛒 Thông Tin Đơn Hàng</h2>
            <div class="product-list">
              <div class="product-item" v-for="p in orderDetail?.productOrderResponses" :key="p?.id">
                <div class="product-image">
                  <img :src="p.image">
                </div>
                <div class="product-details">
                  <div class="product-name">
                    {{ p?.productName }}
                  </div>
                  <div class="product-variant">
                    Phân loại: {{ p?.ram }} | <span class="spec-badge"
                                                    :style="{ backgroundColor: p?.color, color: getContrastColor(p.color) }">
              </span> | {{p?.screen}} | {{p?.camera}}
                  </div>
                  <div class="product-quantity">
                    Số lượng: x{{ p?.quantity }}
                  </div>
                </div>
                <div class="product-price">
                  {{ formatCurrency(p?.intoMoney) }}
                </div>
              </div>
            </div>
            <div class="price-summary">
              <div class="price-row"><span class="price-label">Tạm tính</span> <span
                  class="price-value">{{ formatCurrency(orderDetail?.totalPrice) }}</span>
              </div>
              <div class="price-row"><span class="price-label">Phí vận chuyển</span> <span
                  class="price-value">{{ formatCurrency(orderDetail?.shippingFee) }}</span>
              </div>
              <div class="price-row price-total"><span class="price-label">Tổng thanh toán</span> <span
                  class="price-value">{{ formatCurrency(orderDetail?.totalOrderAmount) }}</span>
              </div>
            </div>
          </article>
        </div><!-- Right Column: Shipping & Payment Info -->
        <div>
          <article class="info-card">
            <h2 class="card-title">📍 Thông Tin Khác</h2><!-- Shipping Info -->
            <div class="info-section">
              <h3 class="section-subtitle">🚚 Thông tin giao hàng</h3>
              <div class="info-row"><span class="info-label">Người nhận:</span> <span
                  class="info-value">{{ orderDetail?.fullName }}</span>
              </div>
              <div class="info-row"><span class="info-label">Số điện thoại:</span> <span
                  class="info-value">{{ orderDetail?.phoneNumber }}</span>
              </div>
              <div class="info-row"><span class="info-label">Địa chỉ:</span> <span
                  class="info-value">{{ orderDetail?.address }}</span>
              </div>
            </div><!-- Delivery Info -->
            <div class="info-section">
              <h3 class="section-subtitle">💳 Thanh toán</h3>
              <div class="info-row"><span class="info-label">Phương thức:</span> <span
                  class="payment-badge">Tiền mặt</span>
              </div>
              <div class="info-row"><span class="info-label">Hình thức:</span> <span class="info-value">Thanh toán khi nhận hàng</span>
              </div>
            </div><!-- Notes -->
            <div class="info-section">
              <h3 class="section-subtitle">📝 Ghi chú</h3>
              <div class="info-row"><span
                  class="info-value">Giao hàng giờ hành chính. Vui lòng gọi trước 15 phút.</span>
              </div>
            </div>
          </article>
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
.spec-badge {
  padding: 5px 12px;
  background: #f0f0f0;
  border-radius: 8px;
  font-size: 0.85em;
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
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.header-left h1 {
  font-size: 1.8em;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.order-id {
  font-size: 0.9em;
  color: #667eea;
  font-weight: 600;
}

.back-btn {
  padding: 12px 24px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn:hover {
  background: #667eea;
  color: white;
}

/* Order Status Timeline */
.timeline-card {
  background: white;
  border-radius: 12px;
  padding: 35px 30px;
  margin-bottom: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.timeline-title {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  gap: 10px;
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
    box-shadow: 0 4px 12px rgba(67, 233, 123, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 6px 20px rgba(67, 233, 123, 0.5);
  }
}

.step-label {
  font-weight: 700;
  font-size: 0.9em;
  color: #666;
  text-align: center;
  margin-bottom: 6px;
}

.timeline-step.completed .step-label,
.timeline-step.active .step-label {
  color: #1a1a2e;
}

.step-time {
  font-size: 0.8em;
  color: #999;
  text-align: center;
}

.timeline-step.completed .step-time,
.timeline-step.active .step-time {
  color: #667eea;
  font-weight: 600;
}

/* Main Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 25px;
  margin-bottom: 25px;
}

/* Order Info Card */
.info-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card-title {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 15px;
  border-bottom: 3px solid #f0f0f0;
}

/* Product Items */
.product-list {
  margin-bottom: 25px;
}

.product-item {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #fafafa;
  border-radius: 10px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.product-item:hover {
  background: #f5f5f5;
  transform: translateX(5px);
}

.product-image {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5em;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.product-image img {
  width: 110px;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-weight: 700;
  font-size: 1.05em;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.product-variant {
  color: #666;
  font-size: 0.9em;
  margin-bottom: 6px;
}

.product-quantity {
  color: #999;
  font-size: 0.85em;
}

.product-price {
  text-align: right;
  font-size: 1.2em;
  font-weight: 800;
  color: #667eea;
  flex-shrink: 0;
}

/* Price Summary */
.price-summary {
  border-top: 2px solid #f0f0f0;
  padding-top: 20px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 0.95em;
}

.price-label {
  color: #666;
}

.price-value {
  font-weight: 700;
  color: #1a1a2e;
}

.price-discount {
  color: #43e97b;
}

.price-total {
  border-top: 2px dashed #e0e0e0;
  padding-top: 15px;
  margin-top: 15px;
}

.price-total .price-label {
  font-size: 1.1em;
  font-weight: 700;
  color: #1a1a2e;
}

.price-total .price-value {
  font-size: 1.5em;
  color: #ff6b6b;
}

/* Shipping Info */
.info-section {
  margin-bottom: 25px;
  padding-bottom: 25px;
  border-bottom: 2px solid #f0f0f0;
}

.info-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-subtitle {
  font-weight: 700;
  font-size: 1em;
  color: #1a1a2e;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 0.95em;
}

.info-label {
  color: #666;
  min-width: 120px;
  flex-shrink: 0;
}

.info-value {
  color: #1a1a2e;
  font-weight: 600;
}

.payment-badge {
  display: inline-block;
  padding: 6px 14px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.85em;
}

.shipping-code {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: #f0f0f0;
  border-radius: 8px;
  font-weight: 700;
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
}

.shipping-code:hover {
  background: #667eea;
  color: white;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 25px;
}

.btn {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  font-size: 0.95em;
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

.btn-outline {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
}

/* Responsive */
@media (max-width: 968px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .timeline {
    flex-wrap: wrap;
    padding: 0;
  }

  .timeline::before,
  .timeline-progress {
    display: none;
  }

  .timeline-step {
    flex-basis: 50%;
    margin-bottom: 25px;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 20px 10px;
  }

  .page-header {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 1.4em;
  }

  .timeline-card,
  .info-card {
    padding: 20px;
  }

  .product-item {
    flex-direction: column;
  }

  .product-price {
    text-align: left;
  }

  .action-buttons {
    flex-direction: column;
  }

  .info-row {
    flex-direction: column;
    gap: 6px;
  }

  .info-label {
    min-width: auto;
  }
}
</style>