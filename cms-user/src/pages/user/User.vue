<script setup lang="ts">
import {ref, onMounted, computed} from "vue";
import {userService} from "@/service/UserService";
import {UserSearchForm} from "@/models/UserSearchForm";
import {useRoute} from "vue-router";
import {toast} from "vue3-toastify";
import {formatCurrency} from "@/utils/Constant.ts";

const route = useRoute();
const userId = route.params.id as string;

const users = ref<any[]>([]);
const page = ref<number>(1);
const size = ref<number>(10);
const totalPages = ref<number>(0);

const form = ref(new UserSearchForm(page, size, "", "", ""));

const searchUsers = async () => {
  try {
    const payload = form.value.toPayload();
    const res = await userService.search(payload);

    // backend trả về Page<UserDto>
    const pageData = res.data.data;

    users.value = pageData.content;
    totalPages.value = pageData.totalPages;

  } catch (e) {
    console.error("Search error:", e);
  }
};

// đổi trang
const changePage = async (page: number) => {
  if (page < 1 || page > totalPages.value) return;
  form.value.page = page;
  await searchUsers();
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
  searchUsers();
};

const deleted = async (id: string) => {
  const res = await userService.deleteUser(id);
  if (res.data.code === 0) {
    toast.success("Xóa tài khoản thành công")
  } else {
    toast.error("Xóa tài khoản không thành công")
  }
  searchUsers()
}

// load lần đầu
onMounted(() => {
  searchUsers();
});
</script>


<template>
  <section class="content-card" id="users">
    <div class="card-header">
      <h2 class="card-title">👥 Quản Lý Người Dùng</h2>
      <div class="card-actions">
        <router-link class="btn btn-primary" to="create-user">+ Thêm User</router-link>
      </div>
    </div>
    <div class="table-wrapper">
      <table>
        <thead>
        <tr>
          <th>Người Dùng</th>
          <th>Số Điện Thoại</th>
          <th>Đơn Hàng</th>
          <th>Tổng Chi</th>
          <th>Ngày Đăng Ký</th>
          <th>Trạng Thái</th>
          <th>Thao Tác</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>
            <div class="user-cell">
              <div class="user-avatar">👨</div>
              <div class="user-info">
                <div class="user-name">{{ u.fullName }}</div>
                <div class="user-email">{{ u.email }}</div>
              </div>
            </div>
          </td>
          <td>{{ u.telNo }}</td>
          <td>{{ u.orderCount }}</td>
          <td><span class="price">{{ formatCurrency(u.totalSpent) }}</span></td>
          <td>{{ u.created }}</td>
          <td>
      <span :class="['status-badge', u.deleted ? 'status-active' : 'status-block']">
        {{ u.deleted ? 'Tạm Khóa' : 'Hoạt Động' }}
      </span>
          </td>

          <td>
            <div class="action-buttons">
              <router-link :to="`/admin/user-detail/${u.id}`" class="action-btn btn-edit">✏️</router-link>
              <button class="action-btn btn-delete" @click="deleted(u.id)">🗑️</button>
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
/* Reset */
body { box-sizing: border-box; }
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body { height: 100%; }

/* Base */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #1a1a2e;
  line-height: 1.6;
}

.admin-wrapper {
  display: flex;
  min-height: 100%;
}

/* Sidebar Nav */
.nav-menu li { margin-bottom: 5px; }
.nav-menu a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  font-weight: 600;
  font-size: 1.05em;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}
.nav-menu a:hover,
.nav-menu a.active {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-left-color: #fee140;
}

/* Card */
.content-card {
  background: white;
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
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-actions { display: flex; gap: 10px; }

/* Buttons */
.btn {
  padding: 10px 25px;
  border-radius: 25px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95em;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
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
  font-weight: 600;
  padding: 10px;
  border-bottom: 2px solid #e2e8f0;
  text-align: center;
}

tbody td {
  padding: 12px 10px;
  border-bottom: 1px solid #e2e8f0;
}

td, th {
  border-right: 1px solid #e5e7eb;
  text-align: center;
}

td:last-child, th:last-child {
  border-right: none;
}

th {
  padding: 20px 25px;
  font-weight: 700;
  color: #1a1a2e;
  font-size: 1em;
  border-bottom: 2px solid #e0e0e0;
}

td {
  padding: 20px 25px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 0.98em;
}

tbody tr {
  transition: all 0.3s ease;
}

tbody tr:hover {
  background: #f9fafb;
  transform: scale(1.01);
}

/* Status Badge */
.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 700;
  display: inline-block;
}

/* User cell */
.user-cell {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-avatar {
  width: 45px;
  height: 45px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
  color: white;
  flex-shrink: 0;
}

.user-info { flex: 1; }

.user-name {
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 3px;
}

.user-email {
  font-size: 0.9em;
  color: #666;
}

/* Action buttons */
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
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.1em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover { transform: scale(1.15); }

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

.page-btn:disabled,
.page-btn:disabled:hover {
  background: #ddd !important;
  color: #888 !important;
  cursor: not-allowed !important;
  transform: none !important;
}

/* Responsive */
@media (max-width: 1200px) {
  .sidebar { width: 240px; }
}

@media (max-width: 968px) {
  .table-wrapper { overflow-x: scroll; }
  table { min-width: 800px; }
}

@media (max-width: 480px) {
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
}
</style>
