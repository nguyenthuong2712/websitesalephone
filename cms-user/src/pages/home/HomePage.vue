<script setup lang="ts">
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import {productService} from "@/service/ProductService.ts";
import {onBeforeUnmount, onMounted, ref} from "vue";
import {formatCurrency} from "../../utils/Constant.ts";
import { 
  Tag, 
  ShoppingBag, 
  Ticket, 
  ShieldCheck, 
  Truck, 
  Award, 
  Sparkles, 
  Shield, 
  CreditCard,
  Quote,
  Star,
  Mail
} from '@lucide/vue';

const products = ref<any[]>([]);

const loadProducts = async () => {
  try {
    const res = await productService.getAllNewProduct();
    products.value = res.data.data || [];
  } catch (error) {
    console.error("Lỗi khi load sản phẩm:", error);
  }
};

const handleVisibilityChange = () => {
  if (document.visibilityState === "visible") {
    loadProducts();
  }
};

// Compute promotional details matching mockup values
const getProductPromoDetails = (product: any) => {
  if (!product.price) {
    return {
      originalPrice: 0,
      discountPercent: 0,
      isInline: false,
      isNew: true
    };
  }

  const name = product.productName.toLowerCase();
  
  let discountRate = 3;
  let isInline = false;
  
  if (name.includes('air')) {
    discountRate = 28;
  } else if (name.includes('512') && name.includes('17e')) {
    discountRate = 6;
  } else if (name.includes('512') && name.includes('pro max')) {
    discountRate = 3;
  } else if (name.includes('512') && name.includes('pro')) {
    discountRate = 4;
  } else if (name.includes('17 pro max 256')) {
    discountRate = 3;
    isInline = true;
  } else if (name.includes('17 256')) {
    discountRate = 4;
    isInline = true;
  } else if (name.includes('17 pro 256')) {
    discountRate = 3;
  } else if (name.includes('17e 256')) {
    discountRate = 3;
  } else {
    // Fallback pseudo-random calculation based on character code sum
    const charCodeSum = product.productName.split('').reduce((sum: number, c: string) => sum + c.charCodeAt(0), 0);
    discountRate = (charCodeSum % 5) + 3; // 3% to 7%
    isInline = charCodeSum % 2 === 0;
  }

  const rawOriginal = product.price / (1 - discountRate / 100);
  const roundedOriginal = Math.ceil(rawOriginal / 1000000) * 1000000 - 1000;
  const actualDiscount = Math.round((roundedOriginal - product.price) / roundedOriginal * 100);

  return {
    originalPrice: roundedOriginal,
    discountPercent: actualDiscount || discountRate,
    isInline,
    isNew: true
  };
};

onMounted(() => {
  loadProducts();
  document.addEventListener("visibilitychange", handleVisibilityChange);
});

onBeforeUnmount(() => {
  document.removeEventListener("visibilitychange", handleVisibilityChange);
});

</script>

<template>
  <HomeLayout/>
  
  <!-- Redesigned Hero Section -->
  <section class="hero-section" id="home">
    <div class="hero-container">
      
      <!-- Left Column: Content -->
      <div class="hero-content-col">
        <span class="hero-subtitle-tag">Smartphone Chính Hãng</span>
        
        <h1 class="hero-title">
          Chuẩn Chính Hãng <br />
          Trọn Trải Nghiệm <span class="gradient-text">Đỉnh Cao</span>
        </h1>
        
        <p class="hero-desc">
          Chuyên cung cấp smartphone chính hãng, giá tốt nhất thị trường, bảo hành uy tín và giao hàng nhanh toàn quốc.
        </p>

        <!-- CTA Buttons -->
        <div class="hero-cta-group">
          <a href="#products" class="btn-primary-hero">
            <ShoppingBag :size="18" class="cta-btn-icon" />
            <span>Khám phá sản phẩm</span>
          </a>
          <a href="#products" class="btn-secondary-hero">
            <Ticket :size="18" class="cta-btn-icon" />
            <span>Ưu đãi hôm nay</span>
          </a>
        </div>

        <!-- Banner Trust badges row -->
        <div class="hero-badges-row">
          <div class="hero-badge-item">
            <div class="badge-icon-circle">
              <ShieldCheck :size="18" />
            </div>
            <div class="badge-text-block">
              <h4 class="badge-heading">Chính hãng 100%</h4>
              <p class="badge-sub">Cam kết hàng chính hãng</p>
            </div>
          </div>

          <div class="hero-badge-item">
            <div class="badge-icon-circle">
              <Truck :size="18" />
            </div>
            <div class="badge-text-block">
              <h4 class="badge-heading">Giao hàng nhanh</h4>
              <p class="badge-sub">Toàn quốc, siêu tốc</p>
            </div>
          </div>

          <div class="hero-badge-item">
            <div class="badge-icon-circle">
              <Award :size="18" />
            </div>
            <div class="badge-text-block">
              <h4 class="badge-heading">Bảo hành uy tín</h4>
              <p class="badge-sub">Hỗ trợ 12–24 tháng</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Visual -->
      <div class="hero-visual-col">
        <div class="hero-circle-backdrop"></div>
        <img src="../assets/hero_phone.png" alt="Phone Store Products" class="hero-promo-image" />
      </div>

    </div>
  </section>

  <!-- Products Section -->
  <main id="products">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Sản Phẩm Mới Nhất</h2>
        <p class="section-subtitle">Những chiếc điện thoại cao cấp nhất hiện nay</p>
      </div>

      <div class="products-grid">
        <router-link 
          v-for="product in products" 
          :key="product.id"
          :to="`detail-product/${product.id}`" 
          class="product-card"
        >
          <!-- Top Left Discount Badge (Only shown if NOT inline) -->
          <span 
            v-if="!getProductPromoDetails(product).isInline" 
            class="badge-discount-top"
          >
            Giảm {{ getProductPromoDetails(product).discountPercent }}%
          </span>
          
          <!-- Top Right 'Mới' Badge -->
          <span class="badge-new-top">
            <Tag :size="12" class="new-icon" />
            <span>Mới</span>
          </span>

          <!-- Product Image Container -->
          <div class="product-image-container">
            <img :src="product.url" :alt="product.productName" class="product-img" />
          </div>

          <!-- Product Info -->
          <div class="product-info-container">
            <h3 class="product-title">{{ product.productName }}</h3>
            
            <div class="price-container">
              <!-- Standard Price Layout (Main Price & Strike Price Inline Side-by-Side) -->
              <div 
                v-if="!getProductPromoDetails(product).isInline" 
                class="price-row-standard"
              >
                <span class="price-main">{{ formatCurrency(product.price) }}</span>
                <span class="price-original-strike">
                  {{ formatCurrency(getProductPromoDetails(product).originalPrice) }}
                </span>
              </div>

              <!-- Split Price Layout (Main Price Large on Top, Strike Price & Red Discount Badge below) -->
              <div v-else class="price-row-split">
                <div class="price-row-split-large">
                  {{ formatCurrency(product.price) }}
                </div>
                <div class="price-row-split-sub">
                  <span class="price-original-strike">
                    {{ formatCurrency(getProductPromoDetails(product).originalPrice) }}
                  </span>
                  <span class="badge-discount-inline">
                    -{{ getProductPromoDetails(product).discountPercent }}%
                  </span>
                </div>
              </div>
            </div>
          </div>
        </router-link>
      </div>
    </div>
  </main>

  <!-- Redesigned 'Why Choose Us' Features Section -->
  <section class="features-section-alt">
    <div class="features-container">
      
      <div class="features-header">
        <div class="features-tag">
          <Sparkles :size="12" class="sparkles-icon" />
          <span>Trải nghiệm an tâm</span>
        </div>
        <h2 class="features-title">Tại Sao Chọn Chúng Tôi</h2>
        <div class="title-underline"></div>
      </div>

      <div class="features-grid-alt">
        <!-- Item 1: Chính hãng 100% -->
        <div class="feature-card-alt">
          <div class="feature-icon-badge">
            <ShieldCheck :size="24" class="badge-icon" />
          </div>
          <h3 class="feature-heading">Chính Hãng 100%</h3>
          <div class="heading-line"></div>
          <p class="feature-description">
            Cam kết sản phẩm chính hãng, <br />
            có tem đầy đủ
          </p>
        </div>

        <!-- Item 2: Giao Hàng Miễn Phí -->
        <div class="feature-card-alt">
          <div class="feature-icon-badge">
            <Truck :size="24" class="badge-icon" />
          </div>
          <h3 class="feature-heading">Giao Hàng Miễn Phí</h3>
          <div class="heading-line"></div>
          <p class="feature-description">
            Miễn phí vận chuyển <br />
            toàn quốc
          </p>
        </div>

        <!-- Item 3: Bảo Hành 12 Tháng -->
        <div class="feature-card-alt">
          <div class="feature-icon-badge">
            <div class="badge-shield-container">
              <Shield :size="24" class="badge-icon" />
              <span class="shield-number">12</span>
            </div>
          </div>
          <h3 class="feature-heading">Bảo Hành 12 Tháng</h3>
          <div class="heading-line"></div>
          <p class="feature-description">
            Bảo hành chính hãng tại <br />
            trung tâm ủy quyền
          </p>
        </div>

        <!-- Item 4: Trả Góp 0% -->
        <div class="feature-card-alt">
          <div class="feature-icon-badge">
            <CreditCard :size="24" class="badge-icon" />
          </div>
          <h3 class="feature-heading">Trả Góp 0%</h3>
          <div class="heading-line"></div>
          <p class="feature-description">
            Hỗ trợ trả góp lãi suất 0% <br />
            qua thẻ
          </p>
        </div>
      </div>

    </div>
  </section>

  <!-- Redesigned Testimonials Section -->
  <section class="testimonials-section-alt">
    <div class="testimonials-container">
      
      <div class="testimonials-header">
        <Quote :size="32" class="quote-header-icon" />
        <h2 class="testimonials-title">Khách Hàng Nói Gì</h2>
        <div class="title-underline"></div>
        <p class="testimonials-subtitle">Hơn 10,000 khách hàng hài lòng</p>
      </div>

      <div class="testimonials-grid-alt">
        <!-- Card 1 -->
        <div class="testimonial-card-alt">
          <Quote :size="24" class="card-quote-icon" />
          <p class="testimonial-text-alt">
            Sản phẩm chất lượng, giao hàng nhanh, nhân viên tư vấn nhiệt tình. Sẽ tiếp tục ủng hộ shop!
          </p>
          <div class="testimonial-footer-alt">
            <img src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&h=120&q=80" alt="Nguyễn Văn A" class="author-avatar-img" />
            <div class="author-meta-block">
              <h4 class="author-name-alt">Nguyễn Văn A</h4>
              <span class="author-role-alt">Khách hàng thân thiết</span>
            </div>
            <div class="rating-stars-block">
              <Star v-for="i in 5" :key="i" :size="12" class="star-icon-filled" />
            </div>
          </div>
        </div>

        <!-- Card 2 -->
        <div class="testimonial-card-alt">
          <Quote :size="24" class="card-quote-icon" />
          <p class="testimonial-text-alt">
            Mua iPhone 15 Pro Max ở đây, giá tốt nhất thị trường. Máy zin 100%, đóng gói cẩn thận!
          </p>
          <div class="testimonial-footer-alt">
            <img src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&h=120&q=80" alt="Trần Thị B" class="author-avatar-img" />
            <div class="author-meta-block">
              <h4 class="author-name-alt">Trần Thị B</h4>
              <span class="author-role-alt">Khách hàng mới</span>
            </div>
            <div class="rating-stars-block">
              <Star v-for="i in 5" :key="i" :size="12" class="star-icon-filled" />
            </div>
          </div>
        </div>

        <!-- Card 3 -->
        <div class="testimonial-card-alt">
          <Quote :size="24" class="card-quote-icon" />
          <p class="testimonial-text-alt">
            Dịch vụ bảo hành tốt, đổi máy nhanh khi có vấn đề. Rất hài lòng với cửa hàng!
          </p>
          <div class="testimonial-footer-alt">
            <img src="https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&h=120&q=80" alt="Lê Văn C" class="author-avatar-img" />
            <div class="author-meta-block">
              <h4 class="author-name-alt">Lê Văn C</h4>
              <span class="author-role-alt">Khách hàng VIP</span>
            </div>
            <div class="rating-stars-block">
              <Star v-for="i in 5" :key="i" :size="12" class="star-icon-filled" />
            </div>
          </div>
        </div>
      </div>

    </div>
  </section>

  <!-- Redesigned Newsletter Section -->
  <section class="newsletter-section-alt">
    <div class="newsletter-container-alt">
      
      <!-- Envelope Badge -->
      <div class="envelope-badge-container">
        <div class="envelope-badge">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="envelope-svg-icon">
            <rect width="20" height="16" x="2" y="4" rx="2"/>
            <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/>
          </svg>
        </div>
        <!-- Ambient glow for envelope badge -->
        <div class="envelope-glow"></div>
      </div>

      <!-- Text & Form Content -->
      <div class="newsletter-content-block">
        <h2 class="newsletter-heading-alt">Đăng Ký Nhận Tin</h2>
        <p class="newsletter-sub-alt">Nhận thông báo về sản phẩm mới và ưu đãi đặc biệt</p>
        
        <form class="newsletter-form-alt">
          <div class="newsletter-input-wrapper">
            <Mail :size="18" class="newsletter-mail-icon" />
            <input type="email" class="newsletter-input-alt" placeholder="Nhập email của bạn" aria-label="Email">
          </div>
          <button type="submit" class="newsletter-submit-btn">Đăng Ký</button>
        </form>
      </div>

    </div>
  </section>

  <Footer/>
</template>

<style scoped>
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
  background: #f8f9fa;
  color: #1a1a2e;
  line-height: 1.6;
}

/* Redesigned Hero Section */
.hero-section {
  width: 100%;
  background: linear-gradient(135deg, #fbfbfe 0%, #f4effa 100%);
  padding: 80px 0 60px 0;
  position: relative;
  overflow: hidden;
}

.hero-container {
  max-width: 1400px;
  width: calc(100% - 60px);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 40px;
  align-items: center;
}

.hero-content-col {
  display: flex;
  flex-direction: column;
  z-index: 1;
}

.hero-subtitle-tag {
  color: #7c3aed;
  font-size: 0.78rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  margin-bottom: 20px;
  display: block;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.25;
  color: #111827;
  margin-bottom: 24px;
  letter-spacing: -0.01em;
}

.gradient-text {
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  font-size: 1.05rem;
  color: #4b5563;
  line-height: 1.7;
  max-width: 520px;
  margin-bottom: 40px;
}

/* CTA buttons */
.hero-cta-group {
  display: flex;
  gap: 16px;
  margin-bottom: 50px;
}

.btn-primary-hero {
  padding: 14px 28px;
  background: linear-gradient(135deg, #7c3aed 0%, #da458f 100%);
  color: white;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 10px 20px -8px rgba(124, 58, 237, 0.4);
  transition: all 0.3s ease;
}

.btn-primary-hero:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -6px rgba(124, 58, 237, 0.5);
}

.btn-secondary-hero {
  padding: 14px 28px;
  background: #ffffff;
  color: #374151;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
}

.btn-secondary-hero:hover {
  border-color: #d1d5db;
  background: #fafafa;
  transform: translateY(-2px);
}

.cta-btn-icon {
  stroke-width: 2.2px;
}

/* Trust Badges */
.hero-badges-row {
  display: flex;
  gap: 24px;
  border-top: 1.5px solid rgba(229, 231, 235, 0.5);
  padding-top: 36px;
}

.hero-badge-item {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.badge-icon-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #ede9fe;
  color: #7c3aed;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 8px rgba(124, 58, 237, 0.08);
}

.badge-text-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.badge-heading {
  font-size: 0.85rem;
  font-weight: 700;
  color: #1f2937;
}

.badge-sub {
  font-size: 0.75rem;
  color: #6b7280;
}

/* Right Column Visuals */
.hero-visual-col {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  height: 480px;
}

.hero-circle-backdrop {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.06) 0%, rgba(255, 255, 255, 0) 70%);
  z-index: 0;
  pointer-events: none;
}

.hero-promo-image {
  max-height: 480px;
  width: auto;
  object-fit: contain;
  z-index: 1;
  filter: drop-shadow(0 20px 30px rgba(124, 58, 237, 0.08));
  animation: heroFloat 5s ease-in-out infinite;
}

@keyframes heroFloat {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-8px) rotate(1deg);
  }
}

/* Container */
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 60px 30px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 3em;
  font-weight: 800;
  color: #1a1a2e;
  margin-bottom: 15px;
  position: relative;
  display: inline-block;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 5px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
}

.section-subtitle {
  font-size: 1.2em;
  color: #666;
  margin-top: 20px;
}

/* Products Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-top: 40px;
}

.product-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #f3f4f6;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.01);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.04), 0 10px 10px -5px rgba(0, 0, 0, 0.02);
  border-color: #e5e7eb;
}

/* Badges */
.badge-discount-top {
  position: absolute;
  top: 16px;
  left: 16px;
  background: #e11d48;
  color: #ffffff;
  padding: 4px 8px;
  font-size: 0.72rem;
  font-weight: 700;
  border-radius: 6px;
  z-index: 2;
  box-shadow: 0 2px 5px rgba(225, 29, 72, 0.25);
}

.badge-new-top {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #f0fdf4;
  border: 1.5px solid #10b981;
  color: #10b981;
  padding: 4px 8px;
  font-size: 0.72rem;
  font-weight: 700;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 2;
}

.new-icon {
  stroke-width: 2.5px;
}

/* Image Container */
.product-image-container {
  width: 100%;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  position: relative;
  background: #ffffff;
}

.product-img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  transition: transform 0.5s ease;
}

.product-card:hover .product-img {
  transform: scale(1.04);
}

/* Info Container */
.product-info-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.product-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 12px;
  line-height: 1.4;
  height: 2.8rem; /* Clamp to 2 lines */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Prices */
.price-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

/* Standard Inline Layout */
.price-row-standard {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.price-main {
  font-size: 0.95rem;
  font-weight: 800;
  color: #2563eb;
}

.price-original-strike {
  font-size: 0.8rem;
  color: #9ca3af;
  text-decoration: line-through;
  font-weight: 500;
}

/* Split Price Layout */
.price-row-split {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.price-row-split-large {
  font-size: 1.15rem;
  font-weight: 800;
  color: #2563eb;
  margin-bottom: 4px;
}

.price-row-split-sub {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.badge-discount-inline {
  background: #e11d48;
  color: #ffffff;
  padding: 1px 5px;
  font-size: 0.7rem;
  font-weight: 700;
  border-radius: 4px;
  line-height: 1.2;
}

/* Redesigned Features Section */
.features-section-alt {
  width: 100%;
  background: linear-gradient(180deg, #f4effa 0%, #fcfbfe 100%);
  padding: 80px 0;
  overflow: hidden;
}

.features-container {
  max-width: 1400px;
  width: calc(100% - 60px);
  margin: 0 auto;
}

.features-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60px;
  text-align: center;
}

.features-tag {
  background: rgba(124, 58, 237, 0.08);
  color: #7c3aed;
  padding: 6px 14px;
  border-radius: 9999px;
  font-size: 0.72rem;
  font-weight: 700;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border: 1px solid rgba(124, 58, 237, 0.15);
}

.sparkles-icon {
  color: #da458f;
}

.features-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: #111827;
  margin-bottom: 12px;
}

.title-underline {
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #7c3aed 0%, #da458f 100%);
  border-radius: 4px;
}

.features-grid-alt {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card-alt {
  background: #ffffff;
  border-radius: 24px;
  border: 1px solid rgba(229, 231, 235, 0.5);
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.04);
}

.feature-card-alt:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px -10px rgba(124, 58, 237, 0.1);
  border-color: rgba(124, 58, 237, 0.2);
}

.feature-icon-badge {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #f5f3ff;
  border: 1px solid #ede9fe;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c3aed;
  margin-bottom: 24px;
  box-shadow: 0 8px 16px rgba(124, 58, 237, 0.06);
  transition: all 0.3s ease;
}

.feature-card-alt:hover .feature-icon-badge {
  background: #7c3aed;
  color: #ffffff;
  transform: scale(1.05);
  box-shadow: 0 10px 20px rgba(124, 58, 237, 0.2);
}

.badge-icon {
  stroke-width: 2.2px;
}

/* Custom shield with 12 positioning */
.badge-shield-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shield-number {
  position: absolute;
  font-size: 0.65rem;
  font-weight: 800;
  color: #7c3aed;
  top: 55%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: color 0.3s ease;
}

.feature-card-alt:hover .shield-number {
  color: #ffffff;
}

.feature-heading {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 12px;
}

.heading-line {
  width: 24px;
  height: 2.5px;
  background: #ede9fe;
  border-radius: 2px;
  margin-bottom: 18px;
  transition: all 0.3s ease;
}

.feature-card-alt:hover .heading-line {
  background: #7c3aed;
  width: 40px;
}

.feature-description {
  font-size: 0.88rem;
  color: #6b7280;
  line-height: 1.6;
}

/* Testimonials Section Redesign */
.testimonials-section-alt {
  width: 100%;
  background: #ffffff;
  padding: 80px 0;
  overflow: hidden;
}

.testimonials-container {
  max-width: 1400px;
  width: calc(100% - 60px);
  margin: 0 auto;
}

.testimonials-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60px;
  text-align: center;
}

.quote-header-icon {
  color: #c084fc;
  margin-bottom: 12px;
  opacity: 0.8;
}

.testimonials-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: #111827;
  margin-bottom: 12px;
}

.testimonials-subtitle {
  font-size: 1.05rem;
  color: #6b7280;
  margin-top: 16px;
}

.testimonials-grid-alt {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.testimonial-card-alt {
  background: #fdfaf6;
  border-radius: 24px;
  padding: 36px;
  border: 1px solid rgba(229, 231, 235, 0.4);
  box-shadow: 0 10px 25px -15px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.testimonial-card-alt:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 30px -10px rgba(0, 0, 0, 0.08);
}

.card-quote-icon {
  color: #c084fc;
  margin-bottom: 20px;
  opacity: 0.6;
}

.testimonial-text-alt {
  font-size: 0.95rem;
  color: #374151;
  line-height: 1.7;
  margin-bottom: 30px;
  flex: 1;
}

.testimonial-footer-alt {
  display: flex;
  align-items: center;
  gap: 12px;
  border-top: 1px solid rgba(229, 231, 235, 0.5);
  padding-top: 20px;
}

.author-avatar-img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

.author-meta-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.author-name-alt {
  font-size: 0.88rem;
  font-weight: 700;
  color: #1f2937;
}

.author-role-alt {
  font-size: 0.75rem;
  color: #6b7280;
}

.rating-stars-block {
  display: flex;
  gap: 2px;
}

.star-icon-filled {
  fill: #f59e0b;
  color: #f59e0b;
}

/* Newsletter Section Redesign */
.newsletter-section-alt {
  width: 100%;
  background: #ffffff;
  padding: 40px 0 80px 0;
}

.newsletter-container-alt {
  max-width: 1340px;
  width: calc(100% - 60px);
  margin: 0 auto;
  background: linear-gradient(135deg, #e9e3fd 0%, #f6f0fd 100%);
  border: 1px solid rgba(124, 58, 237, 0.12);
  border-radius: 32px;
  padding: 45px 60px;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 48px;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* Diagonal layout lines in background */
.newsletter-container-alt::after {
  content: "";
  position: absolute;
  top: -50%;
  right: -20%;
  width: 60%;
  height: 200%;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.03) 0%, rgba(255, 255, 255, 0) 100%);
  transform: rotate(30deg);
  pointer-events: none;
}

.envelope-badge-container {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.envelope-badge {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c3aed;
  box-shadow: 0 10px 25px rgba(124, 58, 237, 0.15);
  border: 1px solid rgba(124, 58, 237, 0.08);
  z-index: 2;
}

.envelope-svg-icon {
  stroke-width: 1.8px;
}

.envelope-glow {
  position: absolute;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.25) 0%, rgba(255, 255, 255, 0) 70%);
  z-index: 1;
}

.newsletter-content-block {
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.newsletter-heading-alt {
  font-size: 1.75rem;
  font-weight: 800;
  color: #1e1b4b;
  margin-bottom: 8px;
}

.newsletter-sub-alt {
  font-size: 0.95rem;
  color: #6b7280;
  margin-bottom: 24px;
}

.newsletter-form-alt {
  display: flex;
  align-items: center;
  gap: 16px;
  max-width: 600px;
  width: 100%;
}

.newsletter-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  height: 48px;
  box-sizing: border-box;
}

.newsletter-mail-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
  z-index: 2;
}

.newsletter-input-alt {
  width: 100%;
  height: 100%;
  padding: 0 16px 0 48px;
  border: 1.5px solid rgba(229, 231, 235, 0.8);
  border-radius: 9999px;
  font-size: 0.95rem;
  outline: none;
  background: #ffffff;
  color: #1f2937;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.01);
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.newsletter-input-alt:focus {
  border-color: #7c3aed;
  box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.08), inset 0 2px 4px rgba(0,0,0,0.01);
}

.newsletter-submit-btn {
  height: 48px;
  padding: 0 36px;
  background: #7c3aed;
  color: #ffffff;
  border: none;
  border-radius: 9999px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  box-shadow: 0 10px 20px -8px rgba(124, 58, 237, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  margin: 0;
}

.newsletter-submit-btn:hover {
  background: #6d28d9;
  transform: translateY(-1px);
  box-shadow: 0 12px 24px -6px rgba(124, 58, 237, 0.5);
}

.newsletter-submit-btn:active {
  transform: translateY(1px);
}

/* Responsiveness */
@media (max-width: 1024px) {
  .testimonials-grid-alt {
    grid-template-columns: 1fr;
    gap: 24px;
    max-width: 480px;
    margin: 0 auto;
  }
  
  .newsletter-container-alt {
    grid-template-columns: 1fr;
    gap: 30px;
    padding: 40px 30px;
    text-align: center;
  }
  
  .envelope-badge-container {
    margin-bottom: 10px;
  }
  
  .newsletter-content-block {
    align-items: center;
  }
  
  .newsletter-form-alt {
    flex-direction: column;
    max-width: 420px;
    width: 100%;
    align-items: stretch;
  }
  
  .newsletter-input-wrapper {
    width: 100%;
    height: 48px;
  }
  
  .newsletter-input-alt {
    border-radius: 14px;
    height: 100%;
  }
  
  .newsletter-submit-btn {
    width: 100%;
    height: 48px;
    border-radius: 14px;
  }
}

@media (max-width: 640px) {
  .testimonials-title {
    font-size: 1.8rem;
  }
  .newsletter-heading-alt {
    font-size: 1.5rem;
  }
  .newsletter-sub-alt {
    font-size: 0.88rem;
  }
}
</style>
