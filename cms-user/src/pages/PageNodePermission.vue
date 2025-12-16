<template>
  <div class="app">
    <div class="error-box">
      <div class="error-code">403</div>
      <div class="error-title">Access Denied</div>
      <p class="error-desc">You do not have permission to view this page.</p>
      <button class="btn btn-primary" @click="goHome">Go to Homepage</button>
    </div>
  </div>
</template>

<script>
import {authService} from "@/service/AuthService.js";
import {useRouter} from 'vue-router';

export default {
  setup() {
    const router = useRouter();

    const goHome = () => {
      const isAuth = authService.isAuthenticated();
      const role = authService.getRole();

      if (!isAuth) {
        return router.push('/');
      }

      if (role === 'CUSTOMER') {
        return router.push('/customer/home');
      }

      if (role === 'ADMIN') {
        return router.push('/admin/dashboard');
      }

      if (role === 'STAFF') {
        return router.push('/admin/product');
      }

      return router.push('/');
    };

    return { goHome };
  }
}
</script>
<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  padding: 20px;
}

.error-box {
  text-align: center;
  background: #ffffff;
  padding: 40px 30px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.error-code {
  font-size: 80px;
  font-weight: 700;
  color: #dc3545;
  margin-bottom: 10px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.error-desc {
  font-size: 16px;
  color: #6c757d;
  margin-bottom: 25px;
}

.btn-primary {
  padding: 10px 20px;
  font-size: 15px;
  border-radius: 8px;
}

</style>
