<script setup lang="ts">
import {ref, onMounted, computed} from "vue";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import {cartService} from "@/service/CartService.ts";
import type {CartResponse, ProductInCart} from "@/models/Cart.ts";
import {CartRequest} from "@/models/CartRequest.ts";
import {toast} from "vue3-toastify";
import {CheckOutRequest} from "@/models/CheckOutRequest.ts";
import {
  ShoppingCart,
  ShoppingBag,
  MapPin,
  Receipt,
  Lock,
  Truck,
  ShieldCheck,
  RotateCcw,
  Award,
  Headphones,
  ArrowLeft,
  Trash2,
  Plus,
  Minus
} from "@lucide/vue";

type CartItemWithSelect = ProductInCart & { selected: boolean };
const cartItems = ref<CartItemWithSelect[]>([]);
const loading = ref(false);
const search = {};
const address = ref<string>("");

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
    toast.error("Update cart error", err);
    console.log("Update cart error", err);
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
    console.log(payload)
    await cartService.checkoutCart(payload.toPayload());
    toast.success("Thanh toán thành công!");
    await fetchCartItems();
  } catch (err) {
    toast.error("Checkout error", err);
  }
};

function isDisable(): boolean{
  if (cartItems.value.length === 0) return true;
  return address.value === null || address.value === '' || address.value === undefined;
}
</script>

<template>
  <HomeLayout/>
  <div class="cart-page-wrapper">
    <div class="container">
      
      <!-- Cart Header -->
      <header class="cart-header-alt">
        <div class="header-left-alt">
          <div class="header-icon-circle">
            <ShoppingCart :size="24" />
          </div>
          <div class="header-titles">
            <h1 class="cart-title-alt">Giỏ Hàng Của Bạn</h1>
            <div class="breadcrumb-alt">
              <router-link to="/customer/product-home" class="breadcrumb-link">Sản phẩm</router-link>
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-current">Giỏ hàng</span>
            </div>
          </div>
        </div>
        <div class="header-right-alt">
          <div class="cart-summary-badge">
            <ShoppingBag :size="16" />
            <span>{{ totalQuantity }} sản phẩm</span>
          </div>
        </div>
      </header>

      <!-- Cart Grid -->
      <div class="cart-grid-alt">
        
        <!-- Left Column: Products List / Empty State -->
        <div class="cart-main-col">
          <div class="cart-card-alt">
            <div class="cart-card-header">
              <div class="header-title-group">
                <ShoppingBag :size="20" class="header-icon-purple" />
                <h2 class="card-title-text">Sản phẩm trong giỏ</h2>
              </div>
              <label class="select-all-checkbox-label" v-if="cartItems.length > 0">
                <input type="checkbox" v-model="allSelected" class="custom-checkbox" />
                <span>Chọn tất cả</span>
              </label>
            </div>

            <!-- Empty Cart State -->
            <div class="empty-cart-state" v-if="cartItems.length === 0">
              <div class="empty-illustration-container">
                <svg width="200" height="200" viewBox="0 0 240 240" fill="none" class="empty-cart-svg">
                  <circle cx="120" cy="120" r="90" fill="#f3effc" />
                  <circle cx="120" cy="120" r="60" fill="#e8dcfa" />
                  
                  <path d="M60 70 L64 74 L60 78 L56 74 Z" fill="#c084fc" />
                  <path d="M180 60 L183 63 L180 66 L177 63 Z" fill="#c084fc" />
                  <path d="M195 130 L198 133 L195 136 L192 133 Z" fill="#c084fc" />
                  
                  <!-- Phone standing tilted in cart -->
                  <g transform="translate(95, 52) rotate(-12)">
                    <rect width="56" height="105" rx="8" fill="#7c3aed" />
                    <rect x="3" y="3" width="50" height="99" rx="6" fill="#a78bfa" />
                    <!-- Camera -->
                    <rect x="7" y="7" width="15" height="15" rx="3" fill="#6d28d9" />
                    <circle cx="11" cy="11" r="2" fill="#fff" />
                    <circle cx="18" cy="11" r="1.5" fill="#fff" />
                    <circle cx="11" cy="18" r="1.5" fill="#fff" />
                    <!-- Screen reflection line -->
                    <path d="M3 3 L53 3 L20 102 L3 102 Z" fill="rgba(255, 255, 255, 0.12)" />
                  </g>
                  
                  <!-- Basket details -->
                  <g stroke="#7c3aed" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M45 80 H65 L88 150 H165 L185 90" />
                    <circle cx="100" cy="175" r="9" fill="#7c3aed" stroke="none" />
                    <circle cx="155" cy="175" r="9" fill="#7c3aed" stroke="none" />
                    
                    <path d="M72 95 H180" stroke-width="2.5" />
                    <path d="M78 110 H175" stroke-width="2.5" />
                    <path d="M84 125 H170" stroke-width="2.5" />
                    <path d="M90 140 H165" stroke-width="2.5" />
                    
                    <path d="M92 90 L92 150" stroke-width="2.5" />
                    <path d="M116 90 L114 150" stroke-width="2.5" />
                    <path d="M140 90 L136 150" stroke-width="2.5" />
                    <path d="M164 90 L158 150" stroke-width="2.5" />
                  </g>
                </svg>
              </div>
              <h3 class="empty-title-alt">Giỏ hàng của bạn đang trống</h3>
              <p class="empty-desc-alt">Hãy khám phá những mẫu điện thoại mới nhất và thêm vào giỏ hàng nhé!</p>
              
              <router-link to="/customer/product-home" class="btn-continue-shopping-alt">
                <ArrowLeft :size="18" />
                <span>Tiếp tục mua sắm</span>
              </router-link>
            </div>

            <!-- Active Cart Items List -->
            <div class="cart-items-list" v-else>
              <div class="cart-item-row" v-for="item in cartItems" :key="item.productId">
                <div class="item-select-col">
                  <input type="checkbox" v-model="item.selected" class="custom-checkbox" />
                </div>
                <div class="item-image-col">
                  <img :src="item.image || '/placeholder.png'" class="item-thumbnail" alt="Product Image" />
                </div>
                <div class="item-info-col">
                  <h3 class="item-title-text">{{ item.productName }}</h3>
                  <div class="item-specs-badges">
                    <span class="spec-badge-pill" v-if="item.ram">{{ item.ram }}</span>
                    <span class="spec-badge-pill" v-if="item.ops">{{ item.ops }}</span>
                    <div class="color-badge-pill" v-if="item.color">
                      <span class="color-circle" :style="{ backgroundColor: item.color }"></span>
                      <span class="color-label">{{ item.color }}</span>
                    </div>
                  </div>
                  <div class="item-price-val">{{ Number(item.price).toLocaleString('vi-VN') }}đ</div>
                </div>
                <div class="item-actions-col">
                  <div class="item-quantity-selector">
                    <button class="quantity-btn-alt" @click="decreaseQty(item)" :disabled="item.quantity <= 1">
                      <Minus :size="14" />
                    </button>
                    <input type="text" class="quantity-input-alt" :value="item.quantity" readonly />
                    <button class="quantity-btn-alt" @click="increaseQty(item)">
                      <Plus :size="14" />
                    </button>
                  </div>
                  <button class="item-delete-btn" @click="removeItem(item)" title="Xóa sản phẩm">
                    <Trash2 :size="14" />
                    <span>Xóa</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Shipping Info & Cart Summary -->
        <div class="cart-side-col">
          
          <!-- Shipping Address Card -->
          <div class="cart-card-alt summary-card">
            <div class="card-header-iconic">
              <MapPin :size="18" class="summary-card-icon" />
              <h3 class="summary-card-title">Địa chỉ nhận hàng</h3>
            </div>
            <div class="address-input-wrapper">
              <MapPin :size="16" class="input-inner-icon" />
              <input
                id="address"
                v-model="address"
                type="text"
                class="address-field-alt"
                placeholder="Nhập địa chỉ giao hàng..."
              />
            </div>
          </div>

          <!-- Order Summary Card -->
          <div class="cart-card-alt summary-card">
            <div class="card-header-iconic">
              <Receipt :size="18" class="summary-card-icon" />
              <h3 class="summary-card-title">Tóm tắt đơn hàng</h3>
            </div>
            <div class="summary-details-rows">
              <div class="detail-row">
                <span class="detail-label-text">Tạm tính</span>
                <span class="detail-value-text">{{ subtotal.toLocaleString('vi-VN') }}đ</span>
              </div>
              <div class="detail-row align-start">
                <span class="detail-label-text">Hình thức thanh toán</span>
                <div class="detail-value-group">
                  <span class="detail-value-text text-right">Khi nhận hàng</span>
                  <span class="payment-disclaimer">(Hiện chưa hỗ trợ chuyển khoản)</span>
                </div>
              </div>
              <div class="detail-row">
                <span class="detail-label-text">Phí vận chuyển</span>
                <span class="detail-value-text shipping-free">0đ</span>
              </div>
              
              <div class="total-highlight-box">
                <span class="total-label-large">Tổng cộng</span>
                <span class="total-value-large">{{ subtotal.toLocaleString('vi-VN') }}đ</span>
              </div>
              
              <button class="btn-checkout-alt" @click="checkout" :disabled="isDisable()">
                <Lock :size="16" />
                <span>Đặt hàng ngay</span>
              </button>
              <p class="checkout-hint-text" v-if="isDisable() && cartItems.length > 0">
                Vui lòng nhập địa chỉ nhận hàng để tiếp tục
              </p>
              <p class="checkout-hint-text" v-else-if="cartItems.length === 0">
                Vui lòng thêm sản phẩm vào giỏ hàng
              </p>
            </div>
          </div>

          <!-- Trust Badges -->
          <div class="side-trust-badges-grid">
            <div class="side-trust-card">
              <div class="badge-icon-wrap">
                <Truck :size="20" />
              </div>
              <span class="badge-text-label">Giao hàng miễn phí</span>
            </div>
            <div class="side-trust-card">
              <div class="badge-icon-wrap">
                <ShieldCheck :size="20" />
              </div>
              <span class="badge-text-label">Thanh toán bảo mật</span>
            </div>
            <div class="side-trust-card">
              <div class="badge-icon-wrap">
                <RotateCcw :size="20" />
              </div>
              <span class="badge-text-label">Đổi trả 30 ngày</span>
            </div>
          </div>
          
        </div>
      </div>

      <!-- Bottom Banners Row -->
      <div class="store-features-bar-alt">
        <div class="store-feature-item">
          <div class="feature-icon-outer">
            <ShieldCheck :size="18" />
          </div>
          <div class="feature-texts">
            <span class="feature-title-bold">Sản phẩm chính hãng</span>
            <span class="feature-subtitle-soft">100% chính hãng</span>
          </div>
        </div>
        <div class="store-feature-item">
          <div class="feature-icon-outer">
            <Award :size="18" />
          </div>
          <div class="feature-texts">
            <span class="feature-title-bold">Bảo hành uy tín</span>
            <span class="feature-subtitle-soft">Tại trung tâm chính hãng</span>
          </div>
        </div>
        <div class="store-feature-item">
          <div class="feature-icon-outer">
            <Headphones :size="18" />
          </div>
          <div class="feature-texts">
            <span class="feature-title-bold">Hỗ trợ tận tâm</span>
            <span class="feature-subtitle-soft">24/7 mọi lúc mọi nơi</span>
          </div>
        </div>
        <div class="store-feature-item">
          <div class="feature-icon-outer">
            <RotateCcw :size="18" />
          </div>
          <div class="feature-texts">
            <span class="feature-title-bold">Trả góp 0%</span>
            <span class="feature-subtitle-soft">Linh hoạt, dễ dàng</span>
          </div>
        </div>
      </div>

    </div>
  </div>
  <Footer/>
</template>

<style scoped>
.cart-page-wrapper {
  background-color: #f8f9fc;
  min-height: 100vh;
  padding: 40px 20px 80px 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.container {
  max-width: 1340px;
  margin: 0 auto;
}

/* Header */
.cart-header-alt {
  background: #ffffff;
  padding: 24px 32px;
  border-radius: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  border: 1px solid rgba(124, 58, 237, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left-alt {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #f3effc;
  color: #7c3aed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-titles {
  display: flex;
  flex-direction: column;
}

.cart-title-alt {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1e1b4b;
  margin-bottom: 4px;
}

.breadcrumb-alt {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.88rem;
  color: #6b7280;
}

.breadcrumb-link {
  color: #7c3aed;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.breadcrumb-link:hover {
  color: #6d28d9;
}

.breadcrumb-separator {
  color: #d1d5db;
}

.breadcrumb-current {
  color: #9ca3af;
}

.header-right-alt {
  display: flex;
  align-items: center;
}

.cart-summary-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #7c3aed;
  color: #ffffff;
  padding: 10px 20px;
  border-radius: 9999px;
  font-weight: 700;
  font-size: 0.9rem;
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.2);
}

/* Grid Layout */
.cart-grid-alt {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
  align-items: start;
  margin-bottom: 32px;
}

.cart-main-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.cart-side-col {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: sticky;
  top: 96px;
}

/* Cart Card */
.cart-card-alt {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(124, 58, 237, 0.08);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
  padding: 28px;
}

.cart-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1.5px solid #f1f5f9;
  margin-bottom: 20px;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon-purple {
  color: #7c3aed;
}

.card-title-text {
  font-size: 1.15rem;
  font-weight: 800;
  color: #1e1b4b;
}

.select-all-checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  user-select: none;
}

.custom-checkbox {
  width: 18px;
  height: 18px;
  border-radius: 6px;
  border: 1.5px solid #d1d5db;
  outline: none;
  cursor: pointer;
  accent-color: #7c3aed;
}

/* Cart Item Row */
.cart-items-list {
  display: flex;
  flex-direction: column;
}

.cart-item-row {
  display: grid;
  grid-template-columns: auto auto 1fr auto;
  gap: 24px;
  align-items: center;
  padding: 24px 0;
  border-bottom: 1.5px solid #f1f5f9;
}

.cart-item-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.cart-item-row:first-child {
  padding-top: 0;
}

.item-select-col {
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image-col {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  background: #f8f9fc;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 8px;
}

.item-thumbnail {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.item-info-col {
  display: flex;
  flex-direction: column;
}

.item-title-text {
  font-size: 1.05rem;
  font-weight: 700;
  color: #1e1b4b;
  margin-bottom: 6px;
  line-height: 1.4;
}

.item-specs-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.spec-badge-pill {
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 9999px;
  font-size: 0.78rem;
  color: #4b5563;
  font-weight: 600;
}

.color-badge-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 9999px;
  font-size: 0.78rem;
  color: #4b5563;
  font-weight: 600;
}

.color-circle {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.1);
  display: inline-block;
}

.item-price-val {
  font-size: 1.15rem;
  font-weight: 800;
  color: #7c3aed;
}

.item-actions-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.item-quantity-selector {
  display: flex;
  align-items: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 9999px;
  background: #ffffff;
  overflow: hidden;
  height: 36px;
}

.quantity-btn-alt {
  width: 32px;
  height: 100%;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.quantity-btn-alt:hover:not(:disabled) {
  background: #f1f5f9;
  color: #7c3aed;
}

.quantity-btn-alt:disabled {
  color: #d1d5db;
  cursor: not-allowed;
}

.quantity-input-alt {
  width: 40px;
  height: 100%;
  border: none;
  text-align: center;
  font-size: 0.9rem;
  font-weight: 700;
  color: #1e1b4b;
  background: transparent;
  outline: none;
}

.item-delete-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: 1.5px solid #fee2e2;
  border-radius: 9999px;
  color: #ef4444;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.item-delete-btn:hover {
  background: #ef4444;
  border-color: #ef4444;
  color: #ffffff;
}

/* Empty Cart State */
.empty-cart-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-illustration-container {
  margin-bottom: 24px;
}

.empty-cart-svg {
  filter: drop-shadow(0 10px 20px rgba(124, 58, 237, 0.08));
}

.empty-title-alt {
  font-size: 1.3rem;
  font-weight: 800;
  color: #1e1b4b;
  margin-bottom: 8px;
}

.empty-desc-alt {
  font-size: 0.95rem;
  color: #6b7280;
  max-width: 400px;
  margin-bottom: 28px;
  line-height: 1.5;
}

.btn-continue-shopping-alt {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: #5832e0;
  color: #ffffff;
  text-decoration: none;
  border-radius: 9999px;
  font-weight: 700;
  font-size: 0.95rem;
  box-shadow: 0 8px 20px rgba(88, 50, 224, 0.25);
  transition: all 0.3s ease;
}

.btn-continue-shopping-alt:hover {
  background: #4726c4;
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(88, 50, 224, 0.35);
}

/* Side Card Options */
.summary-card {
  padding: 24px;
}

.card-header-iconic {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.summary-card-icon {
  color: #7c3aed;
}

.summary-card-title {
  font-size: 1rem;
  font-weight: 800;
  color: #1e1b4b;
}

.address-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-inner-icon {
  position: absolute;
  left: 14px;
  color: #9ca3af;
  pointer-events: none;
}

.address-field-alt {
  width: 100%;
  height: 48px;
  padding: 0 16px 0 40px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.9rem;
  outline: none;
  background: #ffffff;
  color: #1f2937;
  transition: all 0.3s;
}

.address-field-alt:focus {
  border-color: #7c3aed;
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.08);
}

/* Summary Details */
.summary-details-rows {
  display: flex;
  flex-direction: column;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-row.align-start {
  align-items: flex-start;
}

.detail-row:last-of-type {
  border-bottom: none;
}

.detail-label-text {
  font-size: 0.9rem;
  font-weight: 600;
  color: #6b7280;
}

.detail-value-text {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1e1b4b;
}

.detail-value-group {
  display: flex;
  flex-direction: column;
}

.text-right {
  text-align: right;
}

.payment-disclaimer {
  font-size: 0.72rem;
  color: #9ca3af;
  margin-top: 2px;
  font-weight: 500;
  text-align: right;
}

.shipping-free {
  color: #22c55e;
}

.total-highlight-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f3effc;
  padding: 16px 20px;
  border-radius: 16px;
  margin: 16px 0;
}

.total-label-large {
  font-size: 1rem;
  font-weight: 800;
  color: #1e1b4b;
}

.total-value-large {
  font-size: 1.5rem;
  font-weight: 800;
  color: #7c3aed;
}

.btn-checkout-alt {
  width: 100%;
  height: 52px;
  background: #7c3aed;
  color: #ffffff;
  border: none;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.25);
  transition: all 0.3s;
}

.btn-checkout-alt:hover:not(:disabled) {
  background: #6d28d9;
  transform: translateY(-1px);
  box-shadow: 0 10px 28px rgba(124, 58, 237, 0.35);
}

.btn-checkout-alt:active:not(:disabled) {
  transform: translateY(1px);
}

.btn-checkout-alt:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

.checkout-hint-text {
  text-align: center;
  font-size: 0.78rem;
  color: #ef4444;
  margin-top: 10px;
  font-weight: 600;
}

/* Side trust badges */
.side-trust-badges-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.side-trust-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 16px;
  border: 1px solid rgba(124, 58, 237, 0.08);
}

.badge-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f3effc;
  color: #7c3aed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-text-label {
  font-size: 0.85rem;
  font-weight: 700;
  color: #4b5563;
}

/* Store Features Bottom Bar */
.store-features-bar-alt {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(124, 58, 237, 0.08);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
}

.store-feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 12px;
  border-right: 1px solid #f1f5f9;
}

.store-feature-item:last-child {
  border-right: none;
}

.feature-icon-outer {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f3effc;
  color: #7c3aed;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-texts {
  display: flex;
  flex-direction: column;
}

.feature-title-bold {
  font-size: 0.85rem;
  font-weight: 800;
  color: #1e1b4b;
}

.feature-subtitle-soft {
  font-size: 0.75rem;
  color: #9ca3af;
  font-weight: 500;
}

/* Responsiveness */
@media (max-width: 1200px) {
  .cart-grid-alt {
    grid-template-columns: 1fr 360px;
  }
  .store-features-bar-alt {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  .store-feature-item {
    border-right: none;
  }
}

@media (max-width: 968px) {
  .cart-grid-alt {
    grid-template-columns: 1fr;
  }
  .cart-side-col {
    position: static;
  }
}

@media (max-width: 640px) {
  .cart-item-row {
    grid-template-columns: auto 1fr;
    gap: 16px;
  }
  .item-select-col {
    grid-column: 1;
    grid-row: 1;
  }
  .item-image-col {
    grid-column: 1;
    grid-row: 2;
  }
  .item-info-col {
    grid-column: 2;
    grid-row: 1 / span 2;
  }
  .item-actions-col {
    grid-column: 1 / span 2;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    border-top: 1px dashed #f1f5f9;
    padding-top: 12px;
    margin-top: 8px;
  }
  .store-features-bar-alt {
    grid-template-columns: 1fr;
  }
  .cart-header-alt {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .header-right-alt {
    width: 100%;
  }
  .cart-summary-badge {
    width: 100%;
    justify-content: center;
  }
}
</style>