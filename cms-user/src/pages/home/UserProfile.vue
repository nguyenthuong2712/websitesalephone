<script setup lang="ts">
import {onMounted, ref} from "vue";
import HomeLayout from "@/layout/Header.vue";
import Footer from "@/layout/Footer.vue";
import {useUserStore} from "@/userStore.ts";
import {toast} from "vue3-toastify";
const isEditing = ref(false);

const toggleEdit = () => {
  isEditing.value = !isEditing.value;
};

const saveProfile = async () => {
  const profile = {
    fullName: userStore.user.fullName,
    telNo: userStore.user.telNo,
    email: userStore.user.email,
    address: userStore.user.address,
    gender: userStore.user.gender
  };

  try {
    await useUserStore().updateUser(profile);

    isEditing.value = false;

    toast.success("Cập nhật thông tin thành công!");
  } catch (err) {
    toast.error("Cập nhật thất bại!");
  }
};


const userStore = useUserStore();
const user = userStore.user;

onMounted(async () => {
  await userStore.getUserByLoginId();
  console.log(userStore.user);
});

</script>


<template>
  <HomeLayout/>
  <br>
  <article class="profile-card" aria-label="Thông tin người dùng">
    <aside class="profile-left">
      <div class="avatar" id="avatar">BD</div>
      <div class="name" id="displayName">{{userStore.user?.fullName}}</div>
      <div class="role">{{userStore.user.role === 'ADMIN' ? 'Quản Lý' : userStore.user.role === 'STAFF' ? 'Nhân Viên' : 'Khách hàng'}}</div>
      <div class="contact-quick" aria-hidden="false">
        <div class="chip" id="quickEmail">{{userStore.user?.email}}</div>
        <div class="chip" id="quickPhone">{{userStore.user?.telNo}}</div>
      </div>

      <div class="note" style="text-align:center; margin-top:8px;">
        Thông tin an toàn — giữ cho tài khoản luôn bảo mật.
      </div>
    </aside>

    <section class="profile-right">
      <div class="header-row">
        <div>
          <div class="title">Hồ sơ cá nhân</div>
          <div class="subtitle">Xem và chỉnh sửa thông tin cơ bản</div>
        </div>

        <div>
          <button class="btn ghost" @click="toggleEdit">
            {{ isEditing ? "Đóng" : "Chỉnh sửa" }}
          </button>
        </div>
      </div>

      <div class="field-list" v-if="!isEditing">
        <div class="field">
          <div class="label">Họ và tên</div>
          <div class="value" id="v-fullname">{{userStore.user?.fullName}}</div>
        </div>

        <div class="field">
          <div class="label">Email</div>
          <div class="value" id="v-email">{{userStore.user?.email}}</div>
        </div>

        <div class="field">
          <div class="label">Địa chỉ</div>
          <div class="value" id="v-address">{{userStore.user?.address}}</div>
        </div>

        <div class="field">
          <div class="label">Số điện thoại</div>
          <div class="value" id="v-phone">{{userStore.user?.telNo}}</div>
        </div>

        <div class="field" role="group" aria-label="Giới tính">
          <div class="label">Giới tính</div>
          <div class="value" id="v-gender">{{userStore.user?.gender === 'male' ? 'Nam' : 'Nữ'}}</div>
        </div>
      </div>

      <!-- Edit form -->
      <form class="edit-form" v-if="isEditing">
        <div class="form-row">
          <label class="label" for="fullnameInput">Họ và tên</label>
          <input v-model="userStore.user.fullName" type="text" value="Bố Đức" />
        </div>

        <div class="form-row">
          <label class="label" for="emailInput">Email</label>
          <input v-model="userStore.user.email" type="email" value="boduc@example.com" />
        </div>

        <div class="form-row">
          <label class="label" for="addressInput">Địa chỉ</label>
          <input v-model="userStore.user.address" type="text" value="123 Đường Lê Lợi, Quận 1, TP.HCM" />
        </div>

        <div class="form-row">
          <label class="label" for="phoneInput">SĐT</label>
          <input v-model="userStore.user.telNo" type="tel" value="+84 912 345 678" />
        </div>

        <div class="form-row">
          <label class="label" for="genderSelect">Giới tính</label>
          <select v-model="userStore.user.gender" aria-label="Chọn giới tính">
            <option value="male">Nam</option>
            <option value="female">Nữ</option>
            <option value="other">Khác</option>
          </select>
        </div>

        <div class="actions">
          <button type="button" class="btn ghost" id="saveBtn" @click="saveProfile">Lưu</button>
        </div>
      </form>

    </section>
  </article>
  <br>
  <Footer/>
</template>

<style scoped>
:root{
  --bg:#f6f8fb;
  --card:#ffffff;
  --muted: #475569;      /* trước hơi nhạt */
  --accent: #1d4ed8;
  --success:#16a34a;
  --radius:12px;
  --shadow: 0 6px 18px rgba(15,23,42,0.08);
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
}

body{
  margin:0;
  padding:32px;
  background:linear-gradient(180deg,var(--bg),#eef2f7);
  display:flex;
  align-items:flex-start;
  justify-content:center;
  min-height:100vh;
  color:#0f172a;
}

.profile-card {
  height: 450px;
  width: 100%;
  max-width: 920px;
  background: var(--card);
  border-radius: var(--radius);
  overflow: hidden;
  display: flex;
  gap: 24px;

  border: 1px solid rgba(15,23,42,0.12);
  box-shadow:
      0 0 0 3px rgba(255,255,255,0.4) inset,
      0 6px 18px rgba(15,23,42,0.06);
  backdrop-filter: blur(6px);               /* tạo chiều sâu kiểu glass */
  margin-left: 500px;
}

/* left: avatar & quick info */
.profile-left{
  padding:10px;
  flex:0 0 320px;
  background:linear-gradient(180deg,#fbfdff,#f6fbff);
  display:flex;
  flex-direction:column;
  align-items:center;
  gap:18px;
}

.avatar{
  width:120px;
  height:120px;
  border-radius:50%;
  overflow:hidden;
  box-shadow:0 6px 16px rgba(37,99,235,0.12);
  display:flex;
  align-items:center;
  justify-content:center;
  background:linear-gradient(135deg,#e6eefc,#fff);
  font-weight:700;
  font-size:36px;
  color:var(--accent);
}

.name{
  font-size:18px;
  font-weight:700;
  letter-spacing:0.2px;
  text-align:center;
}

.role{
  font-size:13px;
  color:var(--muted);
}

.contact-quick{
  margin-top:6px;
  width:100%;
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:8px;
}

.chip {
  background: #f1f5f9;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 13px;
  color: #1e293b;        /* đậm hơn */
  font-weight: 600;
  text-align: center;
  box-shadow: 0 2px 8px rgba(15,23,42,0.05);
}

/* right: detail fields */
.profile-right{
  padding:28px;
  flex:1;
  min-width:0;
}

.header-row{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:12px;
  margin-bottom:14px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.subtitle{
  font-size:13px;
  color:var(--muted);
}

.btn{
  border:0;
  padding:8px 12px;
  border-radius:10px;
  font-weight:600;
  cursor:pointer;
  background:var(--accent);
  color:white;
  box-shadow:0 8px 18px rgba(37,99,235,0.12);
}

.btn.ghost{
  background:transparent;
  color:var(--accent);
  border:1px solid rgba(37,99,235,0.12);
  box-shadow:none;
}

.field-list{
  margin-top:8px;
  display:grid;
  gap:12px;
}

.field {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid rgba(15,23,42,0.08);
}

.label{
  width:140px;
  font-size:13px;
  color:var(--muted);
  flex-shrink:0;
}

.value {
  font-size: 15px;
  color: #0f172a;
  font-weight: 700;     /* đậm rõ ràng */
  letter-spacing: 0.2px;
}


/* edit form styles */
.edit-form{
  margin-top:12px;
  background:#fff;
  border-radius:10px;
  padding:12px;
  border:1px dashed rgba(15,23,42,0.04);
}

.form-row{
  display:flex;
  gap:12px;
  align-items:center;
  margin-bottom:10px;
}

.form-row input[type="text"],
.form-row input[type="email"],
.form-row input[type="tel"],
.form-row select{
  flex:1;
  padding:10px 12px;
  border-radius:8px;
  border:1px solid rgba(15,23,42,0.06);
  font-size:14px;
  background:transparent;
  outline:none;
}

.actions{
  margin-top:10px;
  display:flex;
  gap:8px;
  justify-content:flex-end;
}

.note{
  font-size:12px;
  color:var(--muted);
  margin-top:8px;
}

/* responsive */
@media (max-width:820px){
  body{ padding:18px; }
  .profile-card{ flex-direction:column; max-width:520px; }
  .profile-left{ flex:unset; width:100%; padding:18px; }
  .label{ width:110px; }
}
</style>