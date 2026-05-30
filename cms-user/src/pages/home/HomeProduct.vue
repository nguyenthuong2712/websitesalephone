<script setup lang="ts">
import {ref, onMounted} from "vue";
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import {productService} from "@/service/ProductService";
import {Search} from "@/models/Search.ts";
import {formatCurrency} from "@/utils/Constant.ts";
import {CartRequest} from "@/models/CartRequest.ts";
import {cartService} from "@/service/CartService.ts";
import {toast} from "vue3-toastify";
import {
  Search as SearchIcon,
  SlidersHorizontal,
  ChevronDown,
  Heart,
  ArrowRight,
  ChevronLeft,
  ChevronRight
} from "@lucide/vue";

const products = ref<any[]>([]);
const searchText = ref("");
const page = ref(1);
const size = ref(12);
const totalPages = ref<number>(1);
const quantity = ref(1);

const loadProducts = async () => {
  const search = new Search(page.value, size.value, searchText.value, '');
  try {
    const res = await productService.search(search);
    products.value = res.data.data.content || [];
    totalPages.value = Number(res.data.data.totalPages ?? 1);

    console.log("📄 Fetched page:", page.value, "totalPages:", totalPages.value);
  } catch (error) {
    console.error("Lỗi khi load sản phẩm:", error);
  }
};

const onSearch = () => {
  page.value = 1;
  loadProducts();
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
  loadProducts();
};

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

      <!-- Search, Filter & Sort Row -->
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
          <button class="white-action-btn-alt filter-btn-alt">
            <span>Lọc</span>
            <SlidersHorizontal :size="16" />
          </button>
          
          <div class="sort-select-wrapper-alt">
            <button class="white-action-btn-alt sort-btn-alt">
              <span>Sắp xếp: Mới nhất</span>
              <ChevronDown :size="16" />
            </button>
          </div>
        </div>
      </div>

      <!-- Products Grid -->
      <div class="products-grid-alt">
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
            
            <!-- Technical Specs list -->
            <div class="product-specs-list-alt" v-if="product.specs && product.specs.length">
              <span class="spec-tag-alt" v-for="spec in product.specs" :key="spec">{{ spec }}</span>
            </div>
            
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
      <div class="pagination-bar-alt" v-if="totalPages > 1">
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

/* Products Grid */
.products-grid-alt {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
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
  height: 2.8em; /* Force two lines height maximum */
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-specs-list-alt {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.spec-tag-alt {
  padding: 4px 8px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 0.72rem;
  color: #4b5563;
  font-weight: 600;
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

/* Responsiveness */
@media (max-width: 1200px) {
  .products-grid-alt {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 968px) {
  .products-grid-alt {
    grid-template-columns: repeat(2, 1fr);
  }
  .search-filter-row-alt {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-actions-group-alt {
    justify-content: space-between;
  }
}

@media (max-width: 640px) {
  .products-grid-alt {
    grid-template-columns: 1fr;
  }
  .white-action-btn-alt {
    flex: 1;
    justify-content: center;
  }
  .sort-select-wrapper-alt {
    flex: 1;
  }
  .sort-btn-alt {
    width: 100%;
    justify-content: center;
  }
}
</style>