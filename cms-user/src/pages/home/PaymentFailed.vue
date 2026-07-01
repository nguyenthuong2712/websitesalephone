<script setup lang="ts">
import { useRoute } from 'vue-router'
import HomeLayout from '../../layout/Header.vue'
import Footer from '../../layout/Footer.vue'

const route = useRoute()

const orderId = route.query.orderId as string
const status = route.query.status as string
const orderCode = route.query.orderCode as string
</script>

<template>
  <HomeLayout />
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-md-8 col-lg-6">
        <div class="card border-0 shadow-lg text-center p-4">
          <div class="card-body">
            <div class="mb-4 text-danger">
              <span class="display-1">❌</span>
            </div>
            <h1 class="card-title text-danger mb-3" style="font-weight: 700;">Thanh Toán Thất Bại</h1>
            <p class="text-muted mb-4">Giao dịch thanh toán trực tuyến của bạn đã không thể hoàn tất hoặc đã bị hủy.</p>

            <div class="bg-light rounded p-4 mb-4 text-start">
              <div class="d-flex justify-content-between mb-3 border-bottom pb-2">
                <span class="text-secondary">Trạng thái giao dịch:</span>
                <strong class="text-danger text-end">{{ status === 'CANCELLED' ? 'Khách hàng hủy thanh toán' : 'Giao dịch thất bại' }}</strong>
              </div>
              <div class="d-flex justify-content-between" v-if="orderCode">
                <span class="text-secondary">Mã số giao dịch:</span>
                <strong class="text-dark">{{ orderCode }}</strong>
              </div>
              <div v-if="orderId" class="d-flex justify-content-between mt-3 border-top pt-2">
                <span class="text-secondary">Mã đơn hàng:</span>
                <strong class="text-dark">{{ orderId }}</strong>
              </div>
            </div>

            <div class="d-grid gap-2 d-sm-flex justify-content-sm-center">
              <router-link to="/customer/order-by-user" class="btn btn-danger px-4 py-2 me-sm-2">Xem đơn hàng của tôi</router-link>
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
