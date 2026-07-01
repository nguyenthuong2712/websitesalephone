<script setup lang="ts">
import {ref, onMounted, computed} from "vue";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import {productService} from "@/service/ProductService";
import {Search} from "@/models/Search.ts";
import {formatCurrency} from "@/utils/Constant.ts";
import {toast} from "vue3-toastify";
import {
  Search as SearchIcon,
  SlidersHorizontal,
  Heart,
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  X,
  RefreshCw
} from "@lucide/vue";

const products = ref<any[]>([]);
const searchText = ref("");
const page = ref(1);
const size = ref(12);
const totalPages = ref<number>(1);
const totalProducts = ref(0);
const loading = ref(false);

const minPriceInput = ref("");
const maxPriceInput = ref("");
const minPrice = ref<number | null>(null);
const maxPrice = ref<number | null>(null);
const sortBy = ref("newest");
const showMobileFilters = ref(false);

const loadProducts = async () => {
  loading.value = true;
  let searchMin = minPrice.value;
  let searchMax = maxPrice.value;

  if (minPrice.value !== null && maxPrice.value !== null && minPrice.value > maxPrice.value) {
    searchMax = null;
  }

  const search = new Search(
    page.value,
    size.value,
    searchText.value,
    '',
    searchMin,
    searchMax,
    null,
    null,
    null,
    null,
    sortBy.value
  );

  try {
    const res = await productService.search(search);
    products.value = res.data.data.content || [];
    totalPages.value = Number(res.data.data.totalPages ?? 1);
    totalProducts.value = Number(res.data.data.totalElements ?? 0);
  } catch (error) {
    console.error("Lỗi khi load sản phẩm:", error);
  } finally {
    loading.value = false;
  }
};

const onSearch = () => {
  page.value = 1;
  loadProducts();
};

const onFilterChange = () => {
  if (minPrice.value !== null && maxPrice.value !== null && minPrice.value > maxPrice.value) {
    toast.error("Giá tối thiểu không được lớn hơn giá tối đa");
    return;
  }
  page.value = 1;
  loadProducts();
};

const handleMinPriceInput = (event: Event) => {
  const input = event.target as HTMLInputElement;
  const rawValue = input.value.replace(/\D/g, "");
  minPrice.value = rawValue ? parseInt(rawValue, 10) : null;
  minPriceInput.value = formatPriceInput(input.value);
};

const handleMaxPriceInput = (event: Event) => {
  const input = event.target as HTMLInputElement;
  const rawValue = input.value.replace(/\D/g, "");
  maxPrice.value = rawValue ? parseInt(rawValue, 10) : null;
  maxPriceInput.value = formatPriceInput(input.value);
};

const formatPriceInput = (val: string) => {
  const clean = val.replace(/\D/g, "");
  if (!clean) return "";
  return new Intl.NumberFormat("vi-VN").format(parseInt(clean, 10));
};

const clearFilters = () => {
  minPriceInput.value = "";
  maxPriceInput.value = "";
  minPrice.value = null;
  maxPrice.value = null;
  sortBy.value = "newest";
  searchText.value = "";
  page.value = 1;
  loadProducts();
};

const onPageChange = (newPage: number) => {
  newPage = Number(newPage);
  if (newPage < 1) newPage = 1;
  if (newPage > totalPages.value) newPage = totalPages.value;
  if (newPage === page.value) return;
  page.value = newPage;
  loadProducts();
};

const activeFiltersCount = computed(() => {
  let count = 0;
  if (minPrice.value !== null) count++;
  if (maxPrice.value !== null) count++;
  if (searchText.value) count++;
  return count;
});

onMounted(loadProducts);
</script>

<template>
  <HomeLayout/>
  <div class="products-page-wrapper">
    <div class="container">

      <!-- Breadcrumbs at the top -->
      <div class="breadcrumb-top-alt">
        <router-link to="/" class="breadcrumb-link-alt">Trang chủ</router-link>
        <span class="breadcrumb-separator-alt">/</span>
        <span class="breadcrumb-current-alt">Sản phẩm</span>
      </div>

      <!-- Search, Filter Summary & Sort Row -->
      <div class="search-filter-row-alt">
        <div class="search-input-wrapper-alt">
          <SearchIcon :size="18" class="search-inner-icon-left" />
          <input
            v-model="searchText"
            type="text"
            class="search-input-alt"
            placeholder="Tìm kiếm sản phẩm..."
            @keyup.enter="onSearch"
          />
          <button class="search-action-btn-alt" @click="onSearch" aria-label="Tìm kiếm">
            <SearchIcon :size="16" />
          </button>
        </div>

        <div class="filter-actions-group-alt">
          <button class="white-action-btn-alt filter-btn-alt mobile-only" @click="showMobileFilters = true">
            <span>Bộ lọc</span>
            <SlidersHorizontal :size="16" />
          </button>

          <div class="sort-select-wrapper-alt">
            <select v-model="sortBy" @change="onFilterChange" class="sort-select-alt">
              <option value="newest">Mới nhất</option>
              <option value="oldest">Cũ nhất</option>
              <option value="priceAsc">Giá tăng dần</option>
              <option value="priceDesc">Giá giảm dần</option>
              <option value="nameAsc">Tên A → Z</option>
              <option value="nameDesc">Tên Z → A</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Filter Stats -->
      <div class="filter-summary-row" v-if="totalProducts > 0 || activeFiltersCount > 0">
        <div class="result-count">
          Tìm thấy <strong>{{ totalProducts }}</strong> sản phẩm
        </div>
        <div class="active-filters-badges" v-if="activeFiltersCount > 0">
          <span class="active-filter-label">Bộ lọc đang chọn ({{ activeFiltersCount }}):</span>
          <button class="clear-filters-btn" @click="clearFilters">
            <span>Xóa bộ lọc</span>
            <X :size="14" />
          </button>
        </div>
      </div>

      <!-- Main Layout: Sidebar & Content -->
      <div class="main-page-layout">

        <!-- Sidebar filters -->
        <aside class="filters-sidebar" :class="{ 'mobile-show': showMobileFilters }">
          <div class="sidebar-header mobile-only">
            <h3>Bộ lọc tìm kiếm</h3>
            <button class="close-sidebar-btn" @click="showMobileFilters = false" aria-label="Đóng">
              <X :size="20" />
            </button>
          </div>

          <!-- Section: Khoảng giá -->
          <div class="filter-group">
            <h4 class="filter-group-title">Khoảng giá (đ)</h4>
            <div class="price-range-inputs">
              <input
                type="text"
                placeholder="Từ"
                :value="minPriceInput"
                @input="handleMinPriceInput"
                @blur="onFilterChange"
                class="price-input"
              />
              <span class="range-separator">-</span>
              <input
                type="text"
                placeholder="Đến"
                :value="maxPriceInput"
                @input="handleMaxPriceInput"
                @blur="onFilterChange"
                class="price-input"
              />
            </div>
            <button class="apply-price-btn" @click="onFilterChange">Áp dụng giá</button>
          </div>

          <!-- Clear Filters Sidebar Button -->
          <button class="sidebar-clear-btn" v-if="activeFiltersCount > 0" @click="clearFilters">
            Đặt lại bộ lọc
          </button>
        </aside>

        <!-- Product Listing Grid -->
        <main class="products-grid-container">

          <!-- Loading state -->
          <div class="loading-state" v-if="loading">
            <RefreshCw class="loading-spinner" :size="36" />
            <p>Đang tải danh sách sản phẩm...</p>
          </div>

          <!-- Empty state -->
          <div class="empty-state" v-else-if="products.length === 0">
            <SlidersHorizontal :size="48" class="empty-icon" />
            <h3>Không tìm thấy sản phẩm nào</h3>
            <p>Vui lòng đổi bộ lọc hoặc xóa từ khóa tìm kiếm để thử lại.</p>
            <button class="clear-filters-btn-empty" @click="clearFilters">Xóa toàn bộ bộ lọc</button>
          </div>

          <!-- Grid list -->
          <div class="products-grid-alt" v-else>
            <article v-for="product in products" :key="product.id" class="product-card-alt">
              <!-- Top Row: Origin Tag & Wishlist -->
              <div class="card-top-row">
                <span class="origin-tag-alt" v-if="product.originName">{{ product.originName }}</span>
                <span class="origin-tag-alt" v-else>Quốc tế</span>
                <button class="wishlist-btn-alt" aria-label="Yêu thích">
                  <Heart :size="16" />
                </button>
              </div>

              <!-- Image -->
              <div class="product-image-container-alt">
                <img :src="product.url || '/placeholder.png'" class="product-thumbnail-alt" alt="Product Image" />
              </div>

              <!-- Product Information -->
              <div class="product-info-block-alt">
                <h3 class="product-title-alt" :title="product.productName">{{ product.productName }}</h3>

                <!-- Price and Details Button -->
                <div class="product-card-footer-alt">
                  <div class="product-price-val-alt">{{ formatCurrency(product.price) }}</div>
                  <router-link :to="`detail-product/${product.id}`" class="btn-view-detail-alt">
                    <span>Xem chi tiết</span>
                    <ArrowRight :size="14" />
                  </router-link>
                </div>
              </div>
            </article>
          </div>

          <!-- Pagination Block -->
          <div class="pagination-bar-alt" v-if="totalPages > 1 && !loading">
            <button class="pagination-arrow-btn" :disabled="page === 1" @click="onPageChange(page - 1)" aria-label="Trang trước">
              <ChevronLeft :size="16" />
            </button>
            <span class="pagination-page-indicator">
              Trang <strong>{{ page }}</strong> trên <strong>{{ totalPages }}</strong>
            </span>
            <button class="pagination-arrow-btn" :disabled="page >= totalPages" @click="onPageChange(page + 1)" aria-label="Trang sau">
              <ChevronRight :size="16" />
            </button>
          </div>
        </main>
      </div>

    </div>
  </div>
  <Footer/>
</template>

<style scoped>
.products-page-wrapper {
  background-color: #f8f9fc;
  min-height: 100vh;
  padding: 40px 20px 80px 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.container {
  max-width: 1340px;
  margin: 0 auto;
}

/* Breadcrumbs */
.breadcrumb-top-alt {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.88rem;
  color: #6b7280;
  margin-bottom: 24px;
  padding-left: 4px;
}

.breadcrumb-link-alt {
  color: #7c3aed;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.breadcrumb-link-alt:hover {
  color: #6d28d9;
}

.breadcrumb-separator-alt {
  color: #d1d5db;
}

.breadcrumb-current-alt {
  color: #9ca3af;
  font-weight: 500;
}

/* Search, Filter & Sort Row */
.search-filter-row-alt {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-bottom: 32px;
  width: 100%;
}

.search-input-wrapper-alt {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  height: 48px;
}

.search-inner-icon-left {
  position: absolute;
  left: 18px;
  color: #9ca3af;
  pointer-events: none;
}

.search-input-alt {
  width: 100%;
  height: 100%;
  padding: 0 54px 0 48px;
  border: 1px solid #e2e8f0;
  border-radius: 9999px;
  font-size: 0.95rem;
  outline: none;
  background: #ffffff;
  color: #1f2937;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
  transition: all 0.3s;
}

.search-input-alt:focus {
  border-color: #7c3aed;
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.08);
}

.search-action-btn-alt {
  position: absolute;
  right: 6px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #7c3aed;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(124, 58, 237, 0.25);
  transition: all 0.2s;
}

.search-action-btn-alt:hover {
  background: #6d28d9;
  transform: scale(1.05);
}

.filter-actions-group-alt {
  display: flex;
  gap: 12px;
  align-items: center;
}

.white-action-btn-alt {
  height: 48px;
  padding: 0 20px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5563;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
  transition: all 0.2s;
}

.white-action-btn-alt:hover {
  border-color: #7c3aed;
  color: #7c3aed;
}

/* Sort select styling */
.sort-select-alt {
  height: 48px;
  padding: 0 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  color: #4b5563;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
  outline: none;
  transition: all 0.2s;
}

.sort-select-alt:focus, .sort-select-alt:hover {
  border-color: #7c3aed;
  color: #7c3aed;
}

/* Filter Summary and Badges */
.filter-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: #ffffff;
  padding: 16px 24px;
  border-radius: 16px;
  border: 1px solid rgba(124, 58, 237, 0.08);
}

.result-count {
  font-size: 0.95rem;
  color: #4b5563;
}

.result-count strong {
  color: #7c3aed;
  font-size: 1.05rem;
}

.active-filters-badges {
  display: flex;
  align-items: center;
  gap: 12px;
}

.active-filter-label {
  font-size: 0.88rem;
  color: #6b7280;
  font-weight: 600;
}

.clear-filters-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fee2e2;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 0.82rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-filters-btn:hover {
  background: #fee2e2;
  color: #dc2626;
}

/* Main Layout Grid */
.main-page-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
  align-items: start;
}

/* Sidebar Filters */
.filters-sidebar {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(124, 58, 237, 0.08);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-group-title {
  font-size: 0.92rem;
  font-weight: 800;
  color: #1e1b4b;
  margin: 0;
  padding-bottom: 8px;
  border-bottom: 1.5px solid #f1f5f9;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 220px;
  overflow-y: auto;
  padding-right: 4px;
}

/* Scrollbar styling for filters */
.filter-options-list::-webkit-scrollbar {
  width: 5px;
}
.filter-options-list::-webkit-scrollbar-track {
  background: #f1f5f9;
}
.filter-options-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 9999px;
}

.filter-option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.88rem;
  color: #4b5563;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  user-select: none;
}

.filter-option-item input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.custom-radio {
  width: 18px;
  height: 18px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  display: inline-block;
  position: relative;
  transition: all 0.2s;
}

.filter-option-item:hover .custom-radio {
  border-color: #7c3aed;
}

.filter-option-item input:checked ~ .custom-radio {
  border-color: #7c3aed;
  background: #7c3aed;
}

.filter-option-item input:checked ~ .custom-radio::after {
  content: '';
  position: absolute;
  top: 4px;
  left: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ffffff;
}

/* Price Range */
.price-range-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.88rem;
  outline: none;
  font-weight: 600;
  color: #1f2937;
  transition: all 0.2s;
}

.price-input:focus {
  border-color: #7c3aed;
}

.range-separator {
  color: #94a3b8;
}

.apply-price-btn {
  height: 40px;
  background: #f3effc;
  color: #7c3aed;
  border: none;
  border-radius: 10px;
  font-size: 0.88rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.apply-price-btn:hover {
  background: #7c3aed;
  color: #ffffff;
}

.sidebar-clear-btn {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 12px;
  font-weight: 700;
  font-size: 0.88rem;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.sidebar-clear-btn:hover {
  background: #cbd5e1;
  color: #1e293b;
}

/* Products Grid Container */
.products-grid-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.products-grid-alt {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

/* Loading state */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #6b7280;
  gap: 16px;
}

.loading-spinner {
  color: #7c3aed;
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  background: #ffffff;
  border-radius: 24px;
  border: 1px dashed #cbd5e1;
  text-align: center;
  color: #6b7280;
}

.empty-icon {
  color: #94a3b8;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1e1b4b;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 0.92rem;
  margin: 0 0 20px 0;
}

.clear-filters-btn-empty {
  background: #7c3aed;
  color: #ffffff;
  border: none;
  padding: 10px 24px;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(124, 58, 237, 0.2);
  transition: all 0.2s;
}

.clear-filters-btn-empty:hover {
  background: #6d28d9;
  transform: translateY(-1px);
}

.product-card-alt {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(124, 58, 237, 0.08);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.01);
  padding: 24px;
  display: flex;
  flex-direction: column;
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;
}

.card-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  z-index: 2;
}

.origin-tag-alt {
  padding: 4px 10px;
  background: #f3effc;
  color: #7c3aed;
  border-radius: 9999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.wishlist-btn-alt {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #f1f5f9;
  background: #ffffff;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.wishlist-btn-alt:hover {
  background: #fef2f2;
  border-color: #fee2e2;
  color: #ef4444;
}

.product-image-container-alt {
  height: 180px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 16px;
  border-radius: 16px;
  background: #ffffff;
}

.product-thumbnail-alt {
  max-height: 90%;
  max-width: 90%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.product-info-block-alt {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.product-title-alt {
  font-size: 1.05rem;
  font-weight: 700;
  color: #1e1b4b;
  margin-bottom: 8px;
  line-height: 1.4;
  height: 2.8em;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-card-footer-alt {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: auto;
}

.product-price-val-alt {
  font-size: 1.25rem;
  font-weight: 800;
  color: #7c3aed;
}

.btn-view-detail-alt {
  width: 100%;
  height: 42px;
  background: transparent;
  color: #7c3aed;
  border: 1.5px solid rgba(124, 58, 237, 0.3);
  border-radius: 9999px;
  font-size: 0.88rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  text-decoration: none;
}

/* Card Hover States */
.product-card-alt:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 28px rgba(124, 58, 237, 0.08);
  border-color: rgba(124, 58, 237, 0.2);
}

.product-card-alt:hover .product-thumbnail-alt {
  transform: scale(1.05);
}

.product-card-alt:hover .btn-view-detail-alt {
  background: #7c3aed;
  color: #ffffff;
  border-color: #7c3aed;
  box-shadow: 0 8px 16px rgba(124, 58, 237, 0.2);
}

/* Pagination */
.pagination-bar-alt {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
}

.pagination-arrow-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #4b5563;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-arrow-btn:hover:not(:disabled) {
  border-color: #7c3aed;
  color: #7c3aed;
}

.pagination-arrow-btn:disabled {
  background: #f1f5f9;
  color: #d1d5db;
  cursor: not-allowed;
}

.pagination-page-indicator {
  font-size: 0.95rem;
  color: #4b5563;
}

.pagination-page-indicator strong {
  color: #1e1b4b;
}

/* Helper Utilities */
.mobile-only {
  display: none !important;
}

/* Responsiveness */
@media (max-width: 1200px) {
  .products-grid-alt {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 968px) {
  .main-page-layout {
    grid-template-columns: 1fr;
  }

  .filters-sidebar {
    position: fixed;
    top: 0;
    left: -320px;
    width: 300px;
    height: 100vh;
    z-index: 999;
    overflow-y: auto;
    border-radius: 0;
    transition: left 0.3s ease;
  }

  .filters-sidebar.mobile-show {
    left: 0;
  }

  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1.5px solid #f1f5f9;
    padding-bottom: 12px;
    margin-bottom: 12px;
  }

  .sidebar-header h3 {
    margin: 0;
    font-size: 1.1rem;
    color: #1e1b4b;
    font-weight: 800;
  }

  .close-sidebar-btn {
    background: transparent;
    border: none;
    cursor: pointer;
    color: #475569;
  }

  .mobile-only {
    display: flex !important;
  }

  .search-filter-row-alt {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions-group-alt {
    justify-content: space-between;
  }

  .sort-select-wrapper-alt, .filter-btn-alt {
    flex: 1;
  }

  .sort-select-alt {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .products-grid-alt {
    grid-template-columns: 1fr;
  }
}
</style>