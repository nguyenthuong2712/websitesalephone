<script setup lang="ts">
import HomeLayout from "../../layout/Header.vue";
import Footer from "../../layout/Footer.vue";
import {productService} from "@/service/ProductService.ts";
import {onMounted, ref} from "vue";
import {formatCurrency} from "../../utils/Constant.ts";
const products = ref<any[]>([]);

const loadProducts = async () => {
  try {
    const res = await productService.getAllNewProduct();
    products.value = res.data.data || [];
    console.log("📄 Fetched page:", page.value, "totalPages:", totalPages.value);
  } catch (error) {
    console.error("Lỗi khi load sản phẩm:", error);
  }
};
onMounted(loadProducts);

</script>

<template>
  <HomeLayout/>
  <section class="hero" id="home">
    <div class="hero-content">
      <h1>Điện Thoại Chính Hãng</h1>
      <p>Giá tốt nhất thị trường - Bảo hành chính hãng - Giao hàng miễn phí toàn quốc</p><button class="cta-button">Khám Phá Ngay</button>
    </div>
  </section><!-- Products Section -->
  <main id="products">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Sản Phẩm Mới Nhất</h2>
        <p class="section-subtitle">Những chiếc điện thoại cao cấp nhất hiện nay</p>
      </div>
      <div class="products-grid"><!-- Product 1 -->
        <article class="product-card" v-for="product in products">
          <span class="product-badge badge-hot">🔥 NEW</span>
          <div class="product-image">
            <div class="phone-emoji">
              <img :src="product.url" alt="" sizes="" srcset="">
            </div>
          </div>
          <div class="product-info">
            <span class="product-category">{{ product.originName }}</span>
            <h3 class="product-name">{{ product.productName }}</h3>
            <div class="product-footer">
              <div class="product-price">
                {{ formatCurrency(product.price | currency) }}
              </div>

            </div>
            <router-link :to="`detail-product/${product.id}`"
                         class="btn-add-cart">Xem chi tiết
            </router-link>
          </div>
        </article><!-- Product 2 -->
      </div>
    </div>
  </main><!-- Features Section -->
  <section class="features">
    <div class="section-header">
      <h2 class="section-title" style="color: #1a1a2e;">Tại Sao Chọn Chúng Tôi</h2>
    </div>
    <div class="features-grid">
      <div class="feature-item">
        <div class="feature-icon">
          ✅
        </div>
        <h3 class="feature-title">Chính Hãng 100%</h3>
        <p class="feature-desc">Cam kết sản phẩm chính hãng, có tem đầy đủ</p>
      </div>
      <div class="feature-item">
        <div class="feature-icon">
          🚚
        </div>
        <h3 class="feature-title">Giao Hàng Miễn Phí</h3>
        <p class="feature-desc">Miễn phí vận chuyển toàn quốc</p>
      </div>
      <div class="feature-item">
        <div class="feature-icon">
          🔧
        </div>
        <h3 class="feature-title">Bảo Hành 12 Tháng</h3>
        <p class="feature-desc">Bảo hành chính hãng tại trung tâm ủy quyền</p>
      </div>
      <div class="feature-item">
        <div class="feature-icon">
          💳
        </div>
        <h3 class="feature-title">Trả Góp 0%</h3>
        <p class="feature-desc">Hỗ trợ trả góp lãi suất 0% qua thẻ</p>
      </div>
    </div>
  </section><!-- Testimonials -->
  <section class="testimonials">
    <div class="section-header">
      <h2 class="section-title">Khách Hàng Nói Gì</h2>
      <p class="section-subtitle">Hơn 10,000 khách hàng hài lòng</p>
    </div>
    <div class="testimonials-grid">
      <div class="testimonial-card">
        <div class="testimonial-quote">
          "
        </div>
        <p class="testimonial-text">Sản phẩm chất lượng, giao hàng nhanh, nhân viên tư vấn nhiệt tình. Sẽ tiếp tục ủng hộ shop!</p>
        <div class="testimonial-author">
          <div class="author-avatar">
            👨
          </div>
          <div class="author-info">
            <div class="author-name">
              Nguyễn Văn A
            </div>
            <div class="author-title">
              Khách hàng thân thiết
            </div>
          </div>
          <div class="rating">
            ⭐⭐⭐⭐⭐
          </div>
        </div>
      </div>
      <div class="testimonial-card">
        <div class="testimonial-quote">
          "
        </div>
        <p class="testimonial-text">Mua iPhone 15 Pro Max ở đây, giá tốt nhất thị trường. Máy zin 100%, đóng gói cẩn thận!</p>
        <div class="testimonial-author">
          <div class="author-avatar">
            👩
          </div>
          <div class="author-info">
            <div class="author-name">
              Trần Thị B
            </div>
            <div class="author-title">
              Khách hàng mới
            </div>
          </div>
          <div class="rating">
            ⭐⭐⭐⭐⭐
          </div>
        </div>
      </div>
      <div class="testimonial-card">
        <div class="testimonial-quote">
          "
        </div>
        <p class="testimonial-text">Dịch vụ bảo hành tốt, đổi máy nhanh khi có vấn đề. Rất hài lòng với cửa hàng!</p>
        <div class="testimonial-author">
          <div class="author-avatar">
            👨
          </div>
          <div class="author-info">
            <div class="author-name">
              Lê Văn C
            </div>
            <div class="author-title">
              Khách hàng VIP
            </div>
          </div>
          <div class="rating">
            ⭐⭐⭐⭐⭐
          </div>
        </div>
      </div>
    </div>
  </section><!-- Newsletter -->
  <section class="newsletter">
    <div class="newsletter-content">
      <h2>Đăng Ký Nhận Tin</h2>
      <p>Nhận thông báo về sản phẩm mới và ưu đãi đặc biệt</p>
      <form class="newsletter-form"><input type="email" class="newsletter-input" placeholder="Nhập email của bạn" aria-label="Email"> <button type="submit" class="newsletter-btn">Đăng Ký</button>
      </form>
    </div>
  </section><!-- Footer -->
  <Footer/>
</template>

<style scoped>
body {
  box-sizing: border-box;
}
img {
  width: 250px;
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
  background: #f8f9fa;
  color: #1a1a2e;
  line-height: 1.6;
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
/* Hero Section */
.hero {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 100px 30px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.hero-content {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero h1 {
  font-size: 4em;
  font-weight: 900;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero p {
  font-size: 1.5em;
  margin-bottom: 40px;
  opacity: 0.95;
}

.cta-button {
  display: inline-block;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #1a1a2e;
  padding: 20px 50px;
  border-radius: 50px;
  font-size: 1.3em;
  font-weight: 700;
  text-decoration: none;
  box-shadow: 0 10px 30px rgba(250, 112, 154, 0.4);
  transition: all 0.3s ease;
  border: none;
  cursor: pointer;
}

.cta-button:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 15px 40px rgba(250, 112, 154, 0.6);
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
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 35px;
  margin-top: 40px;
}

.product-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  transition: all 0.4s ease;
  position: relative;
}

.product-card:hover {
  transform: translateY(-15px) rotateX(5deg);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.product-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 700;
  z-index: 10;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.badge-hot {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  color: white;
}

.product-image {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 50px;
  text-align: center;
  position: relative;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image::before {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(103, 126, 234, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.phone-emoji {
  font-size: 7em;
  position: relative;
  z-index: 1;
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
}

.product-info {
  padding: 30px;
}

.product-name {
  font-size: 1.4em;
  font-weight: 700;
  margin-bottom: 10px;
  color: #1a1a2e;
}

.product-desc {
  font-size: 0.95em;
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.product-price {
  font-size: 1.8em;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Features Section */
.features {
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  padding: 80px 30px;
  margin-top: 60px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.feature-item {
  text-align: center;
  padding: 40px 30px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 4em;
  margin-bottom: 20px;
}

.feature-title {
  font-size: 1.4em;
  font-weight: 700;
  margin-bottom: 10px;
  color: #1a1a2e;
}

.feature-desc {
  color: #666;
  font-size: 1em;
}

/* Testimonials */
.testimonials {
  padding: 80px 30px;
  background: white;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 35px;
  max-width: 1400px;
  margin: 0 auto;
  margin-top: 40px;
}

.testimonial-card {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  padding: 35px;
  border-radius: 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  position: relative;
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.testimonial-quote {
  font-size: 3em;
  color: rgba(26, 26, 46, 0.2);
  line-height: 0;
  margin-bottom: 20px;
}

.testimonial-text {
  font-size: 1.05em;
  line-height: 1.7;
  margin-bottom: 25px;
  color: #2c3e50;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 15px;
}

.author-avatar {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
}

.author-info {
  flex: 1;
}

.author-name {
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 3px;
}

.author-title {
  font-size: 0.9em;
  color: #666;
}

.rating {
  color: #ffd700;
  font-size: 1.1em;
}

/* Newsletter */
.newsletter {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 30px;
  color: white;
  text-align: center;
}

.newsletter-content {
  max-width: 700px;
  margin: 0 auto;
}

.newsletter h2 {
  font-size: 2.5em;
  font-weight: 800;
  margin-bottom: 15px;
}

.newsletter p {
  font-size: 1.2em;
  margin-bottom: 40px;
  opacity: 0.95;
}

.newsletter-form {
  display: flex;
  gap: 15px;
  max-width: 550px;
  margin: 0 auto;
}

.newsletter-input {
  flex: 1;
  padding: 18px 25px;
  border: none;
  border-radius: 50px;
  font-size: 1.05em;
  outline: none;
}

.newsletter-btn {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #1a1a2e;
  border: none;
  padding: 18px 40px;
  border-radius: 50px;
  font-weight: 700;
  font-size: 1.05em;
  cursor: pointer;
  transition: all 0.3s ease;
}

.newsletter-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(250, 112, 154, 0.4);
}

.footer-section h3 {
  font-size: 1.4em;
  margin-bottom: 20px;
  color: #fff;
}

.footer-section ul {
  list-style: none;
}

.footer-section ul li {
  margin-bottom: 12px;
}

.footer-section a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s ease;
}

.footer-section a:hover {
  color: white;
  padding-left: 5px;
}
/* Responsive */
@media (max-width: 968px) {
  .hero h1 {
    font-size: 2.5em;
  }

  .hero p {
    font-size: 1.2em;
  }

  .section-title {
    font-size: 2.2em;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 25px;
  }

  .newsletter-form {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .hero h1 {
    font-size: 2em;
  }

  .products-grid {
    grid-template-columns: 1fr;
  }
}
</style>