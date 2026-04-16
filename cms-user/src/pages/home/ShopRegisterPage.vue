<script setup lang="ts">
import HomeLayout from "@/layout/Header.vue";
import Footer from "@/layout/Footer.vue";
import { onMounted, ref } from "vue";
import { useUserStore } from "@/userStore";
import { shopService } from "@/service/ShopService";
import { toast } from "vue3-toastify";
import { authService } from "@/service/AuthService";

const userStore = useUserStore();
const shopName = ref("");
const description = ref("");
const paymentMethod = ref("BANK_TRANSFER");
const qrCode = ref("");
const paymentNote = ref("");

const avatarShop = ref<File | null>(null);
const bannerImage = ref<File | null>(null);
const cccdImage = ref<File | null>(null);

onMounted(async () => {
  if (!userStore.user) {
    await userStore.getUserByLoginId();
  }
});

const onFileChange = (event: Event, target: "avatar" | "banner" | "cccd") => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0] || null;
  if (target === "avatar") avatarShop.value = file;
  if (target === "banner") bannerImage.value = file;
  if (target === "cccd") cccdImage.value = file;
};

const submitShopRegister = async () => {
  if (!shopName.value.trim()) {
    toast.error("Vui lòng nhập tên shop");
    return;
  }

  try {
    const username = userStore.user?.loginId || userStore.user?.email || "";
    const res = await shopService.registerShop(
      {
        username,
        shopName: shopName.value,
        description: description.value,
        paymentMethods: [
          {
            method: paymentMethod.value,
            qrCode: qrCode.value,
            note: paymentNote.value,
          },
        ],
      },
      avatarShop.value,
      bannerImage.value,
      cccdImage.value
    );

    if (res.data.code === 0) {
      authService.saveRole("PARTNER");
      toast.success("Đăng ký shop thành công! Tài khoản đã chuyển sang PARTNER.");
    } else {
      toast.error(res.data.message || "Đăng ký shop thất bại");
    }
  } catch (err: any) {
    toast.error(err?.response?.data?.message || "Đăng ký shop thất bại");
  }
};
</script>

<template>
  <HomeLayout />
  <section class="shop-register-page">
    <div class="form-card">
      <h2>Đăng ký shop</h2>
      <p class="sub">Điền thông tin để mở shop bán hàng.</p>

      <div class="grid">
        <label>
          Tên Shop
          <input v-model="shopName" type="text" placeholder="Nhập tên shop" />
        </label>

        <label>
          Description
          <textarea v-model="description" rows="3" placeholder="Mô tả shop"></textarea>
        </label>

        <label>
          Avatar Shop
          <input type="file" accept="image/*" @change="onFileChange($event, 'avatar')" />
        </label>

        <label>
          Banner Image
          <input type="file" accept="image/*" @change="onFileChange($event, 'banner')" />
        </label>

        <label>
          Upload CCCD
          <input type="file" accept="image/*" @change="onFileChange($event, 'cccd')" />
        </label>

        <label>
          Phương thức thanh toán
          <select v-model="paymentMethod">
            <option value="BANK_TRANSFER">Chuyển khoản</option>
            <option value="CASH">Tiền mặt</option>
            <option value="QR">QR</option>
          </select>
        </label>

        <label>
          Mã QR / nội dung QR
          <input v-model="qrCode" type="text" placeholder="Nội dung QR (nếu có)" />
        </label>

        <label>
          Ghi chú thanh toán
          <input v-model="paymentNote" type="text" placeholder="Ví dụ: Vietcombank - Nguyen Van A" />
        </label>
      </div>

      <button class="btn-submit" @click="submitShopRegister">Gửi đăng ký shop</button>
    </div>
  </section>
  <Footer />
</template>

<style scoped>
.shop-register-page { padding: 24px; background: #f7f9ff; min-height: 70vh; }
.form-card { max-width: 900px; margin: 0 auto; background: #fff; border-radius: 14px; padding: 20px; box-shadow: 0 8px 20px rgba(0,0,0,.08); }
.sub { color: #666; margin-bottom: 16px; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
label { display: flex; flex-direction: column; gap: 6px; font-weight: 600; color: #283044; }
input, textarea, select { border: 1px solid #d6dbe8; border-radius: 8px; padding: 10px; font-size: 14px; }
.btn-submit { margin-top: 16px; background: #4863ff; color: #fff; border: none; padding: 12px 16px; border-radius: 8px; cursor: pointer; }
@media (max-width: 768px) { .grid { grid-template-columns: 1fr; } }
</style>
