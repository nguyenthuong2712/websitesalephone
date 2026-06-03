<script setup lang="ts">
import {ref, onMounted, computed} from "vue";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import { cartService } from '@/service/CartService';
import type { CartResponse, ProductInCart } from '@/models/Cart';
import { CartRequest } from '@/models/CartRequest';
import { toast } from 'vue3-toastify';
import { CheckOutRequest } from '@/models/CheckOutRequest';
import { paymentService } from '@/service/PaymentService';

type CartItemWithSelect = ProductInCart & { selected: boolean };
const cartItems = ref<CartItemWithSelect[]>([]);
const loading = ref(false);
const search = {};
const address = ref<string>("");
const paymentMethod = ref<string>("COD");

const fetchCartItems = async () => {
  loading.value = true;
  try {
    const response = await cartService.getCartItems(search);
    const cart: CartResponse = response.data.data;
    cartItems.value = cart.products.map(item => ({...item, selected: true}));
  } catch (err) {
    console.error("Fetch cart error", err);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchCartItems);

const totalQuantity = computed(() =>
    cartItems.value.filter(i => i.selected).reduce((sum, i) => sum + i.quantity, 0)
);

const subtotal = computed(() =>
    cartItems.value.filter(i => i.selected).reduce((sum, i) => sum + i.quantity * Number(i.price), 0)
);

const allSelected = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(i => i.selected),
  set: (val: boolean) => {
    cartItems.value.forEach(i => (i.selected = val));
  }
});

const increaseQty = async (item: CartItemWithSelect) => {
  item.quantity++;
  try {
    const res = await cartService.updateCartItem(new CartRequest(item.idCartItem, item.productId, item.quantity).toPayload());
    if (res.data.code === 2) {
      toast.error(res.data.message)
    } else {
      toast.success(res.data.message)
    }

  } catch (err) {
    console.error('Update cart error', err)
    toast.error('Cập nhật giỏ hàng thất bại')
  }
};

const decreaseQty = async (item: CartItemWithSelect) => {
  if (item.quantity > 1) {
    item.quantity--;
    try {
      const res = await cartService.updateCartItem(new CartRequest(item.idCartItem, item.productId, item.quantity).toPayload());
      if (res.data.code === 2) {
        toast.error(res.data.message)
      } else {
        toast.success(res.data.message)
      }
    } catch (err) {
      console.error("Update cart error", err);
    }
  }
};

const removeItem = async (item: CartItemWithSelect) => {
  try {
    const res = await cartService.updateCartItem(new CartRequest(item.idCartItem, item.productId, 0).toPayload());
    if (res.data.code === 2) {
      toast.error(res.data.message)
    } else {
      toast.success(res.data.message)
    }
    cartItems.value = cartItems.value.filter(i => i.productId !== item.productId);
  } catch (err) {
    console.error("Remove cart item error", err);
  }
};

const checkout = async () => {
  const payload = new CheckOutRequest(
      address.value
  );
  try {
    const res = await cartService.checkoutCart(payload.toPayload());
    if (res.data.code === 2) {
      toast.error(res.data.message);
      return;
    }

    const createdOrder = res.data.data;
    if (paymentMethod.value === 'VNPAY') {
      toast.info('Đang chuyển hướng sang cổng thanh toán VNPAY...');
      const paymentRes = await paymentService.createPayment(createdOrder.order_id);
      if (paymentRes.data.code === 0) {
        window.location.href = paymentRes.data.data;
      } else {
        toast.error(paymentRes.data.message || 'Không thể tạo link thanh toán VNPAY');
      }
    } else {
      toast.success('Đặt hàng thành công!');
      await fetchCartItems();
    }
  } catch (err) {
    console.error('Checkout error', err);
    toast.error('Đặt hàng thất bại');
  }
};

const cartColorMap: Record<string, string> = {
  BLACK: '#000000',
  WHITE: '#FFFFFF',
  RED: '#FF0000',
  GREEN: '#00A651',
  BLUE: '#0000FF',
  YELLOW: '#FFFF00',
  ORANGE: '#FFA500',
  PURPLE: '#800080',
  PINK: '#FFC0CB',
  BROWN: '#8B4513',
  GREY: '#808080',
  GRAY: '#808080',
  SILVER: '#C0C0C0',
  GOLD: '#FFD700',
  DEN: '#000000',
  TRANG: '#FFFFFF',
  DO: '#FF0000',
  XANH: '#0000FF',
  XANH_DUONG: '#0000FF',
  XANH_LA: '#00A651',
  VANG: '#FFD700',
  CAM: '#FFA500',
  TIM: '#800080',
  HONG: '#FFC0CB',
  NAU: '#8B4513',
  XAM: '#808080',
  BAC: '#C0C0C0',
}

const resolveCartColorHex = (colorName?: string): string => {
  if (!colorName) return '#ccc'

  const normalized = colorName.trim()
  if (normalized.startsWith('#') || normalized.startsWith('rgb') || normalized.startsWith('hsl')) {
    return normalized
  }

  const key = normalized
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/\s+/g, '_')
    .replace(/-/g, '_')
    .toUpperCase()

  return cartColorMap[key] ?? normalized.toLowerCase()
}

function getContrastColor(hex: string): string {
  const colorHex = resolveCartColorHex(hex)
  const c = colorHex.startsWith('#') ? colorHex.substring(1) : colorHex

  if (c.length !== 6) return '#000'

  const r = parseInt(c.substring(0, 2), 16)
  const g = parseInt(c.substring(2, 4), 16)
  const b = parseInt(c.substring(4, 6), 16)

  if ([r, g, b].some(Number.isNaN)) return '#000'

  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 125 ? '#000' : '#fff'
}

const getCartColorStyle = (colorName?: string) => ({
  backgroundColor: resolveCartColorHex(colorName),
  color: getContrastColor(resolveCartColorHex(colorName)),
})

void getCartColorStyle
void cartColorMap
void resolveCartColorHex

function isDisable(): boolean{
  if (cartItems.value.length === 0) return true;
  return address.value === null || address.value === '' || address.value === undefined;

}
</script>

<template>
  <HomeLayout/>
  <div class="container">
    <header class="cart-header">
      <div class="header-left">
        <h1>🛒 Giỏ Hàng Của Bạn</h1>
        <div class="breadcrumb">
          <router-link to="/customer/product-home">Sản Phẩm</router-link>
          / Giỏ hàng
        </div>
      </div>
      <div class="header-right">
        <div class="cart-count">{{ totalQuantity }}</div>
        <div class="cart-count-label">Sản phẩm</div>
      </div>
    </header>

    <div class="cart-grid">
      <section class="cart-items">
        <div class="cart-items-header">
          <h2 class="cart-items-title">📦 Sản Phẩm Trong Giỏ</h2>
          <label class="select-all">
            <input type="checkbox" v-model="allSelected"/> Chọn tất cả
          </label>
        </div>

        <article class="cart-item" v-for="item in cartItems" :key="item.productId">
          <div class="item-select">
            <input type="checkbox" v-model="item.selected"/>
          </div>
          <div class="item-image">
            <img :src="item.image || '/placeholder.png'" alt="product"/>
          </div>
          <div class="item-details">
            <h3 class="item-name">{{ item.productName }}</h3>
            <div class="item-specs">
              <span class="spec-badge">{{ item.ram }}</span>
              <span
                  class="spec-badge"
                  :style="getCartColorStyle(item.color)"
              ></span>
              <span class="spec-badge">{{ item.origin }}</span>
            </div>
            <div class="item-price">{{ Number(item.price).toLocaleString('vi-VN') }}₫</div>
            <div class="item-controls">
              <div class="quantity-control">
                <button class="qty-btn" @click="decreaseQty(item)">−</button>
                <input type="text" class="qty-input" :value="item.quantity" readonly/>
                <button class="qty-btn" @click="increaseQty(item)">+</button>
              </div>
              <button class="btn-remove" @click="removeItem(item)">🗑️ Xóa</button>
            </div>
          </div>
        </article>
      </section>

      <aside class="order-summary">
        <div class=" address-row">
          <label for="address" class="summary-label">Địa chỉ nhận hàng</label>
          <input
              id="address"
              v-model="address"
              type="text"
              class="address-input"
              placeholder="Nhập địa chỉ giao hàng..."
          />
        </div>
        <br>
        <h2 class="summary-title">📋 Tóm Tắt Đơn Hàng</h2>
        <div class="summary-row">
          <span class="summary-label">Tạm tính ({{ totalQuantity }} sản phẩm)</span>
          <span class="summary-value">{{ subtotal.toLocaleString('vi-VN') }}₫</span>
        </div>
        <div class="summary-row payment-row" style="flex-direction: column; align-items: flex-start; gap: 8px;">
          <span class="summary-label" style="font-weight: bold; margin-bottom: 4px;">Hình thức thanh toán</span>
          <div class="payment-methods" style="display: flex; flex-direction: column; gap: 8px; width: 100%;">
            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; width: 100%; font-weight: normal;">
              <input type="radio" v-model="paymentMethod" value="COD" />
              <span>💵 Thanh toán khi nhận hàng (COD)</span>
            </label>
            <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; width: 100%; font-weight: normal;">
              <input type="radio" v-model="paymentMethod" value="VNPAY" />
              <span>💳 Thanh toán trực tuyến qua VNPAY</span>
            </label>
          </div>
        </div>
        <div class="summary-row">
          <span class="summary-label">Phí vận chuyển</span>
          <span class="summary-value" style="color: #43e97b;">Chờ admin duyệt đơn</span>
        </div>
        <div class="summary-row summary-total">
          <span class="total-label">Tổng cộng</span>
          <span class="total-value">{{ subtotal.toLocaleString('vi-VN') }}₫</span>
        </div>
        <button class="btn-checkout" @click="checkout" :disabled="isDisable()">💳 Đặt hàng ngay</button>

        <div class="features">
          <div class="feature">
            <div class="feature-icon">🚚</div>
            <div class="feature-text">Giao hàng miễn phí</div>
          </div>
          <div class="feature">
            <div class="feature-icon">🔒</div>
            <div class="feature-text">Thanh toán bảo mật</div>
          </div>
          <div class="feature">
            <div class="feature-icon">↩️</div>
            <div class="feature-text">Đổi trả 30 ngày</div>
          </div>
        </div>
      </aside>
    </div>

    <div class="continue-shopping">
      <router-link to="/customer/product-home" class="btn-continue">⬅️ Tiếp Tục Mua Sắm</router-link>
    </div>
    <br>
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
button.btn-checkout:disabled {
  background: #ddd !important;
  color: #888 !important;
  cursor: not-allowed !important;
  transform: none !important;
}
img {
  width: 110px;
  height: 115px;
}

html, body {
  height: 100%;
}
.cod-note {
  display: flex;
  flex-direction: column;
  font-weight: 600;
}

.payment-info {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
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
}

/* Header */
.cart-header {
  background: white;
  padding: 30px 40px;
  border-radius: 20px;
  margin-bottom: 30px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h1 {
  font-size: 2.5em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 5px;
}

.breadcrumb {
  color: #666;
  font-size: 1em;
}

.breadcrumb a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.header-right {
  text-align: right;
}

.address-row {
  display: flex;
  flex-direction: column;
  margin-top: 12px;
}

.address-input {
  margin-top: 6px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #ddd;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease;
}

.address-input:focus {
  border-color: #43e97b;
}

.cart-count {
  font-size: 3em;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.cart-count-label {
  color: #666;
  font-size: 1.1em;
  font-weight: 600;
}

/* Main Grid */
.cart-grid {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 30px;
  align-items: start;
}

/* Cart Items */
.cart-items {
  background: white;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.cart-items-header {
  padding: 25px 30px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-items-title {
  font-size: 1.6em;
  font-weight: 700;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
}

.checkbox {
  width: 24px;
  height: 24px;
  border: 2px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.checkbox.checked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  position: relative;
}

.checkbox.checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: 800;
  font-size: 0.9em;
}

/* Cart Item */
.cart-item {
  padding: 25px 30px;
  border-bottom: 2px solid #f9f9f9;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  align-items: center;
  transition: all 0.3s ease;
}

.cart-item:hover {
  background: #f9f9f9;
}

.item-select {
  display: flex;
  align-items: center;
}

.item-image {
  width: 120px;
  height: 120px;
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  flex-shrink: 0;
}

.item-details {
  flex: 1;
}

.item-name {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.item-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.spec-badge {
  padding: 5px 12px;
  background: #f0f0f0;
  border-radius: 8px;
  font-size: 0.85em;
  color: #666;
  font-weight: 600;
}

.item-price {
  font-size: 1.4em;
  font-weight: 800;
  color: #667eea;
  margin-bottom: 12px;
}

.item-controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.quantity-control {
  display: flex;
  align-items: center;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
}

.qty-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: white;
  color: #666;
  font-size: 1.3em;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.qty-btn:hover {
  background: #667eea;
  color: white;
}

.qty-input {
  width: 60px;
  height: 40px;
  border: none;
  text-align: center;
  font-size: 1.1em;
  font-weight: 700;
  color: #1a1a2e;
}

.btn-remove {
  padding: 10px 20px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-remove:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(255, 107, 107, 0.4);
}

.item-actions {
  text-align: right;
}

.item-total {
  font-size: 1.8em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 15px;
}

/* Empty Cart */
.empty-cart {
  padding: 80px 30px;
  text-align: center;
}

.empty-icon {
  font-size: 120px;
  margin-bottom: 20px;
  opacity: 0.3;
}

.empty-title {
  font-size: 1.8em;
  font-weight: 700;
  color: #666;
  margin-bottom: 10px;
}

.empty-text {
  color: #999;
  font-size: 1.1em;
  margin-bottom: 30px;
}

.btn-shop {
  display: inline-block;
  padding: 15px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 15px;
  font-weight: 700;
  font-size: 1.1em;
  transition: all 0.3s ease;
}

.btn-shop:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.5);
}

/* Order Summary */
.order-summary {
  background: white;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  padding: 30px;
  position: sticky;
  top: 20px;
}

.summary-title {
  font-size: 1.6em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 2px solid #f0f0f0;
}

.summary-label {
  color: #666;
  font-size: 1.05em;
  font-weight: 600;
}

.summary-value {
  font-weight: 700;
  color: #1a1a2e;
  font-size: 1.1em;
}

.summary-total {
  padding: 20px 0;
  margin-top: 10px;
}

.total-label {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
}

.total-value {
  font-size: 1.8em;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Promo Code */
.promo-section {
  margin: 25px 0;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 15px;
}

.promo-input-group {
  display: flex;
  gap: 10px;
}

.promo-input {
  flex: 1;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 1em;
  font-weight: 600;
}

.promo-input:focus {
  outline: none;
  border-color: #667eea;
}

.btn-apply {
  padding: 12px 25px;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-apply:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(67, 233, 123, 0.4);
}

/* Checkout Button */
.btn-checkout {
  width: 100%;
  padding: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-size: 1.2em;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-checkout:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.5);
}

/* Features */
.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-top: 25px;
}

.feature {
  text-align: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 12px;
}

.feature-icon {
  font-size: 2em;
  margin-bottom: 8px;
}

.feature-text {
  font-size: 0.85em;
  color: #666;
  font-weight: 600;
}

/* Continue Shopping */
.continue-shopping {
  margin-top: 30px;
  text-align: center;
}

.btn-continue {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 15px 30px;
  background: white;
  color: #667eea;
  text-decoration: none;
  border-radius: 15px;
  font-weight: 700;
  font-size: 1.1em;
  border: 2px solid #667eea;
  transition: all 0.3s ease;
}

.btn-continue:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 1200px) {
  .cart-grid {
    grid-template-columns: 1fr 350px;
  }
}

@media (max-width: 968px) {
  .cart-grid {
    grid-template-columns: 1fr;
  }

  .order-summary {
    position: relative;
  }

  .cart-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .header-right {
    text-align: center;
  }

  .cart-item {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .item-actions {
    text-align: left;
  }

  .features {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  body {
    padding: 20px 10px;
  }

  .cart-header h1 {
    font-size: 1.8em;
  }

  .cart-count {
    font-size: 2em;
  }

  .item-image {
    width: 80px;
    height: 80px;
    font-size: 40px;
  }

  .item-name {
    font-size: 1.1em;
  }

  .item-controls {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>