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
import { useCartStore } from '@/cartStore';
import { toast } from "vue3-toastify";
import { formatCurrency } from "@/utils/Constant";
import {
  Home,
  ChevronRight,
  ChevronDown,
  ShieldCheck,
  Award,
  RotateCcw,
  Truck,
  Smartphone,
  Camera,
  ShoppingBag,
  ShoppingCart,
  Headphones,
  FileText,
  Cpu,
  Battery,
  HardDrive,
  Palette,
  Globe,
  Hash
} from '@lucide/vue'

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const productId = route.params.id as string;

const product = ref<ProductDetailResponse | null>(null);

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

const increaseQty = () => {
  if (quantity.value < availableQuantity.value) {
    quantity.value += 1;
  }
};
const decreaseQty = () => {
  if (quantity.value > 1) quantity.value -= 1;
};

const handleQuantityInput = (event: Event) => {
  const input = event.target as HTMLInputElement;
  // Replace any non-digits (prevent letters, special characters, negatives, decimals)
  let valStr = input.value.replace(/\D/g, "");

  if (valStr === "") {
    quantity.value = 1;
    input.value = "1";
    return;
  }

  let num = parseInt(valStr, 10);
  if (isNaN(num) || num < 1) {
    num = 1;
  }

  if (availableQuantity.value > 0 && num > availableQuantity.value) {
    toast.error(`Số lượng vượt quá tồn kho (Tối đa: ${availableQuantity.value})`);
    num = availableQuantity.value;
  }

  quantity.value = num;
  input.value = num.toString();
};

const handleQuantityBlur = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (!quantity.value || quantity.value < 1) {
    quantity.value = 1;
  }
  input.value = quantity.value.toString();
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
    case "color": {
      selectedColor.value = id;
      updateImagePreviewForColor(id);
      startAutoSlide();
      break;
    }
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

  if (quantity.value > availableQuantity.value) {
    toast.error(`Số lượng vượt quá tồn kho (Tối đa: ${availableQuantity.value})`);
    quantity.value = availableQuantity.value;
    return false;
  }

  const cartRequest = new CartRequest('', productVariantId.value, quantity.value);

  try {
    await cartService.addToCart(cartRequest.toPayload());
    await cartStore.fetchCartCount();
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

const handleBuyNow = () => {
  if (!product.value || quantity.value < 1) return;

  if (!productVariantId.value) {
    toast.error('Vui lòng chọn đầy đủ phiên bản sản phẩm');
    return;
  }

  if (quantity.value > availableQuantity.value) {
    toast.error(`Số lượng vượt quá tồn kho (Tối đa: ${availableQuantity.value})`);
    return;
  }

  // Lấy ảnh đại diện đang active
  const activeImageUrl = product.value.images?.[currentIndex.value]?.name || '';

  // Lấy thông tin thuộc tính
  const selectedColorObj = product.value.colors?.find(c => c.id === selectedColor.value);
  const selectedRamObj = product.value.rams?.find(r => r.id === selectedRam.value);
  const selectedOriginObj = product.value.origins?.find(o => o.id === selectedOrigin.value);

  // Tạo cấu trúc CartItem cho buyNowCart
  const buyNowItem = {
    idCartItem: 'buynow_temp',
    productId: productVariantId.value, // variant ID
    productName: product.value.productName,
    ram: selectedRamObj ? selectedRamObj.name : '',
    color: selectedColorObj ? selectedColorObj.name : '',
    origin: selectedOriginObj ? selectedOriginObj.name : '',
    image: activeImageUrl,
    price: selectedPrice.value ?? product.value.price ?? 0,
    quantity: quantity.value,
    selected: true
  };

  // Xóa dữ liệu cũ và lưu buyNowCart mới vào sessionStorage
  sessionStorage.removeItem('buyNowCart');
  sessionStorage.setItem('buyNowCart', JSON.stringify([buyNowItem]));

  router.push({ name: 'cart' });
};

const contactConsultation = () => {
  const chatBtn = document.querySelector('.chat-button') as HTMLButtonElement;
  if (chatBtn) {
    chatBtn.click();
  } else {
    toast.info("Đang kết nối với nhân viên tư vấn qua Chat Realtime...");
  }
};

// Collapse state for long descriptions
const showFullDescription = ref(false);
const hasLongDescription = computed(() => {
  return (product.value?.description?.length ?? 0) > 600;
});

// Safe rendering helper for description
const formattedDescription = computed(() => {
  if (!product.value?.description) return "Chưa có mô tả cho sản phẩm này.";
  const text = product.value.description;
  if (/<[a-z][\s\S]*>/i.test(text)) {
    return text;
  } else {
    return text.replace(/\n/g, '<br/>');
  }
});

// Dynamic Highlights generator using only real data returned by the API
const productHighlights = computed(() => {
  if (!product.value) return [];
  const list: any[] = [];
  
  // 1. CPU / Chip
  if (product.value.cpus && product.value.cpus.length > 0 && product.value.cpus[0]?.name) {
    list.push({
      id: 1,
      title: 'Bộ vi xử lý',
      desc: product.value.cpus[0].name,
      icon: Cpu
    });
  }
  
  // 2. Battery / Pin
  if (product.value.batterys && product.value.batterys.length > 0 && product.value.batterys[0]?.name) {
    list.push({
      id: 2,
      title: 'Dung lượng pin',
      desc: product.value.batterys[0].name,
      icon: Battery
    });
  }
  
  // 3. Screen / Màn hình (Has real data)
  if (product.value.screens && product.value.screens.length > 0 && product.value.screens[0]?.name) {
    list.push({
      id: 3,
      title: 'Màn hình',
      desc: product.value.screens[0].name,
      icon: Smartphone
    });
  }
  
  // 4. Camera (Has real data)
  if (product.value.cameras && product.value.cameras.length > 0 && product.value.cameras[0]?.name) {
    list.push({
      id: 4,
      title: 'Hệ thống camera',
      desc: product.value.cameras[0].name,
      icon: Camera
    });
  }
  
  return list;
});

// Get capacity display prices dynamically from variants list
const getRamDisplayPrice = (ramId: string) => {
  if (!product.value || !product.value.variants) return product.value?.price ?? 0;

  // Find first variant matching this ramId and currently selected other specs (if exists)
  // to show a highly accurate price for that ROM option
  const matched = product.value.variants.find(v =>
    v.ramId === ramId &&
    v.colorId === selectedColor.value &&
    v.originId === selectedOrigin.value &&
    v.screenId === selectedScreen.value &&
    v.cameraId === selectedCamera.value
  );
  if (matched) return matched.price;

  // Fallback to first variant matching just this ramId
  const fallbackMatched = product.value.variants.find(v => v.ramId === ramId);
  return fallbackMatched ? fallbackMatched.price : (product.value.price ?? 0);
}

// Color resolve helpers
const colorMap: Record<string, string> = {
  BLACK: '#1f2937', WHITE: '#f9fafb', RED: '#ef4444', GREEN: '#10b981', BLUE: '#3b82f6',
  YELLOW: '#eab308', ORANGE: '#f97316', PURPLE: '#a855f7', PINK: '#ec4899', BROWN: '#78350f',
  GREY: '#6b7280', GRAY: '#6b7280', SILVER: '#e2e8f0', GOLD: '#fbbf24', CYAN: '#06b6d4',
  TRANG: '#ffffff', DO: '#ef4444', XANH: '#3b82f6', VANG: '#fbbf24', CAM: '#f97316',
  'TITAN TỰ NHIÊN': '#c5bbae', 'TITAN XANH': '#2f435a', 'TITAN TRẮNG': '#eceae6', 'TITAN ĐEN': '#3c3d3a',
  'XANH ĐẬM': '#0f172a', 'XANH_DAM': '#0f172a',
  MAGENTA: "#FF00FF", NAVY: "#000080", LIME: "#00FF00", TEAL: "#008080", OLIVE: "#808000",
  MAROON: "#800000", CORAL: "#FF7F50", TURQUOISE: "#40E0D0", INDIGO: "#4B0082", VIOLET: "#EE82EE",
  BEIGE: "#F5F5DC", TAN: "#D2B48C", CHOCOLATE: "#D2691E", SALMON: "#FA8072", KHAKI: "#F0E68C",
  MINT: "#98FF98", PEACH: "#FFE5B4", VANG_NHAT: "#fef08a", MAU_DEN: "#000000", XANH_DA_TROI: "#38bdf8"
};

const translateColorName = (name: string): string => {
  const normalized = name.toUpperCase().trim();

  if (normalized === 'BLACK' || normalized === '#000000' || normalized === 'DEN' || normalized === 'BLACK_COLOR' || normalized === 'TITAN ĐEN') return 'Đen';
  if (normalized === 'WHITE' || normalized === '#FFFFFF' || normalized === 'TRANG' || normalized === 'TITAN TRẮNG') return 'Trắng';
  if (normalized === 'RED' || normalized === '#FF0000' || normalized === 'DO') return 'Đỏ';
  if (normalized === 'GREEN' || normalized === '#00FF00' || normalized === '#00A651' || normalized === 'XANH_LA' || normalized === 'XANH LA') return 'Xanh lá';
  if (normalized === 'BLUE' || normalized === '#0000FF' || normalized === 'XANH') return 'Xanh dương';
  if (normalized === 'YELLOW' || normalized === '#FFFF00' || normalized === 'VANG') return 'Vàng';
  if (normalized === 'ORANGE' || normalized === '#FFA500' || normalized === 'CAM') return 'Cam';
  if (normalized === 'PINK' || normalized === '#FFC0CB' || normalized === 'HONG') return 'Hồng';
  if (normalized === 'PURPLE' || normalized === '#800080' || normalized === 'TIM') return 'Tím';
  if (normalized === 'GREY' || normalized === 'GRAY' || normalized === '#808080' || normalized === 'XAM') return 'Xám';
  if (normalized === 'SILVER' || normalized === '#C0C0C0' || normalized === 'BAC') return 'Bạc';
  if (normalized === 'GOLD' || normalized === '#FFD700') return 'Vàng Gold';
  if (normalized === 'BROWN' || normalized === '#8B4513' || normalized === 'NAU') return 'Nâu';
  if (normalized === 'CHOCOLATE' || normalized === '#D2691E') return 'Nâu socola';
  if (normalized === 'NAVY' || normalized === '#000080' || normalized === 'XANH_DAM' || normalized === 'XANH DAM' || normalized === 'TITAN XANH') return 'Xanh dương đậm';
  if (normalized === 'TEAL' || normalized === '#008080') return 'Xanh lục lam';
  if (normalized === 'TURQUOISE' || normalized === '#40E0D0') return 'Xanh ngọc';
  if (normalized === 'VIOLET' || normalized === '#EE82EE') return 'Tím violet';
  if (normalized === 'CORAL' || normalized === '#FF7F50') return 'Đỏ san hô';
  if (normalized === 'LIME') return 'Xanh chanh';
  if (normalized === 'OLIVE') return 'Xanh ô liu';
  if (normalized === 'MAROON') return 'Đỏ hạt dẻ';
  if (normalized === 'INDIGO') return 'Xanh chàm';
  if (normalized === 'BEIGE') return 'Kem';
  if (normalized === 'TAN') return 'Nâu sáng';
  if (normalized === 'SALMON') return 'Hồng cam';
  if (normalized === 'KHAKI') return 'Vàng kaki';
  if (normalized === 'MINT') return 'Xanh bạc hà';
  if (normalized === 'PEACH') return 'Màu đào';
  if (normalized === 'TITAN TỰ NHIÊN') return 'Titan tự nhiên';
  if (normalized === 'VANG_NHAT' || normalized === 'VÀNG NHẠT') return 'Vàng nhạt';
  if (normalized === 'MAU_DEN' || normalized === 'MÀU ĐEN') return 'Đen';
  if (normalized === 'XANH_DA_TROI' || normalized === 'XANH DA TRỜI') return 'Xanh da trời';

  return name;
};

const getSelectedColorName = () => {
  if (!product.value || !selectedColor.value) return "";
  const colorObj = product.value.colors.find(c => c.id === selectedColor.value);
  return colorObj ? translateColorName(colorObj.name) : "";
};

const resolveColorHex = (colorName?: string): string => {
  if (!colorName) return '#ccc';
  const trimmed = colorName.trim();
  if (trimmed.startsWith('#') || trimmed.startsWith('rgb') || trimmed.startsWith('hsl')) {
    return trimmed;
  }
  const normalized = trimmed.toUpperCase();
  return colorMap[normalized] ?? '#ccc';
};

const stopAutoSlide = () => {
  if (intervalId) {
    clearInterval(intervalId);
    intervalId = null;
  }
};

const startAutoSlide = () => {
  if (!product.value?.images?.length) return;
  stopAutoSlide();
  intervalId = setInterval(() => {
    const imgList = product.value?.images ?? [];
    if (imgList.length === 0) return;
    currentIndex.value = (currentIndex.value + 1) % imgList.length;
  }, 4000);
};

const updateImagePreviewForColor = (colorId: string) => {
  if (!product.value || !product.value.images || !product.value.colors) return;
  const colorObj = product.value.colors.find(c => c.id === colorId);
  if (!colorObj) return;

  const colorName = colorObj.name.toLowerCase().trim();
  let matchedIndex = -1;

  // Mapping of common hex codes or color names to search keywords in image URLs
  const colorKeywordsMap: Record<string, string[]> = {
    '#ff0000': ['red', 'do', 'đỏ'],
    '#ffffff': ['white', 'trang', 'trắng'],
    '#000000': ['black', 'den', 'đen'],
    '#3c3d3a': ['black', 'den', 'đen', 'titan', 'grey', 'gray', 'xam', 'xám'],
    '#c5bbae': ['titan', 'tu nhien', 'tự nhiên', 'gold', 'vang', 'vàng'],
    '#2f435a': ['blue', 'xanh', 'titan'],
    '#eceae6': ['white', 'trang', 'trắng', 'titan']
  };

  const keywords = colorKeywordsMap[colorName] || [colorName];

  // 1. Try to match by keywords in image URLs
  for (let i = 0; i < product.value.images.length; i++) {
    const img = product.value.images[i];
    if (img && img.name) {
      const imgUrl = img.name.toLowerCase();
      if (keywords.some(kw => imgUrl.includes(kw))) {
        matchedIndex = i;
        break;
      }
    }
  }

  // 2. Fall back to matching color option index with image list index
  if (matchedIndex === -1) {
    const colorIndex = product.value.colors.findIndex(c => c.id === colorId);
    if (colorIndex !== -1 && colorIndex < product.value.images.length) {
      matchedIndex = colorIndex;
    }
  }

  if (matchedIndex !== -1) {
    currentIndex.value = matchedIndex;
  }
};

onMounted(() => {
  loadProductDetail();
});

onUnmounted(() => {
  stopAutoSlide();
});
</script>

<template>
  <HomeLayout/>

  <!-- Breadcrumbs Area -->
  <div class="breadcrumb-container">
    <nav class="breadcrumb">
      <router-link to="/customer/home" class="breadcrumb-home-link">
        <Home :size="16" />
        <span>Trang chủ</span>
      </router-link>
      <ChevronRight :size="14" class="breadcrumb-chevron" />
      <router-link to="/customer/product-home">Sản phẩm</router-link>
      <ChevronRight :size="14" class="breadcrumb-chevron" />
      <span class="breadcrumb-current">{{ product?.productName || "Đang tải..." }}</span>
    </nav>
  </div>

  <article class="product-detail">
    <div class="product-grid">
      
      <!-- Left Column: Vertical gallery & Trust badges -->
      <section class="image-gallery">
        <div class="gallery-wrapper">
          <!-- Vertical Thumbnail list on the left -->
          <div class="vertical-thumbnail-list" v-if="product?.images?.length">
            <div
              v-for="(img, idx) in product.images"
              :key="img.id"
              class="thumbnail"
              :class="{ active: currentIndex === idx }"
              @click="currentIndex = idx; startAutoSlide();"
            >
              <img :src="img.name" alt="thumbnail" />
            </div>
            <button class="thumbnail-scroll-down-btn" aria-label="Xem thêm ảnh">
              <ChevronDown :size="16" />
            </button>
          </div>
          
          <!-- Main Product Image on the right -->
          <div class="main-image">
            <img
              :src="product?.images?.[currentIndex]?.name ?? 'https://cellphones.com.vn/iphone-16-pro-max.html'"
              :alt="product?.productName ?? 'Sản phẩm'"
            />
          </div>
        </div>

        <!-- Trust Badges Bar (Below the main gallery, within left section) -->
        <div class="trust-badges-bar">
          <div class="trust-badge-item">
            <ShieldCheck :size="20" class="trust-icon" />
            <div class="trust-text">
              <strong>Hàng chính hãng</strong>
              <span>Apple Việt Nam</span>
            </div>
          </div>
          
          <div class="trust-badge-item">
            <Award :size="20" class="trust-icon" />
            <div class="trust-text">
              <strong>Bảo hành 12 tháng</strong>
              <span>chính hãng</span>
            </div>
          </div>
          
          <div class="trust-badge-item">
            <RotateCcw :size="20" class="trust-icon" />
            <div class="trust-text">
              <strong>Đổi trả 30 ngày</strong>
              <span>nếu có lỗi</span>
            </div>
          </div>
          
          <div class="trust-badge-item">
            <Truck :size="20" class="trust-icon" />
            <div class="trust-text">
              <strong>Giao hàng nhanh</strong>
              <span>toàn quốc</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Right Column: Specs selection & Buttons -->
      <section class="product-info">
        <span class="product-badge">🔥 Độc Quyền</span>
        <h1 class="product-title">{{ product?.productName }}</h1>

        <!-- Availability Details -->
        <div class="availability-info" :class="{ 'in-stock': availableQuantity > 0, 'out-of-stock': availableQuantity === 0 }">
          <span class="in-stock-dot">{{ availableQuantity > 0 ? '🟢' : '🔴' }}</span>
          <span class="info-value font-semibold">{{ availableQuantity > 0 ? 'Còn hàng' : 'Hết hàng' }}</span>
        </div>

        <!-- Price displaying dynamic or selected pricing -->
        <div class="current-price">{{ formatCurrency(selectedPrice ?? product?.price ?? 0) }}</div>

        <!-- Dung lượng (Rams/Capacity Selection in Grid with individual prices) -->
        <div class="spec-section" v-if="product?.rams?.length">
          <div class="spec-label">
            <HardDrive :size="18" class="spec-icon-lucide" />
            <span>ROM</span>
          </div>
          <div class="capacity-grid">
            <button
              v-for="ram in product.rams"
              :key="ram.id"
              class="capacity-card"
              :class="{ selected: selectedRam === ram.id }"
              @click="selectOption('ram', ram.id)"
            >
              <div class="capacity-name">{{ ram.name }}</div>
              <div class="capacity-price">{{ formatCurrency(getRamDisplayPrice(ram.id)) }}</div>
            </button>
          </div>
        </div>

        <!-- Màu sắc (Colors Selection with text labels underneath) -->
        <div class="spec-section" v-if="product?.colors?.length">
          <div class="spec-label">
            <Palette :size="18" class="spec-icon-lucide" />
            <span>Màu sắc: </span>
            <span class="selected-color-name" style="margin-left: 6px; font-weight: 700; color: #2563eb;">
              {{ getSelectedColorName() }}
            </span>
          </div>
          <div class="color-options-row">
            <button
              v-for="color in product.colors"
              :key="color.id"
              class="color-option-wrapper"
              @click="selectOption('color', color.id)"
              :title="translateColorName(color.name)"
            >
              <div
                class="color-circle"
                :class="{ selected: selectedColor === color.id }"
                :style="{ backgroundColor: resolveColorHex(color.name) }"
              >
                &nbsp;
              </div>
            </button>
          </div>
        </div>

        <!-- Xuất xứ (Origin Selection with layout gaps resolved) -->
        <div class="spec-section" v-if="product?.origins?.length">
          <div class="spec-label">
            <Globe :size="18" class="spec-icon-lucide" />
            <span>Xuất xứ</span>
          </div>
          <div class="spec-options">
            <button 
              v-for="origin in product.origins" 
              :key="origin.id" 
              class="spec-option-tag"
              :class="{ selected: selectedOrigin === origin.id }"
              @click="selectOption('origin', origin.id)"
            >
              Chính hãng {{ origin.name }}
            </button>
          </div>
        </div>

        <!-- Technical Specs Summary cards in a grid -->
        <div class="specs-summary-grid">
          <div class="spec-summary-card">
            <div class="card-icon-circle">
              <Smartphone :size="20" />
            </div>
            <div class="card-text">
              <span class="card-label">Màn hình</span>
              <span class="card-value">
                {{ product?.screens?.[0]?.name || 'Liquid Retina XDR' }}
              </span>
            </div>
          </div>

          <div class="spec-summary-card">
            <div class="card-icon-circle">
              <Camera :size="20" />
            </div>
            <div class="card-text">
              <span class="card-label">Camera</span>
              <span class="card-value">
                {{ product?.cameras?.[0]?.name || 'Camera chính 48MP' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Quantity Selection -->
        <div class="spec-section">
          <div class="spec-label">
            <Hash :size="18" class="spec-icon-lucide" />
            <span>Số lượng</span>
          </div>
          <div class="quantity-options-row">
            <div class="qty-counter-input">
              <button class="qty-change-btn" @click="decreaseQty" :disabled="quantity <= 1">-</button>
              <input type="text" :value="quantity" @input="handleQuantityInput" @blur="handleQuantityBlur" />
              <button class="qty-change-btn" @click="increaseQty" :disabled="quantity >= availableQuantity">+</button>
            </div>
            <span class="stock-info-text">Tối đa có thể mua: {{ availableQuantity }}</span>
          </div>
        </div>

        <!-- Core Action Buttons -->
        <div class="action-buttons-row">
          <button
            class="action-btn buy-now-btn"
            :disabled="availableQuantity === 0"
            @click="handleBuyNow"
            :title="availableQuantity === 0 ? 'Sản phẩm hiện đã hết hàng' : ''"
          >
            <ShoppingBag :size="20" />
            <div class="btn-double-text">
              <strong>Mua ngay</strong>
              <span>Giao hàng tận nơi</span>
            </div>
          </button>

          <button
            class="action-btn add-to-cart-btn"
            :disabled="availableQuantity === 0"
            @click="addToCart(true)"
            :title="availableQuantity === 0 ? 'Sản phẩm hiện đã hết hàng' : ''"
          >
            <ShoppingCart :size="20" />
            <span>Thêm vào giỏ</span>
          </button>

          <button
            class="action-btn consult-btn"
            @click="contactConsultation"
          >
            <Headphones :size="20" />
            <div class="btn-double-text">
              <strong>Tư vấn ngay</strong>
              <span>Hỗ trợ 24/7</span>
            </div>
          </button>
        </div>

        <div class="out-of-stock-alert" v-if="availableQuantity === 0">
          ⚠️ Sản phẩm hiện đã hết hàng. Vui lòng chọn phiên bản khác!
        </div>
      </section>

    </div>

    <!-- Product Description Section -->
    <div class="product-description-section">
      <div class="section-header">
        <FileText :size="20" />
        <h3>Mô tả sản phẩm</h3>
      </div>

      <div class="section-content rich-text" :class="{ 'no-sidebar': !productHighlights.length }">
        <div class="description-col">
          <div class="description-wrapper" :class="{ collapsed: !showFullDescription && hasLongDescription }">
            <div class="description-container" v-html="formattedDescription"></div>
            <div class="description-overlay" v-if="!showFullDescription && hasLongDescription"></div>
          </div>
          <div class="show-more-bar" v-if="hasLongDescription">
            <button @click="showFullDescription = !showFullDescription" class="show-more-btn">
              <span>{{ showFullDescription ? 'Thu gọn nội dung' : 'Xem thêm chi tiết sản phẩm' }}</span>
              <ChevronDown :size="16" :class="{ rotate: showFullDescription }" />
            </button>
          </div>
        </div>

        <!-- Highlight sidebar items: Only displayed if real specifications are present -->
        <div class="highlights-sidebar" v-if="productHighlights.length">
          <h4 class="sidebar-title">Đặc điểm nổi bật</h4>
          <div
            v-for="item in productHighlights"
            :key="item.id"
            class="highlight-item-card"
          >
            <div class="highlight-icon-box">
              <component :is="item.icon" :size="20" />
            </div>
            <div class="highlight-text-box">
              <h5 class="highlight-title">{{ item.title }}</h5>
              <p class="highlight-desc">{{ item.desc }}</p>
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
  max-width: 1280px;
  margin: 0 auto;
}

.breadcrumb {
  background: transparent;
  padding: 24px 0 10px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4b5563;
}

.breadcrumb-home-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4b5563;
  text-decoration: none;
  font-weight: 500;
}

.breadcrumb-home-link:hover {
  color: #3b82f6;
}

.breadcrumb a {
  color: #4b5563;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.breadcrumb a:hover {
  color: #3b82f6;
}

.breadcrumb-chevron {
  color: #9ca3af;
}

.breadcrumb-current {
  color: #9ca3af;
  font-weight: 500;
}

.product-detail {
  max-width: 1280px;
  margin: 10px auto 40px auto;
}

.product-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 36px;
  background: white;
  padding: 32px;
  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.02);
}

.image-gallery {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.gallery-wrapper {
  display: flex;
  gap: 20px;
  width: 100%;
  flex-grow: 1;
}

.vertical-thumbnail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 76px;
  flex-shrink: 0;
}

.thumbnail {
  width: 76px;
  height: 76px;
  border-radius: 12px;
  border: 1.5px solid transparent;
  background: #f8fafc;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.2s;
}

.thumbnail:hover {
  border-color: #cbd5e1;
}

.thumbnail.active {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.thumbnail img {
  max-width: 85%;
  max-height: 85%;
  object-fit: contain;
}

.thumbnail-scroll-down-btn {
  background: #f1f5f9;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  margin: 4px auto 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  cursor: pointer;
  transition: background-color 0.2s;
}

.thumbnail-scroll-down-btn:hover {
  background: #e2e8f0;
}

.main-image {
  flex-grow: 1;
  height: 100%;
  min-height: 420px;
  background: #f8fafc;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #f1f5f9;
}

.main-image img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.main-image img:hover {
  transform: scale(1.03);
}

/* Trust Badges Bar styling */
.trust-badges-bar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
  margin-top: 24px;
  border: 1px solid #f1f5f9;
}

.trust-badge-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trust-icon {
  color: #10b981;
  flex-shrink: 0;
}

.trust-text {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.trust-text strong {
  font-size: 11px;
  color: #1e293b;
  font-weight: 700;
}

.trust-text span {
  font-size: 10px;
  color: #64748b;
  font-weight: 500;
}

/* Right Section details */
.product-info {
  display: flex;
  flex-direction: column;
}

.product-badge {
  background: #ffebe9;
  color: #ff3b30;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 0.72rem;
  font-weight: 700;
  align-self: flex-start;
}

.product-title {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  margin-top: 12px;
  line-height: 1.3;
}

.availability-info {
  margin-top: 8px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.availability-info.in-stock {
  color: #10b981;
}

.availability-info.out-of-stock {
  color: #ef4444;
}

.in-stock-dot {
  font-size: 10px;
}

.out-of-stock-alert {
  margin-top: 16px;
  padding: 12px 16px;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price {
  font-size: 32px;
  font-weight: 800;
  color: #ef4444;
  margin: 16px 0;
}

.spec-section {
  margin-bottom: 20px;
}

.spec-label {
  font-weight: 700;
  font-size: 13px;
  color: #1e293b;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.spec-icon-lucide {
  color: #475569;
  flex-shrink: 0;
}

/* Capacity Selection card style */
.capacity-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.capacity-card {
  padding: 12px 8px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.capacity-card:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.capacity-card.selected {
  border-color: #3b82f6;
  background: #eff6ff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.08);
}

.capacity-name {
  font-size: 13px;
  font-weight: 800;
  color: #1e293b;
}

.capacity-card.selected .capacity-name {
  color: #2563eb;
}

.capacity-price {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
}

.capacity-card.selected .capacity-price {
  color: #2563eb;
}

/* Color select with names */
.color-options-row {
  display: flex;
  gap: 18px;
}

.color-option-wrapper {
  background: transparent;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 2px;
}

.color-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  padding: 2px;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.2s;
  position: relative;
}

.color-circle.selected {
  border-color: #3b82f6;
  transform: scale(1.08);
}

.color-circle.selected::after {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 1.5px solid #3b82f6;
  pointer-events: none;
}

.color-name-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
}

.color-name-label.selected {
  color: #2563eb;
  font-weight: 700;
}

/* Origin tag style: separated buttons */
.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.spec-option-tag {
  padding: 8px 16px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
}

.spec-option-tag:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.spec-option-tag.selected {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #2563eb;
}

/* Screen & Camera highlights cards */
.specs-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin: 10px 0 20px 0;
}

.spec-summary-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.card-icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  flex-shrink: 0;
}

.card-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.card-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.card-value {
  font-size: 12px;
  color: #1e293b;
  font-weight: 700;
  line-height: 1.3;
}

/* Quantity style */
.quantity-options-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.qty-counter-input {
  display: flex;
  align-items: center;
  border: 1.5px solid #cbd5e1;
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
  height: 38px;
}

.qty-change-btn {
  width: 36px;
  height: 100%;
  background: transparent;
  border: none;
  font-size: 18px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: background-color 0.2s;
}

.qty-change-btn:hover:not(:disabled) {
  background: #f1f5f9;
}

.qty-counter-input input {
  width: 42px;
  height: 100%;
  text-align: center;
  border: none;
  border-left: 1.5px solid #cbd5e1;
  border-right: 1.5px solid #cbd5e1;
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.stock-info-text {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

/* Core Actions double text button */
.action-buttons-row {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.action-btn {
  flex: 1;
  height: 52px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.2s;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.buy-now-btn {
  background: #ef4444;
  color: #ffffff;
  flex: 1.35;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.buy-now-btn:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-1px);
}

.add-to-cart-btn {
  background: #ffffff;
  border: 1.5px solid #10b981;
  color: #10b981;
  font-weight: 700;
  font-size: 13px;
}

.add-to-cart-btn:hover:not(:disabled) {
  background: #f0fdf4;
}

.consult-btn {
  background: #ffffff;
  border: 1.5px solid #475569;
  color: #475569;
  font-weight: 700;
}

.consult-btn:hover {
  background: #f8fafc;
}

.btn-double-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  line-height: 1.25;
}

.btn-double-text strong {
  font-size: 13px;
  font-weight: 700;
}

.btn-double-text span {
  font-size: 10px;
  font-weight: 500;
  opacity: 0.85;
}

/* Product Description Section */
.product-description-section {
  margin-top: 32px;
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.01);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  color: #1e293b;
  font-weight: 700;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
}

.section-content {
  padding: 32px;
}

/* Split content for description section */
.section-content.rich-text {
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  gap: 40px;
  align-items: start;
}

.section-content.rich-text.no-sidebar {
  grid-template-columns: 1fr;
}

.description-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.description-wrapper {
  position: relative;
  transition: max-height 0.4s ease;
  overflow: hidden;
}

.description-wrapper.collapsed {
  max-height: 420px;
}

.description-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0), rgba(255, 255, 255, 1));
  pointer-events: none;
}

.description-container {
  font-size: 14.5px;
  line-height: 1.8;
  color: #334155;
  text-align: justify;
}

.description-container p {
  margin-bottom: 16px;
}

.show-more-bar {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.show-more-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f8fafc;
  color: #2563eb;
  border: 1px solid #e2e8f0;
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s ease;
}

.show-more-btn:hover {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1d4ed8;
}

.show-more-btn svg {
  transition: transform 0.3s ease;
}

.show-more-btn svg.rotate {
  transform: rotate(180deg);
}

.highlights-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 24px;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 4px 0;
  text-align: left;
}

.highlight-item-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.highlight-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.02);
}

.highlight-icon-box {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  flex-shrink: 0;
}

.highlight-text-box {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.highlight-title {
  font-size: 13px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.highlight-desc {
  font-size: 10.5px;
  color: #64748b;
  margin: 0;
  font-weight: 500;
  line-height: 1.3;
}

/* Responsive breakpoint styling */
@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .section-content.rich-text {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .vertical-thumbnail-list {
    display: none;
  }

  .trust-badges-bar {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .action-buttons-row {
    flex-direction: column;
    gap: 10px;
  }

  .action-btn {
    width: 100%;
  }
}
</style>
