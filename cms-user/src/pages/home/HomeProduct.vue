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
  <div class="container">
    <header class="page-header">
      <h1>📱 Danh Sách Sản Phẩm</h1>
      <div class="breadcrumb"><a href="#home">Trang chủ</a> / Sản phẩm</div>
      <div class="search-section">
        <div class="search-box">
          <input v-model="searchText" type="text" class="search-input" placeholder="Tìm kiếm sản phẩm...">
          <span class="search-icon" @click="onSearch">🔍</span>
        </div>
      </div>
    </header>

    <div class="products-grid">
      <article v-for="product in products" :key="product.id" class="product-card">
        <div class="product-image"><img :src="product.url" alt="" sizes="" srcset=""></div>
        <div class="product-info">
          <span class="product-category">{{ product.originName }}</span>
          <h3 class="product-name">{{ product.productName }}</h3>

          <div class="product-specs">
            <span class="spec-tag" v-for="spec in product.specs" :key="spec">{{ spec }}</span>
          </div>
          <div class="product-footer">
            <div class="product-price">{{ formatCurrency(product.price | currency) }}</div>
            <router-link :to="`detail-product/${product.id}`"
                         class="btn-add-cart">Xem chi tiết
            </router-link>
          </div>
        </div>
      </article>
    </div>
    <div class="pagination">
      <button class="page-btn" :disabled="page === 1" @click="onPageChange(page - 1)">«</button>
      <button
          class="page-btn active"
      >
        {{ page }} / {{ totalPages }}
      </button>
      <button class="page-btn" :disabled="page >= totalPages" @click="onPageChange(page + 1)">»</button>
    </div>
  </div>
  <Footer/>
</template>

<style scoped>
img {
  width: 250px;
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

/* Header */
.page-header {
  background: white;
  padding: 40px;
  border-radius: 20px;
  margin-bottom: 30px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
}

.page-header h1 {
  font-size: 2.8em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.breadcrumb {
  color: #666;
  font-size: 1.1em;
  margin-bottom: 30px;
}

.breadcrumb a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

/* Search & Filter Section */
.search-section {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 16px 50px 16px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 15px;
  font-size: 1.1em;
  font-weight: 600;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.5em;
  color: #667eea;
}

.results-count strong {
  color: #667eea;
  font-size: 1.3em;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.product-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  transition: all 0.4s ease;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.3);
}

.product-image {
  height: 250px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  position: relative;
}

.product-info {
  padding: 25px;
}

.product-category {
  display: inline-block;
  padding: 5px 12px;
  background: #f0f0f0;
  border-radius: 8px;
  font-size: 0.85em;
  color: #666;
  font-weight: 600;
  margin-bottom: 12px;
}

.product-name {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 10px;
  line-height: 1.4;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}

.spec-tag {
  padding: 4px 10px;
  background: #f9f9f9;
  border-radius: 6px;
  font-size: 0.8em;
  color: #666;
}

.product-footer {
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 2px solid #f0f0f0;
}
a {
  text-decoration: none;
  text-align: center;
}
.product-price {
  font-size: 1.6em;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.btn-add-cart {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 85px;
}

.btn-add-cart:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 30px;
}

.page-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #f0f0f0;
  color: #1a1a2e;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover,
.page-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
}

.page-btn:disabled {
  background: #ddd !important;
  color: #888 !important;
  cursor: not-allowed !important;
  transform: none !important;
}

.page-btn:disabled:hover {
  background: #ddd !important;
  transform: none !important;
}

/* Responsive */
@media (max-width: 968px) {
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 20px;
  }

  .search-section {
    flex-direction: column;
  }

  .search-box {
    width: 100%;
  }

}

@media (max-width: 640px) {
  body {
    padding: 20px 10px;
  }

  .page-header h1 {
    font-size: 2em;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    gap: 5px;
  }

  .page-btn {
    min-width: 40px;
    height: 40px;
    font-size: 0.9em;
  }
}
</style>