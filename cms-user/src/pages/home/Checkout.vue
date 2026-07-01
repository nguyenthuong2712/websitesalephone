<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import { orderService } from '@/service/OrderService';
import { paymentService } from '@/service/PaymentService';
import { useCartStore } from '@/cartStore';
import { toast } from 'vue3-toastify';
import { formatCurrency } from "@/utils/Constant";

const router = useRouter();
const cartStore = useCartStore();

const address = ref<string>("");
const paymentMethod = ref<string>("COD");
const loading = ref(false);
const buyNowItem = ref<any>(null);

const fetchBuyNowItem = () => {
  const itemStr = localStorage.getItem('buy_now_item');
  if (itemStr) {
    try {
      buyNowItem.value = JSON.parse(itemStr);
    } catch (e) {
      console.error("Lỗi parse buyNowItem", e);
      buyNowItem.value = null;
    }
  }
};

onMounted(() => {
  fetchBuyNowItem();
});

const subtotal = computed(() => {
  if (!buyNowItem.value) return 0;
  return buyNowItem.value.quantity * Number(buyNowItem.value.price);
});

const isDisable = (): boolean => {
  if (!buyNowItem.value) return true;
  return address.value === null || address.value.trim() === '';
};

const handleCheckout = async () => {
  if (isDisable()) return;

  loading.value = true;
  try {
    const payload = {
      variantId: buyNowItem.value.variantId,
      quantity: buyNowItem.value.quantity,
      addressLine: address.value.trim(),
      paymentMethod: paymentMethod.value
    };

    const res = await orderService.buyNow(payload);
    if (res.data.code !== 0) {
      toast.error(res.data.message || 'Đặt hàng thất bại');
      return;
    }

    const createdOrder = res.data.data;

    // Clear buy now state from localStorage
    localStorage.removeItem('buy_now_item');

    // Sync cart store count just in case
    await cartStore.fetchCartCount();

    if (paymentMethod.value === 'PAYOS') {
      toast.info('Đang chuyển hướng sang cổng thanh toán PayOS...');
      const paymentRes = await paymentService.createPayment(createdOrder.order_id);
      if (paymentRes.data.code === 0) {
        window.location.href = paymentRes.data.data;
      } else {
        toast.error(paymentRes.data.message || 'Không thể tạo link thanh toán PayOS');
        router.push({ name: 'OrderOfUSer' });
      }
    } else {
      toast.success('Đặt hàng thành công!');
      router.push({ name: 'OrderOfUSer' });
    }
  } catch (err: any) {
    console.error('Checkout error', err);
    toast.error(err?.response?.data?.message || 'Đặt hàng thất bại');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <HomeLayout/>
  <div class="checkout-page-wrapper">
    <div class="container">

      <header class="checkout-header">
        <div class="header-left">
          <h1>💳 Thanh Toán Mua Ngay</h1>
          <div class="breadcrumb">
            <router-link to="/customer/product-home">Sản Phẩm</router-link>
            / Thanh toán
          </div>
        </div>
      </header>

      <div class="checkout-grid" v-if="buyNowItem">
        <!-- Products list -->
        <section class="checkout-items">
          <div class="checkout-items-header">
            <h2 class="checkout-items-title">📦 Sản Phẩm Thanh Toán</h2>
          </div>

          <article class="checkout-item">
            <div class="item-image">
              <img :src="buyNowItem.image || '/placeholder.png'" alt="product"/>
            </div>
            <div class="item-details">
              <h3 class="item-name">{{ buyNowItem.productName }}</h3>
              <div class="item-specs">
                <span class="spec-badge" v-if="buyNowItem.ram">{{ buyNowItem.ram }}</span>
                <span class="spec-badge" v-if="buyNowItem.color">{{ buyNowItem.color }}</span>
                <span class="spec-badge" v-if="buyNowItem.origin">{{ buyNowItem.origin }}</span>
              </div>
              <div class="item-price-qty">
                <span class="item-price">{{ formatCurrency(buyNowItem.price) }}</span>
                <span class="item-qty">x{{ buyNowItem.quantity }}</span>
              </div>
            </div>
          </article>
        </section>

        <!-- Order Summary & Shipping Form -->
        <aside class="order-summary">
          <div class="address-row">
            <label for="address" class="summary-label">Địa chỉ nhận hàng <span class="required">*</span></label>
            <input
                id="address"
                v-model="address"
                type="text"
                class="address-input"
                placeholder="Nhập địa chỉ giao hàng..."
                required
            />
          </div>

          <h2 class="summary-title">📋 Tóm Tắt Đơn Hàng</h2>
          <div class="summary-row">
            <span class="summary-label">Tạm tính ({{ buyNowItem.quantity }} sản phẩm)</span>
            <span class="summary-value">{{ formatCurrency(subtotal) }}</span>
          </div>

          <div class="summary-row payment-row">
            <span class="summary-label payment-title">Hình thức thanh toán</span>
            <div class="payment-methods">
              <label class="method-label">
                <input type="radio" v-model="paymentMethod" value="COD" />
                <span>💵 Thanh toán khi nhận hàng (COD)</span>
              </label>
              <label class="method-label">
                <input type="radio" v-model="paymentMethod" value="PAYOS" />
                <span>💳 Thanh toán trực tuyến qua PayOS (VietQR)</span>
              </label>
            </div>
          </div>

          <div class="summary-row">
            <span class="summary-label">Phí vận chuyển</span>
            <span class="summary-value shipping-status">Chờ admin duyệt đơn</span>
          </div>

          <div class="summary-row summary-total">
            <span class="total-label">Tổng cộng</span>
            <span class="total-value">{{ formatCurrency(subtotal) }}</span>
          </div>

          <button class="btn-checkout" @click="handleCheckout" :disabled="isDisable() || loading">
            <span v-if="loading">Đang xử lý...</span>
            <span v-else>Confirm &amp; Đặt hàng</span>
          </button>
        </aside>
      </div>

      <div class="empty-checkout" v-else>
        <div class="empty-icon">💳</div>
        <h2 class="empty-title">Không tìm thấy thông tin mua ngay</h2>
        <p class="empty-text">Vui lòng quay lại cửa hàng chọn sản phẩm để thực hiện mua ngay.</p>
        <router-link to="/customer/product-home" class="btn-shop">Quay lại mua sắm</router-link>
      </div>

    </div>
  </div>
  <Footer/>
</template>

<style scoped>
.checkout-page-wrapper {
  background-color: #f8f9fc;
  min-height: 80vh;
  padding: 40px 20px 80px 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.checkout-header {
  margin-bottom: 30px;
}

.checkout-header h1 {
  font-size: 2.2em;
  font-weight: 800;
  color: #1a1a2e;
}

.breadcrumb {
  font-size: 0.9em;
  color: #666;
  margin-top: 5px;
}

.breadcrumb a {
  color: #7c3aed;
  text-decoration: none;
  font-weight: 600;
}

/* Grid Layout */
.checkout-grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
  align-items: start;
}

.checkout-items {
  background: white;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  padding: 30px;
}

.checkout-items-header {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.checkout-items-title {
  font-size: 1.4em;
  font-weight: 700;
  color: #1a1a2e;
}

/* Item */
.checkout-item {
  display: flex;
  gap: 20px;
  align-items: center;
  padding: 15px 0;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9fc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

.item-details {
  flex: 1;
}

.item-name {
  font-size: 1.2em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.item-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.spec-badge {
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 0.8em;
  color: #4b5563;
  font-weight: 600;
}

.item-price-qty {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  font-size: 1.25em;
  font-weight: 800;
  color: #7c3aed;
}

.item-qty {
  font-size: 1.05em;
  font-weight: 700;
  color: #4b5563;
}

/* Order Summary */
.order-summary {
  background: white;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.05);
  padding: 30px;
  position: sticky;
  top: 20px;
}

.address-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 25px;
}

.summary-label {
  font-weight: 700;
  font-size: 0.95em;
  color: #4b5563;
  margin-bottom: 8px;
}

.required {
  color: #ef4444;
}

.address-input {
  padding: 12px;
  border-radius: 10px;
  border: 1.5px solid #cbd5e1;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.address-input:focus {
  border-color: #7c3aed;
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.08);
}

.summary-title {
  font-size: 1.4em;
  font-weight: 700;
  color: #1a1a2e;
  margin: 10px 0 20px 0;
  border-top: 1.5px solid #f1f5f9;
  padding-top: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.95em;
}

.summary-value {
  font-weight: 700;
  color: #1a1a2e;
}

.payment-row {
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  border-top: 1.5px solid #f1f5f9;
  border-bottom: 1.5px solid #f1f5f9;
  padding: 15px 0;
}

.payment-title {
  font-weight: bold;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.method-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  width: 100%;
  font-weight: normal;
}

.shipping-status {
  color: #10b981;
}

.summary-total {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 2px solid #f1f5f9;
}

.total-label {
  font-size: 1.25em;
  font-weight: 800;
  color: #1a1a2e;
}

.total-value {
  font-size: 1.6em;
  font-weight: 800;
  color: #7c3aed;
}

.btn-checkout {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-weight: 700;
  font-size: 1.1em;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 20px;
}

.btn-checkout:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.btn-checkout:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
}

/* Empty Checkout */
.empty-checkout {
  padding: 80px 20px;
  text-align: center;
  background: white;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.05);
}

.empty-icon {
  font-size: 72px;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 1.8em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 10px;
}

.empty-text {
  color: #64748b;
  margin-bottom: 30px;
}

.btn-shop {
  display: inline-block;
  padding: 14px 35px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 700;
  transition: all 0.3s ease;
}

.btn-shop:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

@media (max-width: 968px) {
  .checkout-grid {
    grid-template-columns: 1fr;
  }
}
</style>
