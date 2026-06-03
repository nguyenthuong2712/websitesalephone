<script setup lang="ts">
import { useRoute } from 'vue-router'
import HomeLayout from '../../layout/Header.vue'
import Footer from '../../layout/Footer.vue'

const route = useRoute()

const transactionNo = route.query.vnp_TransactionNo as string
const amount = route.query.vnp_Amount ? (Number(route.query.vnp_Amount) / 100) : 0
const orderInfo = route.query.vnp_OrderInfo as string
const orderId = route.query.orderId as string
</script>

<template>
  <HomeLayout />
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-md-8 col-lg-6">
        <div class="card border-0 shadow-lg text-center p-4">
          <div class="card-body">
            <div class="mb-4 text-success">
              <span class="display-1">🎉</span>
            </div>
            <h1 class="card-title text-success mb-3" style="font-weight: 700;">Thanh Toán Thành Công!</h1>
            <p class="text-muted mb-4">Cảm ơn bạn đã mua sắm tại Website Sale Phone. Đơn hàng của bạn đã được ghi nhận và đang chờ xử lý.</p>

            <div class="bg-light rounded p-4 mb-4 text-start">
              <div class="d-flex justify-content-between mb-3 border-bottom pb-2">
                <span class="text-secondary">Mã giao dịch VNPAY:</span>
                <strong class="text-dark">{{ transactionNo }}</strong>
              </div>
              <div class="d-flex justify-content-between mb-3 border-bottom pb-2">
                <span class="text-secondary">Mã đơn hàng:</span>
                <strong class="text-dark">{{ orderId }}</strong>
              </div>
              <div class="d-flex justify-content-between mb-3 border-bottom pb-2">
                <span class="text-secondary">Nội dung thanh toán:</span>
                <strong class="text-dark">{{ orderInfo }}</strong>
              </div>
              <div class="d-flex justify-content-between">
                <span class="text-secondary font-weight-bold">Số tiền thanh toán:</span>
                <strong class="text-success">{{ amount.toLocaleString('vi-VN') }}đ</strong>
              </div>
            </div>

            <div class="d-grid gap-2 d-sm-flex justify-content-sm-center">
              <router-link to="/customer/order-by-user" class="btn btn-success px-4 py-2 me-sm-2">Xem lịch sử đơn hàng</router-link>
              <router-link to="/customer/product-home" class="btn btn-outline-secondary px-4 py-2">Tiếp tục mua sắm</router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<style scoped>
.card {
  border-radius: 16px;
  background: #ffffff;
}
</style>
