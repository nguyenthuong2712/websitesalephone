<script setup lang="ts">
import {ref, onMounted, onUnmounted} from "vue";
import {useRoute} from "vue-router";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import { ProductDetailRequest } from '@/models/ProductDetailRequest';
import { productService } from '@/service/ProductService';
import type { ProductDetailResponse } from '@/models/ProductDetailResponse';
import type { CreateCartRequest } from '@/models/CreateCartRequest';
import { CartRequest } from '@/models/CartRequest';
import { cartService } from '@/service/CartService';
import {toast} from "vue3-toastify";

const route = useRoute();
const productId = route.params.id as string;

const product = ref<ProductDetailResponse | null>(null);

// Selected single option for each spec
const selectedRam = ref<string>("");
const selectedCpu = ref<string>("");
const selectedCamera = ref<string>("");
const selectedScreen = ref<string>("");
const selectedColor = ref<string>("");
const selectedOrigin = ref<string>("");
const selectedOp = ref<string>("");
const selectedBattery = ref<string>("");
const selectedStorage = ref<string>("");
const availableQuantity = ref<number>(0);
const productVariantId = ref<string | null>(null);
const selectedPrice = ref<number | null>(null);

const currentIndex = ref(0)

let intervalId: any = null

// Format giá
const formatCurrency = (value: number) =>
    new Intl.NumberFormat("vi-VN", {style: "currency", currency: "VND"}).format(value);

const loadProductDetail = async () => {
  try {
    const response = await productService.detail(new ProductDetailRequest({ idProduct: productId }));
    product.value = response.data.data as ProductDetailResponse;

    if (product?.value) {
      selectedRam.value = product?.value?.rams[0]?.id || "";
      selectedCamera.value = product?.value?.cameras[0]?.id || "";
      selectedScreen.value = product?.value?.screens[0]?.id || "";
      selectedColor.value = product?.value?.colors[0]?.id || "";
      selectedOrigin.value = product?.value?.origins[0]?.id || "";
      updateQuantity();
      startAutoSlide();
    }
  } catch (error) {
    console.error("Lỗi khi lấy chi tiết sản phẩm", error);
  }
};

const quantity = ref(1);

const increaseQty = () => quantity.value += 1;
const decreaseQty = () => {
  if (quantity.value > 1) quantity.value -= 1;
};

const updateQuantity = async () => {
  // Kiểm tra đủ 3 option bắt buộc
  if (!selectedRam.value || !selectedColor.value || !selectedOrigin.value || !selectedScreen.value || !selectedCamera.value) {
    availableQuantity.value = 0;
    selectedPrice.value = null;
    productVariantId.value = null;
    return;
  }
  if (!product?.value) return;

  const request: CreateCartRequest = {
    idProduct: productId,
    idRam: selectedRam.value,
    idColor: selectedColor.value,
    idOrigin: selectedOrigin.value,
    screenId: selectedScreen.value,
    cameraId: selectedCamera.value,
  };

  try {
    const response = await productService.getQuantity(request);
    availableQuantity.value = response.data.data.quantity;
    productVariantId.value = response.data.data.idProduct;
    selectedPrice.value = response.data.data.price ?? null;
  } catch (error) {
    console.error("Lỗi khi lấy số lượng sản phẩm", error);
    availableQuantity.value = 0;
    productVariantId.value = null;
    selectedPrice.value = null;
  }
};

const selectOption = (
    type: "ram" | "cpu" | "camera" | "screen" | "color" | "origin" | "op" | "battery" | "storage",
    id: string
) => {
  switch (type) {
    case "ram":
      selectedRam.value = id;
      break;
    case "cpu":
      selectedCpu.value = id;
      break;
    case "camera":
      selectedCamera.value = id;
      break;
    case "screen":
      selectedScreen.value = id;
      break;
    case "color":
      selectedColor.value = id;
      break;
    case "origin":
      selectedOrigin.value = id;
      break;
    case "op":
      selectedOp.value = id;
      break;
    case "battery":
      selectedBattery.value = id;
      break;
    case "storage":
      selectedStorage.value = id;
      break;
  }

  updateQuantity();
};

const addToCart = async () => {
  if (!product?.value || quantity.value < 1) return;

  if (!productVariantId.value) {
    toast.error('Vui lòng chọn đầy đủ phiên bản sản phẩm')
    return
  }

  const cartRequest = new CartRequest('', productVariantId.value, quantity.value)

  try {
    await cartService.addToCart(cartRequest.toPayload());
    toast.success(`Đã thêm ${quantity.value} sản phẩm vào giỏ hàng`);
  } catch (err) {
    console.error(err);
    toast.error("Thêm vào giỏ hàng thất bại!");
  }
};

const colorMap: Record<string, string> = {
  BLACK: '#000000',
  WHITE: '#FFFFFF',
  RED: '#FF0000',
  GREEN: '#00FF00',
  BLUE: '#0000FF',
  YELLOW: '#FFFF00',
  ORANGE: '#FFA500',
  PURPLE: '#800080',
  PINK: '#FFC0CB',
  BROWN: '#A52A2A',
  GREY: '#808080',
  GRAY: '#808080',
  SILVER: '#C0C0C0',
  GOLD: '#FFD700',
  CYAN: '#00FFFF',
  MAGENTA: '#FF00FF',
  NAVY: '#000080',
  LIME: '#00FF00',
  TEAL: '#008080',
  OLIVE: '#808000',
  MAROON: '#800000',
  CORAL: '#FF7F50',
  TURQUOISE: '#40E0D0',
  INDIGO: '#4B0082',
  VIOLET: '#EE82EE',
  BEIGE: '#F5F5DC',
  TAN: '#D2B48C',
  CHOCOLATE: '#D2691E',
  SALMON: '#FA8072',
  KHAKI: '#F0E68C',
  MINT: '#98FF98',
  PEACH: '#FFE5B4',
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

const resolveColorHex = (colorName?: string): string => {
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

  return colorMap[key] ?? normalized.toLowerCase()
}

const getColorCircleStyle = (colorName?: string) => ({
  backgroundColor: resolveColorHex(colorName),
})

const getColorTitle = (colorName?: string) => colorName?.trim() || 'Unknown'

void getColorTitle
void getColorCircleStyle
void resolveColorHex

const startAutoSlide = () => {
  if (!product?.value?.responseList?.length) return;

  intervalId = setInterval(() => {
    const images = product.value?.responseList ?? []
    if (images.length === 0) {
      return
    }
    currentIndex.value = (currentIndex.value + 1) % images.length
  }, 3000);
};

onMounted(() =>
        loadProductDetail(),
);
onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<template>
  <HomeLayout/>

  <nav class="breadcrumb">
    <a href="#home">Trang chủ</a> <span>/</span>
    <a href="#products">Sản phẩm</a> <span>/</span>
    <a href="#smartphones">Điện thoại</a> <span>/</span>
    <span>{{ product?.productName || "Đang tải..." }}</span>
  </nav>

  <article class="product-detail">
    <div class="product-grid">
      <!-- Image Gallery -->
      <section class="image-gallery">
        <div class="main-image">
          <img
              :src="product?.responseList?.[currentIndex]?.url ?? 'https://cellphones.com.vn/iphone-16-pro-max.html'"
              alt="main image"
              style="width: 323px; height: 323px"
          />
        </div>
      </section>
      <!-- Product Info -->
      <section class="product-info">
        <span class="product-badge">🔥 HOT DEAL</span>
        <h1 class="product-title">{{ product?.productName }}</h1>

        <div class="current-price">{{ formatCurrency(selectedPrice ?? product?.price ?? 0) }}</div>

        <!-- RAM Selection -->
        <div class="spec-section">
          <div class="spec-label"><span class="spec-icon">💾</span> Dung Lượng RAM</div>
          <div class="spec-options">
            <div v-for="ram in product?.rams" :key="ram.id" class="spec-option"
                 :class="{ selected: selectedRam === ram.id }" @click="selectOption('ram', ram.id)">
              {{ ram.name }}
            </div>
          </div>
        </div>
        <!-- Color Selection -->
        <div class="spec-section" v-if="product?.colors?.length">
          <div class="spec-label"><span class="spec-icon">🎨</span> Màu Sắc</div>
          <div class="color-options">
            <div v-for="color in product.colors" :key="color.id"
                 class="color-option"
                 :class="{ selected: selectedColor === color.id }"
                 :title="getColorTitle(color.name)"
                 @click="selectOption('color', color.id)">
              <span :style="getColorCircleStyle(color.name)">&nbsp;</span>
            </div>
          </div>
        </div>

        <!-- Origin Selection -->
        <div class="spec-section" v-if="product?.origins?.length">
          <div class="spec-label">Xuất xứ</div>
          <div class="spec-options">
            <div v-for="origin in product.origins" :key="origin.id" class="spec-option"
                 :class="{ selected: selectedOrigin === origin.id }"
                 @click="selectOption('origin', origin.id)">
              {{ origin.name }}
            </div>
          </div>
        </div>

        <!-- screen Selection -->
        <div class="spec-section" v-if="product?.screens?.length">
          <div class="spec-label">Màn hình</div>
          <div class="spec-options">
            <div v-for="screen in product.screens" :key="screen.id" class="spec-option"
                 :class="{ selected: selectedScreen === screen.id }"
                 @click="selectOption('screen', screen.id)">
              {{ screen.name }}
            </div>
          </div>
        </div>

        <!-- Camera Selection -->
        <div class="spec-section" v-if="product?.cameras?.length">
          <div class="spec-label">Camera</div>
          <div class="spec-options">
            <div v-for="camera in product.cameras" :key="camera.id" class="spec-option"
                 :class="{ selected: selectedCamera === camera.id }"
                 @click="selectOption('camera', camera.id)">
              {{ camera.name }}
            </div>
          </div>
        </div>

        <!-- Quantity Selection -->
        <div class="spec-section">
          <div class="spec-options quantity-options">
            <!-- Giảm: disable khi quantity <= 1 -->
            <button class="qty-btn" @click="decreaseQty" :disabled="quantity <= 1">-</button>

            <input type="text" v-model.number="quantity" readonly/>

            <!-- Tăng: disable khi quantity >= availableQuantity -->
            <button class="qty-btn" @click="increaseQty" :disabled="quantity >= availableQuantity">+</button>

            <span class="stock-info">Còn lại: {{ availableQuantity }}</span>
          </div>
        </div>

        <!-- Add to Cart -->
        <div class="action-buttons">
          <a
              href="#"
              class="btn btn-primary"
              :class="{ 'disabled-link': availableQuantity === 0 }"
              @click.prevent="availableQuantity === 0 ? null : addToCart()"
          >
            🛒 Thêm Vào Giỏ Hàng
          </a>
        </div>
      </section>
    </div>
  </article>

  <Footer/>
</template>

<style scoped>
.disabled-link {
  pointer-events: none;
  opacity: 0.5;
  cursor: not-allowed;
}

.color-option span {
  display: block;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 2px solid #ddd;
  transition: all 0.3s ease;
}

.color-option.selected {
  border-color: #1a1a2e;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.3);
  position: relative;
}
.main-image img {
  transition: opacity 0.6s ease;
}

.main-image img {
  opacity: 1;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.main-image img.fade {
  opacity: 0;
}

.color-option.selected::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-weight: bold;
  font-size: 1.3em;
  text-shadow: 0 0 3px rgba(0, 0, 0, 0.5);
}

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
}

/* Breadcrumb */
.breadcrumb {
  background: white;
  padding: 20px 30px;
  border-radius: 15px;
  margin-bottom: 30px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
}

.breadcrumb a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.breadcrumb a:hover {
  color: #764ba2;
}

.breadcrumb span {
  color: #666;
  margin: 0 10px;
}

/* Product Detail Card */
.product-detail {
  background: white;
  border-radius: 25px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  padding: 50px;
}

/* Image Gallery */
.image-gallery {
  position: sticky;
  top: 20px;
  width: 586px;
  margin-right: 20px;
}

.main-image {
  width: 100%;
  height: 500px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 180px;
  margin-bottom: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #d1d5db;
  margin-left: 145px;
}

.main-image:hover {
  transform: scale(1.02);
}

/* Product Info */
.product-info {
  padding: 20px 0;
}

.product-badge {
  display: inline-block;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 0.9em;
  font-weight: 700;
  margin-bottom: 15px;
}

.product-title {
  font-size: 2.5em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 15px;
  line-height: 1.2;
}

.current-price {
  font-size: 2.5em;
  font-weight: 800;
  color: #667eea;
  margin-bottom: 5px;
}

/* Specifications Options */
.spec-section {
  margin-bottom: 35px;
}

.spec-label {
  font-size: 1.1em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.spec-icon {
  font-size: 1.3em;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.spec-option {
  padding: 12px 24px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 0.95em;
  color: #666;
}

.spec-option:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

.spec-option.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

/* Color Options */
.color-options {
  display: flex;
  gap: 15px;
}

.color-option {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid transparent;
  position: relative;
}

.color-option:hover {
  transform: scale(1.15);
}

.color-option.selected {
  border-color: #1a1a2e;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
}

.color-option.selected::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: 800;
  font-size: 1.3em;
  text-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 40px;
}

.btn {
  flex: 1;
  padding: 18px 35px;
  border-radius: 15px;
  border: none;
  font-weight: 700;
  font-size: 1.1em;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.5);
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Description Section */
.description-section {
  padding: 40px 50px;
  border-top: 2px solid #f0f0f0;
}

.description-title {
  font-size: 1.8em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 20px;
}

.description-text {
  color: #666;
  font-size: 1.05em;
  line-height: 1.8;
  margin-bottom: 15px;
}

/* Responsive */
@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 30px;
  }

  .image-gallery {
    position: relative;
  }

  .main-image {
    height: 400px;
    font-size: 140px;
  }

}

@media (max-width: 768px) {
  body {
    padding: 20px 10px;
  }

  .product-title {
    font-size: 1.8em;
  }

  .current-price {
    font-size: 2em;
  }

  .main-image {
    height: 300px;
    font-size: 100px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .spec-option {
    padding: 10px 18px;
    font-size: 0.9em;
  }

  .description-section {
    padding: 30px 20px;
  }
}

.quantity-options {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
}

.quantity-options input {
  width: 50px;
  text-align: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 3px 0;
  font-size: 14px;
  background-color: #fff;
}

.quantity-options .qty-btn {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f5f5f5;
  font-size: 18px;
  cursor: pointer;
  transition: 0.2s;
}

.quantity-options .qty-btn:hover {
  background-color: #e0e0e0;
}

</style>