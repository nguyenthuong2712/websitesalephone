<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import { ProductDetailRequest } from '@/models/ProductDetailRequest';
import { productService } from '@/service/ProductService';
import type { ProductDetailResponse } from '@/models/ProductDetailResponse';
import type { CreateCartRequest } from '@/models/CreateCartRequest';
import { CartRequest } from '@/models/CartRequest';
import { cartService } from '@/service/CartService';
import { toast } from "vue3-toastify";
import { formatCurrency } from "@/utils/Constant";

const route = useRoute();
const router = useRouter();
const productId = route.params.id as string;

const navigateToRelated = (id: string) => {
  router.push(`/customer/detail-product/${id}`).then(() => {
    window.location.reload();
  });
};

const product = ref<ProductDetailResponse | null>(null);
const relatedProducts = ref<any[]>([]);

// Spec options selection
const selectedRam = ref<string>("");
const selectedCamera = ref<string>("");
const selectedScreen = ref<string>("");
const selectedColor = ref<string>("");
const selectedOrigin = ref<string>("");
const availableQuantity = ref<number>(0);
const productVariantId = ref<string | null>(null);
const selectedPrice = ref<number | null>(null);

const currentIndex = ref(0);
let intervalId: any = null;
const quantity = ref(1);

// Active Tab logic
const activeTab = ref<'description' | 'specification' | 'reviews' | 'related'>('description');

// Mock reviews since review table is not in database schema
const reviews = ref([
  { id: 1, author: "Nguyễn Văn Hùng", rating: 5, date: "2026-05-15", content: "Sản phẩm dùng rất mượt, pin trâu và camera chụp đêm cực sắc nét. Rất đáng đồng tiền bát gạo!" },
  { id: 2, author: "Trần Thị Lan", rating: 4, date: "2026-05-20", content: "Màu titan tự nhiên đẹp xuất sắc. Màn hình 120Hz mượt mà. Tuy nhiên củ sạc nhanh phải mua riêng hơi tiếc." },
  { id: 3, author: "Lê Minh Tuấn", rating: 5, date: "2026-05-28", content: "Chất lượng máy không chê vào đâu được, nhân viên shop hỗ trợ nhiệt tình, tư vấn chu đáo qua chat realtime luôn!" }
]);

const loadProductDetail = async () => {
  try {
    const response = await productService.detail(new ProductDetailRequest({ idProduct: productId }));
    product.value = response.data.data as ProductDetailResponse;

    if (product.value) {
      selectedRam.value = product.value.rams?.[0]?.id || "";
      selectedCamera.value = product.value.cameras?.[0]?.id || "";
      selectedScreen.value = product.value.screens?.[0]?.id || "";
      selectedColor.value = product.value.colors?.[0]?.id || "";
      selectedOrigin.value = product.value.origins?.[0]?.id || "";
      updateQuantity();
      startAutoSlide();
    }
  } catch (error) {
    console.error("Lỗi khi lấy chi tiết sản phẩm", error);
  }
};

const loadRelatedProducts = async () => {
  try {
    const res = await productService.getAllNewProduct();
    // Exclude the current product to get related ones
    relatedProducts.value = (res.data.data || []).filter((p: any) => p.id !== productId).slice(0, 4);
  } catch (error) {
    console.error("Lỗi khi load sản phẩm liên quan:", error);
  }
};

const increaseQty = () => {
  if (quantity.value < availableQuantity.value) {
    quantity.value += 1;
  }
};
const decreaseQty = () => {
  if (quantity.value > 1) quantity.value -= 1;
};

const updateQuantity = async () => {
  if (!selectedRam.value || !selectedColor.value || !selectedOrigin.value || !selectedScreen.value || !selectedCamera.value) {
    availableQuantity.value = 0;
    selectedPrice.value = null;
    productVariantId.value = null;
    return;
  }
  if (!product.value) return;

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
  type: "ram" | "camera" | "screen" | "color" | "origin",
  id: string
) => {
  switch (type) {
    case "ram": selectedRam.value = id; break;
    case "camera": selectedCamera.value = id; break;
    case "screen": selectedScreen.value = id; break;
    case "color": selectedColor.value = id; break;
    case "origin": selectedOrigin.value = id; break;
  }
  updateQuantity();
};

const addToCart = async (showSuccessToast = true) => {
  if (!product.value || quantity.value < 1) return false;

  if (!productVariantId.value) {
    toast.error('Vui lòng chọn đầy đủ phiên bản sản phẩm');
    return false;
  }

  const cartRequest = new CartRequest('', productVariantId.value, quantity.value);

  try {
    await cartService.addToCart(cartRequest.toPayload());
    if (showSuccessToast) {
      toast.success(`Đã thêm ${quantity.value} sản phẩm vào giỏ hàng`);
    }
    return true;
  } catch (err) {
    console.error(err);
    toast.error("Thêm vào giỏ hàng thất bại!");
    return false;
  }
};

const buyNow = async () => {
  const success = await addToCart(false);
  if (success) {
    router.push({ name: 'cart' });
  }
};

const contactConsultation = () => {
  // Trigger chat widget if present or show helpful toast
  const chatBtn = document.querySelector('.chat-button') as HTMLButtonElement;
  if (chatBtn) {
    chatBtn.click();
  } else {
    toast.info("Đang kết nối với nhân viên tư vấn qua Chat Realtime...");
  }
};

// Safe rendering helper for description
const formattedDescription = computed(() => {
  if (!product.value?.description) return "Chưa có mô tả cho sản phẩm này.";

  // Format standard newlines or handle HTML tags
  const text = product.value.description;
  if (/<[a-z][\s\S]*>/i.test(text)) {
    return text; // It contains HTML tags, return directly for v-html
  } else {
    return text.replace(/\n/g, '<br/>'); // Preserve standard new lines
  }
});

// Color resolve helpers
const colorMap: Record<string, string> = {
  BLACK: '#000000', WHITE: '#FFFFFF', RED: '#FF0000', GREEN: '#00FF00', BLUE: '#0000FF',
  YELLOW: '#FFFF00', ORANGE: '#FFA500', PURPLE: '#800080', PINK: '#FFC0CB', BROWN: '#A52A2A',
  GREY: '#808080', GRAY: '#808080', SILVER: '#C0C0C0', GOLD: '#FFD700', CYAN: '#00FFFF',
  TRANG: '#FFFFFF', DO: '#FF0000', XANH: '#0000FF', VANG: '#FFD700', CAM: '#FFA500'
};

const resolveColorHex = (colorName?: string): string => {
  if (!colorName) return '#ccc';
  const normalized = colorName.trim().toUpperCase();
  return colorMap[normalized] ?? '#ccc';
};

const startAutoSlide = () => {
  if (!product.value?.responseList?.length) return;
  intervalId = setInterval(() => {
    const images = product.value?.responseList ?? [];
    if (images.length === 0) return;
    currentIndex.value = (currentIndex.value + 1) % images.length;
  }, 3000);
};

onMounted(() => {
  loadProductDetail();
  loadRelatedProducts();
});

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId);
});
</script>

<template>
  <HomeLayout/>

  <div class="breadcrumb-container">
    <nav class="breadcrumb">
      <router-link to="/customer/home">Trang chủ</router-link> <span>/</span>
      <router-link to="/customer/product-home">Sản phẩm</router-link> <span>/</span>
      <span>{{ product?.productName || "Đang tải..." }}</span>
    </nav>
  </div>

  <article class="product-detail">
    <div class="product-grid">
      <!-- Image Gallery -->
      <section class="image-gallery">
        <div class="main-image">
          <img
            :src="product?.responseList?.[currentIndex]?.url ?? 'https://cellphones.com.vn/iphone-16-pro-max.html'"
            :alt="product?.productName ?? 'Sản phẩm'"
          />
        </div>
        <div class="thumbnail-list" v-if="product?.responseList?.length">
          <div
            v-for="(img, idx) in product.responseList"
            :key="img.id"
            class="thumbnail"
            :class="{ active: currentIndex === idx }"
            @click="currentIndex = idx"
          >
            <img :src="img.url" alt="thumbnail" />
          </div>
        </div>
      </section>

      <!-- Product Info -->
      <section class="product-info">
        <span class="product-badge">🔥 Độc Quyền</span>
        <h1 class="product-title">{{ product?.productName }}</h1>

        <!-- Availability Details -->
        <div class="availability-info">
          <span class="info-label">Trạng thái: </span>
          <span class="info-value" :class="availableQuantity > 0 ? 'in-stock' : 'out-of-stock'">
            {{ availableQuantity > 0 ? 'Còn hàng' : 'Hết hàng' }}
          </span>
          <span class="divider">|</span>
          <span class="info-label">Tồn kho: </span>
          <span class="info-value font-bold">{{ availableQuantity }} sản phẩm</span>
        </div>

        <div class="current-price">{{ formatCurrency(selectedPrice ?? product?.price ?? 0) }}</div>

        <!-- RAM Selection -->
        <div class="spec-section" v-if="product?.rams?.length">
          <div class="spec-label"><span class="spec-icon">💾</span> Dung Lượng RAM</div>
          <div class="spec-options">
            <div v-for="ram in product.rams" :key="ram.id" class="spec-option"
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
                 :title="color.name"
                 @click="selectOption('color', color.id)">
              <span :style="{ backgroundColor: resolveColorHex(color.name) }">&nbsp;</span>
            </div>
          </div>
        </div>

        <!-- Origin Selection -->
        <div class="spec-section" v-if="product?.origins?.length">
          <div class="spec-label"><span class="spec-icon">🌍</span> Xuất xứ</div>
          <div class="spec-options">
            <div v-for="origin in product.origins" :key="origin.id" class="spec-option"
                 :class="{ selected: selectedOrigin === origin.id }"
                 @click="selectOption('origin', origin.id)">
              {{ origin.name }}
            </div>
          </div>
        </div>

        <!-- Screen Selection -->
        <div class="spec-section" v-if="product?.screens?.length">
          <div class="spec-label"><span class="spec-icon">📱</span> Màn hình</div>
          <div class="spec-options">
            <div v-for="scr in product.screens" :key="scr.id" class="spec-option"
                 :class="{ selected: selectedScreen === scr.id }"
                 @click="selectOption('screen', scr.id)">
              {{ scr.name }}
            </div>
          </div>
        </div>

        <!-- Camera Selection -->
        <div class="spec-section" v-if="product?.cameras?.length">
          <div class="spec-label"><span class="spec-icon">📷</span> Camera</div>
          <div class="spec-options">
            <div v-for="cam in product.cameras" :key="cam.id" class="spec-option"
                 :class="{ selected: selectedCamera === cam.id }"
                 @click="selectOption('camera', cam.id)">
              {{ cam.name }}
            </div>
          </div>
        </div>

        <!-- Quantity Selection -->
        <div class="spec-section">
          <div class="spec-label"><span class="spec-icon">🔢</span> Số lượng mua</div>
          <div class="quantity-options">
            <button class="qty-btn" @click="decreaseQty" :disabled="quantity <= 1">-</button>
            <input type="text" v-model.number="quantity" readonly/>
            <button class="qty-btn" @click="increaseQty" :disabled="quantity >= availableQuantity">+</button>
            <span class="stock-info">Tối đa có thể mua: {{ availableQuantity }}</span>
          </div>
        </div>

        <!-- Core Action Buttons -->
        <div class="action-buttons">
          <button
            class="btn btn-primary"
            :disabled="availableQuantity === 0"
            @click="buyNow"
          >
            🛍️ Mua Ngay
          </button>
          <button
            class="btn btn-secondary"
            :disabled="availableQuantity === 0"
            @click="addToCart(true)"
          >
            🛒 Thêm Vào Giỏ
          </button>
          <button
            class="btn btn-consult"
            @click="contactConsultation"
          >
            💬 Tư Vấn Ngay
          </button>
        </div>
      </section>
    </div>

    <!-- Details Tab System -->
    <div class="details-tab-system">
      <div class="tab-header">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'description' }"
          @click="activeTab = 'description'"
        >
          📖 Mô tả sản phẩm
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'specification' }"
          @click="activeTab = 'specification'"
        >
          ⚙️ Thông số kỹ thuật
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'reviews' }"
          @click="activeTab = 'reviews'"
        >
          ⭐ Đánh giá ({{ reviews.length }})
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'related' }"
          @click="activeTab = 'related'"
        >
          🔗 Sản phẩm liên quan
        </button>
      </div>

      <div class="tab-content animate-fade-in">
        <!-- Description Tab -->
        <div v-if="activeTab === 'description'" class="tab-pane rich-text">
          <div class="description-container" v-html="formattedDescription"></div>
        </div>

        <!-- Specification Tab -->
        <div v-if="activeTab === 'specification'" class="tab-pane spec-table-pane">
          <table class="specs-table">
            <tbody>
              <tr v-if="product?.rams?.length">
                <td class="spec-name">Dung Lượng RAM</td>
                <td class="spec-val">
                  <span v-for="(r, i) in product.rams" :key="r.id">
                    {{ r.name }}{{ i < product.rams.length - 1 ? ', ' : '' }}
                  </span>
                </td>
              </tr>
              <tr v-if="product?.screens?.length">
                <td class="spec-name">Công nghệ màn hình</td>
                <td class="spec-val">
                  <span v-for="(s, i) in product.screens" :key="s.id">
                    {{ s.name }}{{ i < product.screens.length - 1 ? ', ' : '' }}
                  </span>
                </td>
              </tr>
              <tr v-if="product?.cameras?.length">
                <td class="spec-name">Hệ thống camera</td>
                <td class="spec-val">
                  <span v-for="(c, i) in product.cameras" :key="c.id">
                    {{ c.name }}{{ i < product.cameras.length - 1 ? ', ' : '' }}
                  </span>
                </td>
              </tr>
              <tr v-if="product?.origins?.length">
                <td class="spec-name">Xuất xứ sản phẩm</td>
                <td class="spec-val">
                  <span v-for="(o, i) in product.origins" :key="o.id">
                    {{ o.name }}{{ i < product.origins.length - 1 ? ', ' : '' }}
                  </span>
                </td>
              </tr>
              <tr v-if="product?.colors?.length">
                <td class="spec-name">Phiên bản màu sắc</td>
                <td class="spec-val">
                  <span v-for="(col, i) in product.colors" :key="col.id">
                    {{ col.name }}{{ i < product.colors.length - 1 ? ', ' : '' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Reviews Tab -->
        <div v-if="activeTab === 'reviews'" class="tab-pane reviews-pane">
          <div class="reviews-summary">
            <div class="rating-num">4.7</div>
            <div class="rating-stars">⭐⭐⭐⭐⭐</div>
            <p>Được đánh giá dựa trên các khách hàng thực tế đã mua và trải nghiệm.</p>
          </div>

          <div class="reviews-list">
            <div v-for="rev in reviews" :key="rev.id" class="review-item">
              <div class="review-header">
                <span class="reviewer">{{ rev.author }}</span>
                <span class="review-date">{{ rev.date }}</span>
              </div>
              <div class="review-stars">
                {{ '⭐'.repeat(rev.rating) }}
              </div>
              <p class="review-content">{{ rev.content }}</p>
            </div>
          </div>
        </div>

        <!-- Related Products Tab -->
        <div v-if="activeTab === 'related'" class="tab-pane related-products-pane">
          <div v-if="relatedProducts.length === 0" class="no-related">
            Không có sản phẩm liên quan nào hiện tại.
          </div>
          <div class="related-grid" v-else>
            <div
              v-for="rel in relatedProducts"
              :key="rel.id"
              class="related-card"
              @click="navigateToRelated(rel.id)"
            >
              <div class="rel-img">
                <img :src="rel.url || 'https://cellphones.com.vn/iphone-16-pro-max.html'" :alt="rel.productName" />
              </div>
              <div class="rel-info">
                <h4>{{ rel.productName }}</h4>
                <div class="rel-price">{{ formatCurrency(rel.price) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </article>

  <Footer/>
</template>

<style scoped>
.breadcrumb-container {
  max-width: 1400px;
  margin: 0 auto;
}

.breadcrumb {
  background: white;
  padding: 15px 30px;
  border-radius: 12px;
  margin: 20px 20px 0 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  font-size: 14px;
}

.breadcrumb a {
  color: #007aff;
  text-decoration: none;
  font-weight: 500;
}

.breadcrumb span {
  color: #8e8e93;
  margin: 0 8px;
}

.product-detail {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  background: white;
  padding: 40px;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.image-gallery {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.main-image {
  width: 100%;
  height: 450px;
  background: #f8f9fa;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #f2f2f7;
}

.main-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s;
}

.main-image img:hover {
  transform: scale(1.05);
}

.thumbnail-list {
  display: flex;
  gap: 12px;
  margin-top: 15px;
  overflow-x: auto;
  width: 100%;
  padding-bottom: 5px;
}

.thumbnail {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  border: 2px solid transparent;
  background: #f8f9fa;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.thumbnail img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
}

.thumbnail.active {
  border-color: #007aff;
}

.product-info {
  display: flex;
  flex-direction: column;
}

.product-badge {
  background: #ffebe9;
  color: #ff3b30;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  align-self: flex-start;
}

.product-title {
  font-size: 28px;
  font-weight: 800;
  color: #1c1c1e;
  margin-top: 10px;
}

.availability-info {
  margin-top: 10px;
  font-size: 14px;
  color: #8e8e93;
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-value.in-stock {
  color: #34c759;
  font-weight: 600;
}

.info-value.out-of-stock {
  color: #ff3b30;
  font-weight: 600;
}

.divider {
  color: #e5e5ea;
}

.current-price {
  font-size: 32px;
  font-weight: 800;
  color: #ff3b30;
  margin: 15px 0;
}

.spec-section {
  margin-bottom: 20px;
}

.spec-label {
  font-weight: 600;
  font-size: 14px;
  color: #1c1c1e;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-option {
  padding: 8px 16px;
  border: 1px solid #e5e5ea;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
}

.spec-option:hover {
  border-color: #007aff;
  color: #007aff;
}

.spec-option.selected {
  background: #007aff;
  color: white;
  border-color: #007aff;
}

.color-options {
  display: flex;
  gap: 10px;
}

.color-option {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid transparent;
  padding: 2px;
  cursor: pointer;
}

.color-option span {
  display: block;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.color-option.selected {
  border-color: #007aff;
}

.quantity-options {
  display: flex;
  align-items: center;
  gap: 10px;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e5e5ea;
  background: #f8f9fa;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-options input {
  width: 50px;
  height: 36px;
  text-align: center;
  border: 1px solid #e5e5ea;
  border-radius: 8px;
  font-size: 15px;
}

.stock-info {
  font-size: 12px;
  color: #8e8e93;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 30px;
}

.btn {
  flex: 1;
  height: 50px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:disabled {
  background: #e5e5ea;
  color: #8e8e93;
  cursor: not-allowed;
}

.btn-primary {
  background: #ff3b30;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #e02b20;
}

.btn-secondary {
  background: #ffebe9;
  color: #ff3b30;
}

.btn-secondary:hover:not(:disabled) {
  background: #ffd6d3;
}

.btn-consult {
  background: #007aff;
  color: white;
}

.btn-consult:hover {
  background: #0056b3;
}

/* Detail Tabs */
.details-tab-system {
  background: white;
  margin-top: 2px;
  border-bottom-left-radius: 20px;
  border-bottom-right-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.tab-header {
  display: flex;
  border-top: 1px solid #f2f2f7;
  border-bottom: 1px solid #f2f2f7;
  background: #f8f9fa;
}

.tab-btn {
  flex: 1;
  padding: 15px 0;
  background: transparent;
  border: none;
  font-weight: 600;
  font-size: 15px;
  color: #8e8e93;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
}

.tab-btn.active {
  color: #007aff;
  border-bottom-color: #007aff;
  background: white;
}

.tab-content {
  padding: 40px;
}

.rich-text {
  line-height: 1.8;
  color: #2c2c2e;
  font-size: 15px;
}

.description-container {
  white-space: pre-wrap;
}

/* Specs Table */
.specs-table {
  width: 100%;
  border-collapse: collapse;
}

.specs-table td {
  padding: 12px 20px;
  border-bottom: 1px solid #f2f2f7;
  font-size: 14px;
}

.spec-name {
  font-weight: 600;
  color: #8e8e93;
  width: 250px;
}

.spec-val {
  color: #1c1c1e;
}

/* Reviews */
.reviews-summary {
  text-align: center;
  padding-bottom: 30px;
  border-bottom: 1px solid #f2f2f7;
}

.rating-num {
  font-size: 48px;
  font-weight: 800;
  color: #ffcc00;
}

.rating-stars {
  font-size: 20px;
  margin: 5px 0;
}

.reviews-list {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.reviewer {
  font-weight: 700;
  font-size: 14px;
}

.review-date {
  font-size: 12px;
  color: #8e8e93;
}

.review-stars {
  color: #ffcc00;
  margin-bottom: 8px;
}

.review-content {
  font-size: 14px;
  line-height: 1.5;
  color: #2c2c2e;
}

/* Related Products */
.related-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.related-card {
  border: 1px solid #f2f2f7;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.related-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.rel-img {
  height: 180px;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
}

.rel-img img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.rel-info {
  padding: 15px;
}

.rel-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1c1c1e;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rel-price {
  color: #ff3b30;
  font-weight: 700;
  font-size: 14px;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  .related-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .action-buttons {
    flex-direction: column;
  }
  .related-grid {
    grid-template-columns: 1fr;
  }
}
</style>
