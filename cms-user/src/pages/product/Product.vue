<script setup lang="ts">
import {ref, onMounted, computed, watch} from 'vue';
import {productService} from '@/service/ProductService.ts';
import {Search} from '@/models/Search.ts';
import {toast} from "vue3-toastify";
import {formatCurrency} from "@/utils/Constant.ts";

const products = ref<any[]>([]);
const searchText = ref("");
const page = ref<number>(1);
const size = ref<number>(10);
const totalPages = ref<number>(1);

const fetchProducts = async () => {
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
  fetchProducts();
};

watch(searchText, () => {
  page.value = 1;
  fetchProducts();
});

const deleted = async (id: string) => {
  const res = await productService.deleteProduct(id);
  if (res.data.code === 0) {
    toast.success("Xóa sản phẩm thành công")
  } else {
    toast.error("Xóa sản phẩm không thành công")
  }
  await fetchProducts();
}

onMounted(async () => {
  await fetchProducts();
  console.log(page.value)
});

</script>

<template>
  <section class="content-card" id="products">
    <div class="card-header">
      <h2 class="card-title">📱 Quản Lý Sản Phẩm</h2>
      <div class="card-actions">
        <input type="text" v-model="searchText" placeholder="Tìm kiếm sản phẩm..." class="input-search"/>
        <router-link style="width: 219px;" class="btn btn-primary" to="create-product">+ Thêm mới</router-link>
      </div>
    </div>

    <div class="table-wrapper">
      <table>
        <thead>
        <tr>
          <th>Sản Phẩm</th>
          <th>Xuất xứ</th>
          <th>Giá</th>
          <th>Tồn Kho</th>
          <th>Đã Bán</th>
          <th>Trạng Thái</th>
          <th>Thao Tác</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="product in products" :key="product.id">
          <td>
            <div class="product-name">{{ product.productName }}</div>
          </td>
          <td>{{ product.originName }}</td>
          <td><span class="price">{{ formatCurrency(product.price) }}</span></td>
          <td>{{ product.quantity }}</td>
          <td>{{ product.quantityUnitSold }}</td>
          <td>
            <span :class="['status-badge', product.status === 'ACTIVE' ? 'status-active' : 'status-inactive']">
              {{ product.status === 'ACTIVE' ? 'Đang Bán' : 'Ngừng Bán' }}
            </span>
          </td>
          <td>
            <div class="action-buttons">
              <button class="action-btn btn-view">👁️</button>
              <router-link :to="`/admin/product-detail/${product.id}`" class="action-btn btn-edit">✏️</router-link>
              <button class="action-btn btn-delete" @click="deleted(product.id)">🗑️</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
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
  </section>
</template>


<style scoped>
/* =========================
   RESET & BASE
========================= */
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #1a1a2e;
  line-height: 1.6;
}

/* =========================
   FORM — INPUT
========================= */
.input-search {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  font-size: 14px;
  background: #fff;
  transition: .25s;
  outline: none;
}

.input-search::placeholder {
  color: #999;
  font-style: italic;
}

.input-search:focus {
  border-color: #4b8bff;
  box-shadow: 0 0 0 3px rgba(75, 139, 255, 0.2);
}

/* =========================
   NAVIGATION
========================= */
.nav-menu li {
  margin-bottom: 5px;
}

.nav-menu a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 1.05em;
  font-weight: 600;
  border-left: 4px solid transparent;
  transition: .3s;
}

.nav-menu a:hover,
.nav-menu a.active {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border-left-color: #fee140;
}

/* =========================
   CARD — CONTAINER
========================= */
.content-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
  overflow: hidden;
}

.card-header {
  padding: 25px 30px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 1.6em;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-actions {
  display: flex;
  gap: 10px;
}

/* =========================
   BUTTONS
========================= */
.btn {
  padding: 10px 25px;
  border-radius: 25px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  font-size: .95em;
  transition: .3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, .4);
}

/* =========================
   TABLE
========================= */
.table-wrapper {
  width: 100%;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

thead {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

thead th {
  padding: 20px 25px;
  border-bottom: 2px solid #e0e0e0;
  font-size: 1em;
  font-weight: 700;
  text-align: center;
}

tbody td {
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
}

tbody tr {
  transition: .3s;
}

tbody tr:hover {
  background: #f9f9f9;
  transform: scale(1.01);
}

/* Border giữa các cột */
td, th {
  border-right: 1px solid #e5e7eb;
}

td:last-child,
th:last-child {
  border-right: none;
}

/* =========================
   PRODUCT CELL
========================= */
.product-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-image {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8em;
}

.product-name {
  font-weight: 700;
  margin-bottom: 3px;
}

/* =========================
   STATUS BADGE
========================= */
.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: .85em;
  font-weight: 700;
}

/* =========================
   ACTION BUTTONS
========================= */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.action-btn {
  width: 35px;
  height: 35px;
  border-radius: 10px;
  border: none;
  font-size: 1.1em;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: .3s;
}

.action-btn:hover {
  transform: scale(1.15);
}

.btn-view {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.btn-edit {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}

.btn-delete {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: #fff;
}

/* =========================
   PRICE
========================= */
.price {
  font-weight: 700;
  color: #667eea;
  font-size: 1.05em;
}

/* =========================
   PAGINATION
========================= */
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
  font-weight: 600;
  cursor: pointer;
  transition: .3s;
}

.page-btn:hover,
.page-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  transform: scale(1.1);
}

.page-btn:disabled {
  background: #ddd !important;
  color: #888 !important;
  cursor: not-allowed !important;
  transform: none !important;
}

/* =========================
   RESPONSIVE
========================= */
@media (max-width: 968px) {
  .table-wrapper {
    overflow-x: auto;
  }

  table {
    min-width: 800px;
  }
}

@media (max-width: 480px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style>