<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import HomeLayout from '@/layout/Header.vue'
import Footer from '@/layout/Footer.vue'
import { toast } from 'vue3-toastify'
import { useUserStore } from '@/userStore'
import { authService } from '@/service/AuthService.ts'
import {
  Bell,
  CheckCircle2,
  ChevronRight,
  ClipboardList,
  Package,
  Truck,
  MessageSquare,
  Settings,
  AlertCircle,
  FileText,
  Headphones,
  User,
  Mail,
  Phone,
  MapPin,
  ShieldCheck,
  Info,
  HelpCircle,
  ArrowLeft,
  ShieldAlert,
  CreditCard,
  Globe,
  Lock,
  UserX,
  LogOut
} from '@lucide/vue'

type UserProfile = {
  fullName?: string
  telNo?: string
  email?: string
  address?: string
  gender?: string
  role?: string
}

const currentView = ref<'menu' | 'settings'>('menu')
const isEditing = ref(false)
const activeModal = ref<string | null>(null) 
// Extra modals for subpage
const showSecurityModal = ref(false)
const showAddressModal = ref(false)
const showBankModal = ref(false)
const showNotificationSettingsModal = ref(false)
const showLanguageModal = ref(false)
const showPrivacyModal = ref(false)
const showDeleteAccountModal = ref(false)

const userStore = useUserStore()
const router = useRouter()

const user = computed<UserProfile>(() => (userStore.user as UserProfile | null) ?? {})

// Form state for editing profile
const editForm = ref({
  fullName: '',
  telNo: '',
  email: '',
  address: '',
  gender: '',
})

// Password change fields
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// Address field
const addressInput = ref('')

// Toggles for notifications
const notifyPrefs = ref({
  orders: true,
  promos: false,
  chats: true,
})

// Selected language
const selectedLanguage = ref('vi')

// Mock notifications state
const notifications = ref([
  { id: 1, title: 'Đơn hàng đã được xác nhận', time: '10 phút trước', desc: 'Đơn hàng #DH-2849 đang được đóng gói.', read: false },
  { id: 2, title: 'Ưu đãi đặc biệt thành viên', time: '1 ngày trước', desc: 'Nhận ngay mã giảm giá 15% cho phụ kiện.', read: true },
  { id: 3, title: 'Chào mừng thành viên EcoLuck', time: '3 ngày trước', desc: 'Cảm ơn bạn đã tham gia hệ sinh thái EcoLuck.', read: true },
  { id: 4, title: 'Hệ thống bảo trì định kỳ', time: '5 ngày trước', desc: 'Bảo trì hệ thống từ 0h - 2h sáng chủ nhật tuần này.', read: true }
])

const openEditModal = () => {
  editForm.value = {
    fullName: user.value.fullName ?? '',
    telNo: user.value.telNo ?? '',
    email: user.value.email ?? '',
    address: user.value.address ?? '',
    gender: user.value.gender ?? 'male',
  }
  isEditing.value = true
}

const saveProfile = async () => {
  const profile = {
    fullName: editForm.value.fullName,
    telNo: editForm.value.telNo,
    email: editForm.value.email,
    address: editForm.value.address,
    gender: editForm.value.gender,
  }

  try {
    await userStore.updateUser(profile)
    isEditing.value = false
    toast.success('Cập nhật thông tin thành công!')
  } catch (err) {
    console.error('Update profile error', err)
    toast.error('Cập nhật thất bại!')
  }
}

// Save security credentials (mock)
const savePassword = () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.error('Mật khẩu xác nhận không khớp!')
    return
  }
  toast.success('Đổi mật khẩu thành công!')
  showSecurityModal.value = false
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
}

// Save Address
const saveAddress = async () => {
  try {
    await userStore.updateUser({ address: addressInput.value })
    showAddressModal.value = false
    toast.success('Cập nhật địa chỉ nhận hàng thành công!')
  } catch (err) {
    console.error('Update address error', err)
    toast.error('Không thể cập nhật địa chỉ!')
  }
}

// Confirm deletion (mock)
const confirmDeleteAccount = () => {
  toast.success('Yêu cầu hủy tài khoản đã được tiếp nhận và xử lý trong 15 ngày.')
  showDeleteAccountModal.value = false
}

// Helpers
const getAvatarPlaceholder = (name?: string) => {
  if (!name) return 'BD'
  const parts = name.trim().split(/\s+/)
  const first = parts[0]
  const last = parts[parts.length - 1]
  if (parts.length >= 2 && first && last) {
    const firstChar = first.charAt(0)
    const lastChar = last.charAt(0)
    return (firstChar + lastChar).toUpperCase()
  }
  return name.slice(0, 2).toUpperCase()
}

const navigateToOrders = () => {
  router.push('/customer/order-by-user')
}

const openModal = (type: string) => {
  activeModal.value = type
}

const closeModal = () => {
  activeModal.value = null
}

const openAddressModal = () => {
  addressInput.value = user.value.address ?? ''
  showAddressModal.value = true
}

const sendComplaint = () => {
  toast.success('Gửi khiếu nại thành công! Chúng tôi sẽ phản hồi trong 24h.')
  closeModal()
}

// logout
const logout = () => {
  authService.logout()
  toast.success('Đăng xuất thành công!')
  router.push('/login')
}

onMounted(async () => {
  await userStore.getUserByLoginId()
})
</script>

<template>
  <HomeLayout />

  <div class="settings-page-wrapper">
    <div class="settings-container">
      
      <!-- Current View: Menu Dashboard (Main personal page) -->
      <div v-if="currentView === 'menu'" class="settings-menu-view">
        <!-- User Profile Header Banner -->
        <section class="profile-banner-card">
          <div class="profile-banner-background">
            <div class="banner-dots"></div>
          </div>
          
          <div class="profile-banner-content">
            <div class="user-main-info">
              <div class="avatar-container">
                <div class="avatar-circle">
                  {{ getAvatarPlaceholder(user.fullName) }}
                </div>
                <span class="level-tag">Lv.1</span>
              </div>
              
              <div class="user-text-details">
                <h2 class="user-display-name">
                  {{ user.fullName || 'Đang tải...' }}
                  <CheckCircle2 :size="18" class="verified-badge-icon" />
                </h2>
                
              </div>
            </div>
            
            <button class="notification-trigger-btn" @click="openModal('notifications')" aria-label="Thông báo">
              <Bell :size="24" />
              <span class="notification-count-badge">4</span>
            </button>
          </div>
        </section>

        <!-- My Orders Card -->
        <section class="settings-section-card">
          <div class="section-card-header">
            <h3 class="section-card-title">Đơn hàng của tôi</h3>
            <button class="section-card-action-link" @click="navigateToOrders">
              <span>Xem tất cả</span>
              <ChevronRight :size="16" />
            </button>
          </div>
          
          <div class="orders-grid">
            <button class="grid-item-button" @click="navigateToOrders">
              <div class="icon-circle green">
                <ClipboardList :size="22" />
              </div>
              <span class="grid-item-label">Chờ xác nhận</span>
            </button>
            
            <button class="grid-item-button" @click="navigateToOrders">
              <div class="icon-circle green">
                <Package :size="22" />
              </div>
              <span class="grid-item-label">Đang đóng gói</span>
            </button>
            
            <button class="grid-item-button" @click="navigateToOrders">
              <div class="icon-circle green">
                <Truck :size="22" />
              </div>
              <span class="grid-item-label">Đang vận chuyển</span>
            </button>
            
            <button class="grid-item-button" @click="navigateToOrders">
              <div class="icon-circle green">
                <MessageSquare :size="22" />
              </div>
              <span class="grid-item-label">Đánh giá</span>
            </button>
          </div>
        </section>

        <!-- Utilities & Features Card -->
        <section class="settings-section-card">
          <div class="section-card-header">
            <h3 class="section-card-title">Tiện ích & Tính năng</h3>
          </div>
          
          <div class="utilities-grid">
            <button class="grid-item-button" @click="currentView = 'settings'">
              <div class="icon-circle blue">
                <Settings :size="22" />
              </div>
              <span class="grid-item-label">Cài đặt</span>
            </button>
            
            <button class="grid-item-button" @click="openModal('notifications')">
              <div class="icon-circle green">
                <Bell :size="22" />
              </div>
              <span class="grid-item-label">Thông báo</span>
            </button>
            
            <button class="grid-item-button" @click="openModal('complaints')">
              <div class="icon-circle orange">
                <AlertCircle :size="22" />
              </div>
              <span class="grid-item-label">Khiếu nại</span>
            </button>
            
            <button class="grid-item-button" @click="openModal('rules')">
              <div class="icon-circle indigo">
                <FileText :size="22" />
              </div>
              <span class="grid-item-label">Thể lệ</span>
            </button>
            
            <button class="grid-item-button" @click="openModal('support')">
              <div class="icon-circle rose">
                <Headphones :size="22" />
              </div>
              <span class="grid-item-label">Hỗ trợ</span>
            </button>
          </div>
        </section>
      </div>

      <!-- Current View: Settings Details (Subpage view matching the layout) -->
      <div v-else-if="currentView === 'settings'" class="settings-subpage-view">
        <!-- Subpage Header -->
        

        <!-- Subpage List Content -->
        <div class="settings-list-content">
          <!-- Section 1: Tài khoản của tôi -->
          <div class="settings-group">
            <h4 class="group-header-title">Tài khoản của tôi</h4>
            <div class="group-list-card">
              <!-- Item 1: Thông tin cá nhân -->
              <button class="list-item-row" @click="openEditModal">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <User :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Thông tin cá nhân</span>
                    <span class="item-sub-desc">Quản lý thông tin cá nhân</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 2: Tài khoản & Bảo mật -->
              <button class="list-item-row" @click="showSecurityModal = true">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <ShieldAlert :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Tài khoản & Bảo mật</span>
                    <span class="item-sub-desc">Đổi mật khẩu, xác thực 2 bước</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 3: Địa chỉ nhận hàng -->
              <button class="list-item-row" @click="openAddressModal">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <MapPin :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Địa chỉ nhận hàng</span>
                    <span class="item-sub-desc">Quản lý địa chỉ giao hàng</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 4: Tài khoản / Thẻ ngân hàng -->
              <button class="list-item-row" @click="showBankModal = true">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <CreditCard :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Tài khoản / Thẻ ngân hàng</span>
                    <span class="item-sub-desc">Liên kết và quản lý tài khoản</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>
            </div>
          </div>

          <!-- Section 2: Cài đặt -->
          <div class="settings-group">
            <h4 class="group-header-title">Cài đặt</h4>
            <div class="group-list-card">
              <!-- Item 1: Cài đặt thông báo -->
              <button class="list-item-row" @click="showNotificationSettingsModal = true">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <Bell :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Cài đặt thông báo</span>
                    <span class="item-sub-desc">Tùy chỉnh thông báo</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 2: Ngôn ngữ -->
              <button class="list-item-row" @click="showLanguageModal = true">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <Globe :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Ngôn ngữ</span>
                    <span class="item-sub-desc">{{ selectedLanguage === 'vi' ? 'Tiếng Việt' : 'English' }}</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 3: Quyền riêng tư -->
              <button class="list-item-row" @click="showPrivacyModal = true">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <Lock :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Quyền riêng tư</span>
                    <span class="item-sub-desc">Quản lý quyền riêng tư</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>
            </div>
          </div>

          <!-- Section 3: Hỗ trợ -->
          <div class="settings-group">
            <h4 class="group-header-title">Hỗ trợ</h4>
            <div class="group-list-card">
              <!-- Item 1: Trung tâm hỗ trợ -->
              <button class="list-item-row" @click="openModal('support')">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <HelpCircle :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Trung tâm hỗ trợ</span>
                    <span class="item-sub-desc">Tìm câu trả lời và liên hệ hỗ trợ</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 2: Điều khoản & Chính sách -->
              <button class="list-item-row" @click="openModal('rules')">
                <div class="item-left-side">
                  <div class="circle-icon green">
                    <FileText :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Điều khoản & Chính sách</span>
                    <span class="item-sub-desc">Xem điều khoản và chính sách</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>

              <!-- Item 3: Yêu cầu hủy tài khoản -->
              <button class="list-item-row" @click="showDeleteAccountModal = true">
                <div class="item-left-side">
                  <div class="circle-icon red">
                    <UserX :size="18" />
                  </div>
                  <div class="item-text-block">
                    <span class="item-main-title">Yêu cầu hủy tài khoản</span>
                    <span class="item-sub-desc">Yêu cầu xóa tài khoản vĩnh viễn</span>
                  </div>
                </div>
                <ChevronRight :size="16" class="chevron-right-arrow" />
              </button>
            </div>
          </div>

          <!-- Log out Button -->
          <button class="logout-action-btn" @click="logout">
            <LogOut :size="18" />
            <span>Đăng xuất</span>
          </button>
        </div>
      </div>

    </div>
  </div>

  <!-- Settings Modal (Edit Profile) -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="isEditing" @click.self="isEditing = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Thông tin cá nhân</h3>
          <button class="close-modal-btn" @click="isEditing = false">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveProfile" class="modern-settings-form">
            <div class="form-group-item">
              <label>Họ và tên</label>
              <div class="form-input-wrapper">
                <User :size="18" class="form-input-icon" />
                <input v-model="editForm.fullName" type="text" placeholder="Nhập họ và tên" required />
              </div>
            </div>

            <div class="form-group-item">
              <label>Email</label>
              <div class="form-input-wrapper">
                <Mail :size="18" class="form-input-icon" />
                <input v-model="editForm.email" type="email" placeholder="Nhập email" required />
              </div>
            </div>

            <div class="form-group-item">
              <label>Số điện thoại</label>
              <div class="form-input-wrapper">
                <Phone :size="18" class="form-input-icon" />
                <input v-model="editForm.telNo" type="tel" placeholder="Nhập số điện thoại" required />
              </div>
            </div>

            <div class="form-group-item">
              <label>Địa chỉ</label>
              <div class="form-input-wrapper">
                <MapPin :size="18" class="form-input-icon" />
                <input v-model="editForm.address" type="text" placeholder="Nhập địa chỉ" />
              </div>
            </div>

            <div class="form-group-item">
              <label>Giới tính</label>
              <div class="form-input-wrapper">
                <ShieldCheck :size="18" class="form-input-icon" />
                <select v-model="editForm.gender">
                  <option value="male">Nam</option>
                  <option value="female">Nữ</option>
                  <option value="other">Khác</option>
                </select>
              </div>
            </div>

            <div class="modal-footer-actions">
              <button type="button" class="btn-secondary-action" @click="isEditing = false">Hủy</button>
              <button type="submit" class="btn-primary-action">Lưu thay đổi</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Security & Password Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showSecurityModal" @click.self="showSecurityModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Tài khoản & Bảo mật</h3>
          <button class="close-modal-btn" @click="showSecurityModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="info-alert-banner">
            <Lock :size="18" />
            <span>Hãy đổi mật khẩu định kỳ để bảo vệ an toàn tài khoản của bạn.</span>
          </div>

          <form @submit.prevent="savePassword" class="modern-settings-form">
            <div class="form-group-item">
              <label>Mật khẩu hiện tại</label>
              <div class="form-input-wrapper">
                <Lock :size="18" class="form-input-icon" />
                <input v-model="passwordForm.oldPassword" type="password" placeholder="Nhập mật khẩu cũ" required />
              </div>
            </div>

            <div class="form-group-item">
              <label>Mật khẩu mới</label>
              <div class="form-input-wrapper">
                <Lock :size="18" class="form-input-icon" />
                <input v-model="passwordForm.newPassword" type="password" placeholder="Mật khẩu mới từ 6 ký tự" required />
              </div>
            </div>

            <div class="form-group-item">
              <label>Xác nhận mật khẩu mới</label>
              <div class="form-input-wrapper">
                <Lock :size="18" class="form-input-icon" />
                <input v-model="passwordForm.confirmPassword" type="password" placeholder="Xác nhận lại mật khẩu mới" required />
              </div>
            </div>

            <div class="modal-footer-actions">
              <button type="button" class="btn-secondary-action" @click="showSecurityModal = false">Hủy</button>
              <button type="submit" class="btn-primary-action">Cập nhật mật khẩu</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Shipping Address Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showAddressModal" @click.self="showAddressModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Địa chỉ nhận hàng</h3>
          <button class="close-modal-btn" @click="showAddressModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveAddress" class="modern-settings-form">
            <div class="form-group-item">
              <label>Địa chỉ giao hàng mặc định</label>
              <textarea v-model="addressInput" placeholder="Nhập số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố..." rows="4" required></textarea>
            </div>

            <div class="modal-footer-actions">
              <button type="button" class="btn-secondary-action" @click="showAddressModal = false">Hủy</button>
              <button type="submit" class="btn-primary-action">Lưu địa chỉ</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Bank Account/Cards Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showBankModal" @click.self="showBankModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Tài khoản / Thẻ ngân hàng</h3>
          <button class="close-modal-btn" @click="showBankModal = false">&times;</button>
        </div>
        <div class="modal-body text-center py-4">
          <div class="credit-card-mockup">
            <div class="card-chip"></div>
            <div class="card-number">•••• •••• •••• 8294</div>
            <div class="card-footer-row">
              <span class="card-holder">{{ user.fullName || 'MEMBER' }}</span>
              <span class="card-expiry">12/29</span>
            </div>
          </div>
          <p class="mt-4 text-muted fs-7">Tính năng liên kết và thanh toán trực tiếp qua thẻ ngân hàng hiện tại đang được cập nhật và bảo trì. Xin lỗi bạn vì sự bất tiện này.</p>
          <div class="modal-footer-actions justify-content-center">
            <button type="button" class="btn-primary-action" @click="showBankModal = false">Đã hiểu</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Notification Settings Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showNotificationSettingsModal" @click.self="showNotificationSettingsModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Cài đặt thông báo</h3>
          <button class="close-modal-btn" @click="showNotificationSettingsModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="toggle-list">
            <div class="toggle-item-row">
              <div class="toggle-text">
                <h5>Thông báo đơn hàng</h5>
                <p>Cập nhật trạng thái chuẩn bị và giao nhận đơn hàng của bạn.</p>
              </div>
              <label class="switch-toggle">
                <input type="checkbox" v-model="notifyPrefs.orders" />
                <span class="slider-round"></span>
              </label>
            </div>

            <div class="toggle-item-row">
              <div class="toggle-text">
                <h5>Thông báo khuyến mãi</h5>
                <p>Nhận tin nhắn về các chương trình ưu đãi, giảm giá đặc biệt.</p>
              </div>
              <label class="switch-toggle">
                <input type="checkbox" v-model="notifyPrefs.promos" />
                <span class="slider-round"></span>
              </label>
            </div>

            <div class="toggle-item-row">
              <div class="toggle-text">
                <h5>Hỗ trợ & Chat trực tuyến</h5>
                <p>Thông báo khi có phản hồi mới từ nhân viên CSKH hoặc hệ thống khiếu nại.</p>
              </div>
              <label class="switch-toggle">
                <input type="checkbox" v-model="notifyPrefs.chats" />
                <span class="slider-round"></span>
              </label>
            </div>
          </div>
          <div class="modal-footer-actions">
            <button type="button" class="btn-primary-action" @click="showNotificationSettingsModal = false">Lưu cấu hình</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Language Selection Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showLanguageModal" @click.self="showLanguageModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Ngôn ngữ / Language</h3>
          <button class="close-modal-btn" @click="showLanguageModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="language-options-list">
            <label class="lang-option-card">
              <input type="radio" value="vi" v-model="selectedLanguage" name="lang" />
              <span class="lang-label">Tiếng Việt (Việt Nam)</span>
            </label>
            <label class="lang-option-card">
              <input type="radio" value="en" v-model="selectedLanguage" name="lang" />
              <span class="lang-label">English (United States)</span>
            </label>
          </div>
          <div class="modal-footer-actions">
            <button type="button" class="btn-primary-action" @click="showLanguageModal = false">Xác nhận</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Privacy Settings Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showPrivacyModal" @click.self="showPrivacyModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Quyền riêng tư</h3>
          <button class="close-modal-btn" @click="showPrivacyModal = false">&times;</button>
        </div>
        <div class="modal-body rich-text-body">
          <h5>1. Chia sẻ dữ liệu tài khoản</h5>
          <p>Dữ liệu mua sắm và lịch sử truy cập của bạn được mã hóa an toàn trên hệ thống. Chúng tôi cam kết không bán dữ liệu này cho bên thứ ba dưới bất kỳ hình thức nào.</p>
          
          <h5>2. Định vị thiết bị</h5>
          <p>Chúng tôi chỉ sử dụng quyền định vị GPS khi bạn cho phép để tự động đề xuất địa chỉ giao hàng gần nhất hoặc các cửa hàng lân cận.</p>
          
          <h5>3. Quản lý quyền theo dõi</h5>
          <p>Bạn có thể chủ động tắt hoặc mở tính năng nhận quảng cáo đề xuất dựa trên sở thích mua sắm bằng cách cấu hình trực tiếp trong tài khoản của bạn.</p>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Delete Account Danger Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="showDeleteAccountModal" @click.self="showDeleteAccountModal = false">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3 class="text-danger">Yêu cầu hủy tài khoản</h3>
          <button class="close-modal-btn" @click="showDeleteAccountModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="danger-alert-banner">
            <AlertCircle :size="24" />
            <div>
              <h5>Hành động này không thể hoàn tác!</h5>
              <p>Khi bạn yêu cầu hủy tài khoản, mọi thông tin cá nhân, lịch sử đơn hàng và tài khoản thành viên EcoLuck sẽ bị xóa vĩnh viễn sau 15 ngày.</p>
            </div>
          </div>
          <div class="modal-footer-actions">
            <button type="button" class="btn-secondary-action" @click="showDeleteAccountModal = false">Hủy</button>
            <button type="button" class="btn-danger-action" @click="confirmDeleteAccount">Tôi đồng ý xóa</button>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Rules Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="activeModal === 'rules'" @click.self="closeModal">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Thể lệ & Điều khoản</h3>
          <button class="close-modal-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body rich-text-body">
          <h4>1. Điều khoản Thành viên EcoLuck</h4>
          <p>Mỗi khách hàng mua sắm tại Phone Store sẽ tự động trở thành thành viên EcoLuck. Thành viên được hưởng các chính sách ưu đãi giá, tích điểm mua sắm và hỗ trợ kỹ thuật ưu tiên.</p>
          
          <h4>2. Chính sách bảo mật</h4>
          <p>Chúng tôi cam kết bảo mật tuyệt đối thông tin cá nhân của bạn, bao gồm email, số điện thoại, địa chỉ và lịch sử giao dịch mua sắm tại cửa hàng.</p>
          
          <h4>3. Quyền lợi và Nghĩa vụ</h4>
          <p>Khách hàng có quyền yêu cầu hỗ trợ, đổi trả hàng theo quy định của cửa hàng và có nghĩa vụ cung cấp đúng thông tin giao hàng khi đặt mua sản phẩm.</p>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Support Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="activeModal === 'support'" @click.self="closeModal">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Hỗ trợ khách hàng</h3>
          <button class="close-modal-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body support-body">
          <div class="support-contact-card">
            <Phone :size="24" class="support-icon" />
            <div class="support-contact-info">
              <h5>Tổng đài hỗ trợ (Mỹ phẩm & Công nghệ)</h5>
              <p class="highlight-number">1900 1234</p>
              <p class="sub-text">Hỗ trợ từ 8:00 đến 21:30 hàng ngày</p>
            </div>
          </div>

          <div class="support-contact-card">
            <Mail :size="24" class="support-icon" />
            <div class="support-contact-info">
              <h5>Email tiếp nhận thông tin</h5>
              <p class="highlight-email">support@ecoluck.vn</p>
              <p class="sub-text">Chúng tôi phản hồi email của bạn trong 2 giờ làm việc</p>
            </div>
          </div>

          <div class="support-contact-card">
            <HelpCircle :size="24" class="support-icon" />
            <div class="support-contact-info">
              <h5>Trợ lý ảo & Hỗ trợ kỹ thuật</h5>
              <p class="sub-text">Vui lòng bấm vào biểu tượng chat ở góc dưới màn hình để bắt đầu trao đổi trực tuyến với tư vấn viên.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Complaints Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="activeModal === 'complaints'" @click.self="closeModal">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Gửi khiếu nại & Góp ý</h3>
          <button class="close-modal-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="info-alert-banner">
            <Info :size="18" />
            <span>Ý kiến đóng góp của bạn giúp chúng tôi cải thiện dịch vụ tốt hơn mỗi ngày.</span>
          </div>

          <form @submit.prevent="sendComplaint" class="modern-settings-form">
            <div class="form-group-item">
              <label>Loại khiếu nại/Góp ý</label>
              <select required>
                <option value="product">Chất lượng sản phẩm</option>
                <option value="service">Thái độ phục vụ của nhân viên</option>
                <option value="delivery">Thời gian / Vận chuyển đơn hàng</option>
                <option value="other">Góp ý phát triển tính năng hệ thống</option>
              </select>
            </div>

            <div class="form-group-item">
              <label>Nội dung chi tiết</label>
              <textarea placeholder="Vui lòng nhập chi tiết phản ánh của bạn..." rows="5" required></textarea>
            </div>

            <div class="modal-footer-actions">
              <button type="button" class="btn-secondary-action" @click="closeModal">Hủy</button>
              <button type="submit" class="btn-primary-action">Gửi phản hồi</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Notifications Modal -->
  <Transition name="modal-fade">
    <div class="modal-overlay" v-if="activeModal === 'notifications'" @click.self="closeModal">
      <div class="modal-container animate-slide-up">
        <div class="modal-header">
          <h3>Thông báo của tôi</h3>
          <button class="close-modal-btn" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body notification-modal-body">
          <div class="notification-list-wrapper">
            <div 
              v-for="notif in notifications" 
              :key="notif.id" 
              class="notification-item-card"
              :class="{ unread: !notif.read }"
            >
              <div class="notif-header-row">
                <span class="notif-title-text">{{ notif.title }}</span>
                <span class="notif-time-text">{{ notif.time }}</span>
              </div>
              <p class="notif-desc-text">{{ notif.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <Footer />
</template>

<style scoped>
.settings-page-wrapper {
  background-color: #f6f8fb;
  min-height: 100vh;
  padding: 40px 20px 80px 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.settings-container {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-menu-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* User Profile Header Banner */
.profile-banner-card {
  position: relative;
  background: linear-gradient(135deg, #10b981, #047857);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.3);
  padding: 32px;
}

.profile-banner-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  opacity: 0.15;
}

.banner-dots {
  position: absolute;
  right: -20px;
  top: -20px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 15px solid #ffffff;
}

.profile-banner-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 1;
}

.user-main-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-container {
  position: relative;
}

.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 3px solid #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 800;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.level-tag {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: #ffffff;
  color: #047857;
  font-size: 0.7rem;
  font-weight: 800;
  padding: 2px 10px;
  border-radius: 9999px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid #10b981;
}

.user-text-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-display-name {
  color: #ffffff;
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.verified-badge-icon {
  color: #ffffff;
  fill: #10b981;
}

.user-subtitle-role {
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.9rem;
  margin: 0;
  font-weight: 500;
}

.notification-trigger-btn {
  position: relative;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: #ffffff;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.notification-trigger-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.05);
}

.notification-count-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: #ffffff;
  font-size: 0.72rem;
  font-weight: 800;
  min-width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #10b981;
}

/* Settings Card Block */
.settings-section-card {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(16, 185, 129, 0.08);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  padding: 24px;
}

.section-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-card-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.section-card-action-link {
  background: transparent;
  border: none;
  color: #10b981;
  font-weight: 700;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.section-card-action-link:hover {
  color: #047857;
}

/* Grids layout */
.orders-grid, .utilities-grid {
  display: grid;
  gap: 16px;
  width: 100%;
}

.orders-grid {
  grid-template-columns: repeat(4, 1fr);
}

.utilities-grid {
  grid-template-columns: repeat(5, 1fr);
}

.grid-item-button {
  background: transparent;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  padding: 12px 6px;
  border-radius: 16px;
}

.grid-item-button:hover {
  background: #f8fafc;
  transform: translateY(-2px);
}

.icon-circle {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}

.grid-item-button:hover .icon-circle {
  transform: scale(1.08);
}

/* Icon circles color schemes */
.icon-circle.green, .circle-icon.green {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}
.icon-circle.blue {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}
.icon-circle.orange {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}
.icon-circle.indigo {
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
}
.icon-circle.rose {
  background: rgba(244, 63, 94, 0.1);
  color: #f43f5e;
}
.circle-icon.red {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.grid-item-label {
  font-size: 0.82rem;
  font-weight: 700;
  color: #475569;
  text-align: center;
}

/* SUBPAGE VIEW (Cài đặt tài khoản) */
.settings-subpage-view {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  padding: 0;
  overflow: hidden;
}

.subpage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.back-navigation-btn {
  background: transparent;
  border: none;
  color: #475569;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.back-navigation-btn:hover {
  background: #f1f5f9;
}

.subpage-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  text-align: center;
  flex: 1;
}

.header-empty-space {
  width: 36px;
}

.settings-list-content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.group-header-title {
  font-size: 0.82rem;
  font-weight: 700;
  color: #94a3b8;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding-left: 8px;
}

.group-list-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.list-item-row {
  background: transparent;
  border: none;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  text-align: left;
  transition: background-color 0.2s;
  width: 100%;
}

.list-item-row:last-child {
  border-bottom: none;
}

.list-item-row:hover {
  background: #f8fafc;
}

.item-left-side {
  display: flex;
  align-items: center;
  gap: 16px;
}

.circle-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-text-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-main-title {
  font-size: 0.92rem;
  font-weight: 700;
  color: #1e293b;
}

.item-sub-desc {
  font-size: 0.78rem;
  color: #94a3b8;
  font-weight: 500;
}

.chevron-right-arrow {
  color: #cbd5e1;
  transition: transform 0.2s;
}

.list-item-row:hover .chevron-right-arrow {
  transform: translateX(2px);
  color: #475569;
}

/* Logout Button */
.logout-action-btn {
  margin-top: 8px;
  width: 100%;
  height: 48px;
  background: #ffffff;
  border: 1px solid #fee2e2;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #ef4444;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-action-btn:hover {
  background: #fef2f2;
  border-color: #fca5a5;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.05);
}

/* Modals System Styling */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: #ffffff;
  border-radius: 24px;
  width: 90%;
  max-width: 540px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 1.2rem;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}

.close-modal-btn {
  background: transparent;
  border: none;
  font-size: 1.8rem;
  line-height: 1;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.close-modal-btn:hover {
  color: #475569;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

/* Credit Card Mockup */
.credit-card-mockup {
  width: 100%;
  max-width: 340px;
  height: 190px;
  border-radius: 20px;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin: 0 auto;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
  color: #ffffff;
  position: relative;
  overflow: hidden;
}

.credit-card-mockup::after {
  content: '';
  position: absolute;
  right: -50px;
  bottom: -50px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.02);
  pointer-events: none;
}

.card-chip {
  width: 44px;
  height: 32px;
  background: linear-gradient(135deg, #d4af37, #f3e5ab);
  border-radius: 6px;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.5);
}

.card-number {
  font-size: 1.3rem;
  letter-spacing: 2px;
  font-weight: 600;
  margin-top: 24px;
  text-align: left;
  font-family: monospace;
}

.card-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-holder {
  font-size: 0.82rem;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 1px;
}

.card-expiry {
  font-size: 0.82rem;
  font-family: monospace;
}

/* Switch toggle styling */
.toggle-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 8px;
}

.toggle-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.toggle-item-row:last-child {
  border-bottom: none;
}

.toggle-text {
  flex: 1;
  padding-right: 20px;
  text-align: left;
}

.toggle-text h5 {
  font-size: 0.9rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.toggle-text p {
  font-size: 0.76rem;
  color: #94a3b8;
  margin: 0;
  line-height: 1.4;
}

.switch-toggle {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 28px;
  flex-shrink: 0;
}

.switch-toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider-round {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: .3s;
  border-radius: 34px;
}

.slider-round:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .3s;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

input:checked + .slider-round {
  background-color: #10b981;
}

input:checked + .slider-round:before {
  transform: translateX(22px);
}

/* Language selector option */
.language-options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lang-option-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.lang-option-card:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.lang-option-card input[type="radio"]:checked + .lang-label {
  color: #10b981;
  font-weight: 700;
}

.lang-option-card input[type="radio"] {
  accent-color: #10b981;
  width: 18px;
  height: 18px;
}

.lang-label {
  font-size: 0.92rem;
  color: #475569;
}

/* Danger alert banner & button */
.danger-alert-banner {
  background: #fff5f5;
  border: 1px solid #fee2e2;
  color: #991b1b;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
  text-align: left;
}

.danger-alert-banner h5 {
  font-size: 0.9rem;
  font-weight: 800;
  margin: 0 0 6px 0;
}

.danger-alert-banner p {
  font-size: 0.8rem;
  line-height: 1.5;
  margin: 0;
}

.btn-danger-action {
  height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  background: #ef4444;
  border: none;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
  transition: all 0.2s;
}

.btn-danger-action:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

/* Modern Form Inside Modal */
.modern-settings-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group-item label {
  font-size: 0.88rem;
  font-weight: 700;
  color: #475569;
  text-align: left;
}

.form-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input-icon {
  position: absolute;
  left: 14px;
  color: #94a3b8;
  pointer-events: none;
}

.form-input-wrapper input,
.form-input-wrapper select,
.form-group-item select,
.form-group-item textarea {
  width: 100%;
  padding: 10px 14px 10px 42px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95rem;
  outline: none;
  background: #ffffff;
  color: #1f2937;
  transition: all 0.2s;
}

.form-group-item select,
.form-group-item textarea {
  padding-left: 14px;
}

.form-input-wrapper input:focus,
.form-input-wrapper select:focus,
.form-group-item select:focus,
.form-group-item textarea:focus {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.08);
}

.modal-footer-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 12px;
}

.btn-secondary-action, .btn-primary-action {
  height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary-action {
  background: #f1f5f9;
  border: none;
  color: #475569;
}

.btn-secondary-action:hover {
  background: #e2e8f0;
}

.btn-primary-action {
  background: #10b981;
  border: none;
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.btn-primary-action:hover {
  background: #047857;
  transform: translateY(-1px);
}

/* Rich Text & Specialized Modals */
.rich-text-body h4 {
  font-size: 0.98rem;
  font-weight: 800;
  color: #1e293b;
  margin-top: 20px;
  margin-bottom: 8px;
}

.rich-text-body h4:first-of-type {
  margin-top: 0;
}

.rich-text-body h5 {
  font-size: 0.92rem;
  font-weight: 800;
  color: #1e293b;
  margin-top: 18px;
  margin-bottom: 6px;
  text-align: left;
}

.rich-text-body h5:first-of-type {
  margin-top: 0;
}

.rich-text-body p {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #475569;
  margin-top: 0;
  margin-bottom: 16px;
  text-align: left;
}

.info-alert-banner {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1e3a8a;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  text-align: left;
}

.support-contact-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  margin-bottom: 16px;
}

.support-contact-card:last-child {
  margin-bottom: 0;
}

.support-icon {
  color: #10b981;
  margin-top: 2px;
}

.support-contact-info h5 {
  font-size: 0.92rem;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 6px 0;
  text-align: left;
}

.highlight-number {
  font-size: 1.3rem;
  font-weight: 800;
  color: #ef4444;
  margin: 0 0 4px 0;
  text-align: left;
}

.highlight-email {
  font-size: 1rem;
  font-weight: 700;
  color: #10b981;
  margin: 0 0 4px 0;
  text-align: left;
}

.sub-text {
  font-size: 0.8rem;
  color: #64748b;
  margin: 0;
  line-height: 1.4;
  text-align: left;
}

/* Notifications Modal Styles */
.notification-modal-body {
  padding: 12px 24px;
}

.notification-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item-card {
  padding: 16px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  transition: all 0.2s;
}

.notification-item-card.unread {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.notif-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.notif-title-text {
  font-size: 0.9rem;
  font-weight: 800;
  color: #1e293b;
}

.unread .notif-title-text {
  color: #047857;
}

.notif-time-text {
  font-size: 0.72rem;
  color: #94a3b8;
  font-weight: 600;
}

.notif-desc-text {
  font-size: 0.82rem;
  color: #475569;
  margin: 0;
  line-height: 1.4;
  text-align: left;
}

/* Transition Animations */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.animate-slide-up {
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Responsiveness */
@media (max-width: 640px) {
  .orders-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .utilities-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .profile-banner-card {
    padding: 24px 20px;
  }

  .user-display-name {
    font-size: 1.25rem;
  }

  .avatar-circle {
    width: 64px;
    height: 64px;
    font-size: 22px;
  }
  
  .list-item-row {
    padding: 12px 14px;
  }
  
  .subpage-header {
    padding: 12px 16px;
  }
}
</style>