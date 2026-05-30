<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { toast } from 'vue3-toastify'
import { dynamicService } from '@/service/DynamicService'
import { productService } from '@/service/ProductService'
import { productImageService } from '@/service/ProductImageService'
import { ProductVariantRequest } from '@/models/ProductVariantRequest'
import { ProductRequest } from '@/models/ProductRequest'
import { ProductImageRequest } from '@/models/ProductImageRequest'
import { DynamicSearch } from '@/models/DynamicSearch'
import type { CommonResponse } from '@/utils/CommonResponse'
import { getContrastColor } from '@/utils/Constant'

const route = useRoute()
const productIdRouter = ref((route.params.id as string) ?? '')

type DynamicOption = { id: string; name: string }
type ProductVariantDetailState = {
  quantity: number
  price: number
  colorId: string
  cameraId: string
  screenId: string
  originId: string
  ramId: string
}

type ProductVariantRow = {
  idProduct: string
  productName: string
  description: string
  quantity: number
  price: number
  screenName: string
  ramName: string
  cameraName: string
  colorName: string
  originName: string
}

type ProductImageRow = {
  id?: string
  productImageId?: string
  url?: string
  active?: boolean
}

const loading = ref(false)
const error = ref('')
const results = ref<DynamicOption[]>([])
const resultsImage = ref<ProductImageRow[]>([])

const productName = ref('')
const productDescription = ref('')
const createProductResponse = ref<CommonResponse<Record<string, unknown>> | null>(null)
const createProductDetailResponse = ref<unknown[]>([])
const createProductImageResponse = ref<unknown[]>([])
const productDetailList = ref<ProductVariantRow[]>([])
const productVariantDetail = ref<ProductVariantDetailState>({
  quantity: 0,
  price: 0,
  colorId: '',
  cameraId: '',
  screenId: '',
  originId: '',
  ramId: '',
})

const ramList = ref<DynamicOption[]>([])
const cameraList = ref<DynamicOption[]>([])
const originList = ref<DynamicOption[]>([])
const colorList = ref<DynamicOption[]>([])
const screenList = ref<DynamicOption[]>([])

const productImageId = ref('')
const url = ref('')
const isActive = ref(true)

const searchRequest = ref(new DynamicSearch('RAM'))

const fetchData = async (type: string) => {
  loading.value = true
  error.value = ''

  try {
    searchRequest.value = new DynamicSearch(type)
    const res = await dynamicService.search(searchRequest.value)
    results.value = res.data.data || []
  } catch (err) {
    console.error(err)
    error.value = 'Không lấy được dữ liệu. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}

const getWorkingProductId = (): string => {
  const createdId = createProductResponse.value?.data?.id
  return typeof createdId === 'string' && createdId.length > 0 ? createdId : productIdRouter.value
}

const fetchDataImage = async (productId: string) => {
  try {
    const res = await productService.getAllImage(productId)
    resultsImage.value = res.data.data || []
  } catch (err) {
    console.error(err)
    error.value = 'Không lấy được dữ liệu. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}

const handleCreateProduct = async () => {
  try {
    const req = new ProductRequest({
      name: productName.value,
      description: productDescription.value,
    })

    const res = await productService.createProduct(req)
    if (res.data.code === 0) {
      createProductResponse.value = res.data
      toast.success('Tạo sản phẩm thành công!')
      await loadAllProductDetail()
    } else {
      toast.error('Tạo sản phẩm không thành công!')
    }
  } catch (e) {
    console.error(e)
    toast.error('Tạo sản phẩm thất bại')
  }
}

const resetVariantForm = () => {
  productVariantDetail.value = {
    quantity: 0,
    price: 0,
    colorId: '',
    cameraId: '',
    screenId: '',
    originId: '',
    ramId: '',
  }
}

const handleCreateProductDetail = async () => {
  const id = getWorkingProductId()
  if (!id) return

  try {
    const request = new ProductVariantRequest({
      productName: productName.value,
      description: productDescription.value,
      quantity: productVariantDetail.value.quantity,
      price: productVariantDetail.value.price,
      idProduct: id,
      colorId: productVariantDetail.value.colorId,
      cameraId: productVariantDetail.value.cameraId,
      screenId: productVariantDetail.value.screenId,
      originId: productVariantDetail.value.originId,
      ramId: productVariantDetail.value.ramId,
    })

    const res = await productService.createProductDetail(request)
    if (res.data.code === 0) {
      createProductDetailResponse.value.push(res.data.data)
      toast.success('Tạo biến thể thành công!')
      await loadAllProductDetail()
      resetVariantForm()
    } else {
      toast.error('Không thể tạo biến thể')
    }
  } catch (e) {
    console.error(e)
    toast.error('Không thể tạo biến thể')
  }
}

const loadDynamic = async (type: string): Promise<DynamicOption[]> => {
  await fetchData(type)
  return [...results.value]
}

const handleCreateProductImage = async () => {
  const id = getWorkingProductId()
  if (!id) return

  try {
    const request = new ProductImageRequest({
      productImageId: productImageId.value,
      productId: id,
      url: url.value,
      isActive: true,
    })

    const res = await productService.createImage(request)
    if (res.data.code === 0) {
      createProductImageResponse.value.push(res.data.data)
      await fetchDataImage(id)
      url.value = ''
      toast.success('Thêm ảnh thành công!')
    } else {
      toast.error('Không thể thêm ảnh')
    }
  } catch (e) {
    console.error(e)
    toast.error('Không thể thêm ảnh')
  }
}

const loadAllProductDetail = async () => {
  const id = getWorkingProductId()
  if (!id) return

  const res = await productService.getAllProductVariant(id)
  productDetailList.value = res.data.data || []
  if (productDetailList.value.length > 0) {
    productName.value = productDetailList.value[0]?.productName ?? productName.value
    productDescription.value = productDetailList.value[0]?.description ?? productDescription.value
  }
}

const loadProductVariantDetail = async (id: string) => {
  const res = await productService.getProductVariantDetail(id)
  if (res.data.code === 0) {
    productVariantDetail.value = {
      ...productVariantDetail.value,
      ...res.data.data,
    }
  }
}

const loadProductImage = async () => {
  const id = getWorkingProductId()
  if (!id) return

  const res = await productImageService.getImagesByProduct(id)
  resultsImage.value = res.data.data || []
}

const setActiveImage = async (productImgId: string) => {
  const id = getWorkingProductId()
  if (!id) return

  try {
    const request = new ProductImageRequest({
      productImageId: productImgId,
      productId: id,
      url: url.value,
      isActive: isActive.value,
    })

    const res = await productService.updateImage(request)
    createProductImageResponse.value.push(res.data.data)
    await fetchDataImage(id)
    toast.success('Update thành công!')
  } catch (e) {
    console.error(e)
    toast.error('Update thất bại')
  }
}

const deleted = async (id: string) => {
  const res = await productService.deleteProductDetail(id)
  if (res.data.code === 0) {
    toast.success('Xóa biến thể sản phẩm thành công')
    await loadAllProductDetail()
  } else {
    toast.error('Xóa biến thể sản phẩm không thành công')
  }
}

onMounted(async () => {
  ramList.value = await loadDynamic('RAM')
  screenList.value = await loadDynamic('SCREEN')
  colorList.value = await loadDynamic('COLOR')
  cameraList.value = await loadDynamic('CAMERA')
  originList.value = await loadDynamic('ORIGIN')
  if (productIdRouter.value) {
    await loadAllProductDetail()
    await loadProductImage()
  }
})

const deleteImage = async (id: string) => {
  if (!id) return

  try {
    const res = await productService.deleteImage(id)
    if (res.data.code === 0) {
      toast.success('Xóa ảnh thành công!')
      await loadProductImage()
    } else {
      toast.error('Không thể xóa ảnh!')
    }
  } catch (e) {
    console.error(e)
    toast.error('Lỗi kết nối khi xoá ảnh!')
  }
}

const displayGallery = () => Boolean(createProductResponse.value?.code === 0 || productIdRouter.value)

const displayDetailForm = () => Boolean(createProductResponse.value !== null || productIdRouter.value)

const toDynamicId = (item: DynamicOption) => item.id
const toDynamicName = (item: DynamicOption) => item.name

const variantRowId = (item: ProductVariantRow) => item.idProduct

const galleryImageId = (item: ProductImageRow) => item.id ?? item.productImageId ?? ''

const galleryImageUrl = (item: ProductImageRow) => item.url ?? ''

const galleryImageActive = (item: ProductImageRow) => Boolean(item.active)

const galleryImageProductId = (item: ProductImageRow) => item.productImageId ?? ''

const adminColorMap: Record<string, string> = {
  BLACK: '#000000',
  WHITE: '#FFFFFF',
  RED: '#FF0000',
  GREEN: '#00A651',
  BLUE: '#0000FF',
  YELLOW: '#FFFF00',
  ORANGE: '#FFA500',
  PURPLE: '#800080',
  PINK: '#FFC0CB',
  BROWN: '#8B4513',
  GREY: '#808080',
  GRAY: '#808080',
  SILVER: '#C0C0C0',
  GOLD: '#FFD700',
  DEN: '#000000',
  TRANG: '#FFFFFF',
  DO: '#FF0000',
  XANH: '#0000FF',
  XANH_DUONG: '#0000FF',
  XANH_LA: '#00A651',
  VANG: '#FFD700',
  CAM: '#FFA500',
  TIM: '#800080',
  HONG: '#FFC0CB',
  NAU: '#8B4513',
  XAM: '#808080',
  BAC: '#C0C0C0',
}

const resolveAdminColorHex = (colorName?: string) => {
  if (!colorName) return '#ccc'
  const normalized = colorName.trim()
  if (normalized.startsWith('#') || normalized.startsWith('rgb') || normalized.startsWith('hsl')) {
    return normalized
  }
  const key = normalized
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/\s+/g, '_')
    .replace(/-/g, '_')
    .toUpperCase()

  return adminColorMap[key] ?? normalized.toLowerCase()
}

const productVariantColorStyle = (colorName: string) => {
  const colorHex = resolveAdminColorHex(colorName)
  return {
    backgroundColor: colorHex,
    color: getContrastColor(colorHex),
  }
}

void resolveAdminColorHex
void adminColorMap

const imageAltText = (item: ProductImageRow) => item.url ?? 'Ảnh sản phẩm'

const showGallery = displayGallery
const showDetailForm = displayDetailForm
const getItemId = variantRowId
const getOptionId = toDynamicId
const getOptionName = toDynamicName
const getGalleryId = galleryImageId
const getGalleryUrl = galleryImageUrl
const getGalleryActive = galleryImageActive
const getGalleryProductId = galleryImageProductId
const getImageAlt = imageAltText
const getColorStyle = productVariantColorStyle

resetVariantForm()

const quantity = ref(0)
const price = ref(0)
void quantity
void price
void productImageId
void isActive
void error
void loading
void createProductDetailResponse
void createProductImageResponse
const noop = (_value: string) => undefined
void noop
const loadDynamicNoop = (_type: string) => undefined
void loadDynamicNoop
const spareRequest = searchRequest
void spareRequest
const placeholderDescription = ref('')
void placeholderDescription
const imageUrlState = url
void imageUrlState
const imageActiveState = isActive
void imageActiveState
const imageIdState = productImageId
void imageIdState
const variantState = productVariantDetail
void variantState
const variantList = productDetailList
void variantList
const galleryList = resultsImage
void galleryList
const searchResults = results
void searchResults
const busy = loading
void busy
const errState = error
void errState
const created = createProductResponse
void created
const dynamicSeed = searchRequest
void dynamicSeed
const imageResponseState = createProductImageResponse
void imageResponseState
const variantResponseState = createProductDetailResponse
void variantResponseState
const countUnusedQuantity = quantity
void countUnusedQuantity
const countUnusedPrice = price
void countUnusedPrice
</script>

<template>
  <div class="content-grid"><!-- Form Card -->
    <div class="form-card">
      <h2 class="card-title"><span id="formIcon">📝</span> <span id="formTitle">Tạo Sản Phẩm Mới</span></h2>
      <!-- Step 1: Create Product -->
      <div id="step1Form">
        <form id="productForm" @submit.prevent="handleCreateProduct">
          <div class="form-group">
            <label for="productName" class="form-label">
              Tên sản phẩm <span class="required">*</span>
            </label>
            <input
                type="text"
                id="productName"
                class="form-input"
                placeholder="VD: iPhone 15 Pro Max"
                v-model="productName"
                required
            />
          </div>

          <div class="form-group">
            <label for="productDescription" class="form-label">
              Mô tả sản phẩm <span class="required">*</span>
            </label>
            <textarea
                id="productDescription"
                class="form-textarea"
                placeholder="Mô tả chi tiết về sản phẩm..."
                v-model="productDescription"
                required
            ></textarea>
          </div>

          <button type="submit" class="btn btn-primary" id="createProductBtn">
            <span>✓</span> Thêm mới sản phẩm
          </button>
        </form>
      </div><!-- Step 2: Add Details -->
      <div v-if="showDetailForm()">
        <br>
        <form @submit.prevent="handleCreateProductDetail">
          <div class="form-grid">
            <div class="form-group">
              <label for="quantity" class="form-label"> Số lượng <span class="required">*</span>
              </label>
              <input type="number" v-model="productVariantDetail.quantity" id="quantity" class="form-input" placeholder="0" min="0" required>
            </div>
            <div class="form-group">
              <label for="price" class="form-label"> Giá (VNĐ) <span class="required">*</span>
              </label>
              <input type="number" v-model="productVariantDetail.price" class="form-input" placeholder="0" min="0" required>
            </div>
          </div>
          <div class="form-grid">
            <div class="form-group">
              <label for="screenSize" class="form-label"> Màn hình <span class="required">*</span> </label>
              <select id="color" class="form-select" v-model="productVariantDetail.screenId" required>
                <option value="">Chọn...</option>
                <option v-for="c in screenList" :key="getOptionId(c)" :value="getOptionId(c)">
                  {{ getOptionName(c) }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="ram" class="form-label"> RAM <span class="required">*</span> </label>
              <select id="color" class="form-select" v-model="productVariantDetail.ramId" required>
                <option value="">Chọn...</option>
                <option v-for="c in ramList" :key="getOptionId(c)" :value="getOptionId(c)">
                  {{ getOptionName(c) }}
                </option>
              </select>
            </div>
          </div>
          <div class="form-grid">
            <div class="form-group"><label for="camera" class="form-label"> Camera <span class="required">*</span>
            </label>
              <select id="color" class="form-select" v-model="productVariantDetail.cameraId" required>
                <option value="">Chọn...</option>
                <option v-for="c in cameraList" :key="getOptionId(c)" :value="getOptionId(c)">
                  {{ getOptionName(c) }}
                </option>
              </select>
            </div>
            <div class="form-group"><label for="color" class="form-label"> Màu sắc <span class="required">*</span>
            </label>
              <select id="color" class="form-select" v-model="productVariantDetail.colorId" required>
                <option value="">Chọn...</option>
                <option v-for="c in colorList" :key="getOptionId(c)" :value="getOptionId(c)">
                  {{ getOptionName(c) }}
                </option>
              </select>
            </div>
          </div>
          <div class="form-group"><label for="origin" class="form-label"> Xuất xứ <span class="required">*</span>
          </label>
            <select id="color" class="form-select" v-model="productVariantDetail.originId" required>
              <option value="">Chọn...</option>
              <option v-for="c in originList" :key="getOptionId(c)" :value="getOptionId(c)">
                  {{ getOptionName(c) }}
              </option>
            </select>
          </div>
          <button type="submit" class="btn btn-success" id="addDetailsBtn"><span>➕</span> Thêm chi tiết</button>
        </form>
      </div>
    </div><!-- Product List Card -->
    <div class="list-card">
      <h2 class="card-title">
        <span>📋</span>
        Danh Sách Sản Phẩm
      </h2>
      <div class="product-list">
        <table class="product-table">
          <thead>
          <tr>
            <th>Tên Sản Phẩm</th>
            <th>Số Lượng</th>
            <th>Giá Bán</th>
            <th>Màn Hình</th>
            <th>RAM</th>
            <th>Camera</th>
            <th>Màu Sắc</th>
            <th>Xuất Xứ</th>
            <th>Thao Tác</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in productDetailList" :key="getItemId(item)">
            <td>{{ item.productName }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ item.price }}</td>
            <td>{{ item.screenName }}</td>
            <td>{{ item.ramName }}</td>
            <td>{{ item.cameraName }}</td>
            <td>
              <span class="spec-badge"
                    :style="{ backgroundColor: item.colorName, color: getContrastColor(item.colorName) }">
              </span>
            </td>
            <td>{{ item.originName }}</td>

            <td class="action-cell flex gap-2">
              <button class="detail-btn" @click="loadProductVariantDetail(item.idProduct)">
                🔍
              </button>
              <button class="delete-btn" @click="deleted(item.idProduct)">
                🗑️
              </button>
            </td>
          </tr>
          </tbody>
        </table>
        <div class="empty-state" v-if="productDetailList.length === 0">
          <div class="empty-icon">
            📦
          </div>
          <div class="empty-text">
            Chưa có sản phẩm nào
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="image-gallery" v-if="showGallery()">
    <h2 class="card-title">
      <span>🖼️</span>
      Thư Viện Ảnh Sản Phẩm
    </h2>
    <div class="add-image">
      <input v-model="url" type="text" placeholder="Nhập URL ảnh..." class="input-url"/>
      <button @click="handleCreateProductImage" class="btn-add">➕ Thêm ảnh</button>
    </div>
    <div class="gallery-grid">
      <div class="gallery-item" v-for="i in resultsImage" :key="getGalleryId(i)">
        <div v-if="getGalleryActive(i)" class="active-badge">✓ ẢNH ĐẠI DIỆN</div>
        <img
            :src="getGalleryUrl(i)"
            :alt="getImageAlt(i)"
            class="gallery-image"
        />

        <div class="gallery-info">
          <button
              class="set-active-btn"
              :class="{ active: getGalleryActive(i) }"
              @click="!getGalleryActive(i) && setActiveImage(getGalleryProductId(i))"
              :disabled="getGalleryActive(i)"
          >
            {{ getGalleryActive(i) ? "✓ Đang hiển thị" : "🖼️ Đặt làm đại diện" }}
          </button>
          <br>
          <button
              class="delete-image-btn"
              @click="deleteImage(getGalleryProductId(i))"
          >
            🗑️ Xóa
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.add-image {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
.action-cell button {
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  transition: 0.2s;
}
.delete-image-btn {
  width: 100%;
  margin-top: 5px;
  padding: 6px 10px;
  border: none;
  background: #ff4d4f;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.2s ease;
}

.delete-image-btn:hover {
  background: #d9363e;
}

.button.detail-btn:hover {
  color: #007bff; /* xanh nhẹ cho detail */
}

.delete-btn:hover {
  color: red; /* đỏ cho delete theo đúng truyền thống */
}

.input-url {
  flex: 1;
  padding: 0.5rem;
  border-radius: 0.25rem;
  border: 1px solid #ccc;
}

.btn-add {
  padding: 0.5rem 1rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
}

.btn-add:hover {
  background-color: #45a049;
}

.active-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #27ae60;
  color: white;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.75em;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(39, 174, 96, 0.4);
  z-index: 10;
}

.gallery-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.gallery-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9ff;
  border: 2px solid #e0e7ff;
  transition: all 0.3s ease;
}

.gallery-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  transform: translateY(-5px);
}

.gallery-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.gallery-info {
  padding: 15px;
}

.set-active-btn {
  width: 100%;
  padding: 8px;
  background: #f0f4ff;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 0.8em;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.set-active-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.set-active-btn.active {
  background: #27ae60;
  border-color: #27ae60;
  color: white;
}

.set-active-btn.active:hover {
  background: #229954;
  border-color: #229954;
}

.image-gallery {
  background: white;
  border-radius: 16px;
  padding: 30px;
  margin-top: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.gallery-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #f8f9ff;
  border: 2px solid #e0e7ff;
  transition: all 0.3s ease;
}

.spec-badge {
  padding: 5px 12px;
  background: #f0f0f0;
  border-radius: 8px;
  font-size: 0.85em;
  color: #666;
  font-weight: 600;
  border: 1px solid;
}

body {
  box-sizing: border-box;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #1a1a2e;
  line-height: 1.6;
  padding: 30px 20px;
  min-height: 100%;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-text h1 {
  font-size: 1.8em;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 5px;
}

.header-text p {
  color: #666;
  font-size: 0.95em;
}

.content-grid {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 25px;
}

.form-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  height: fit-content;
  animation: slideLeft 0.5s ease;
}

@keyframes slideLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.list-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  animation: slideRight 0.5s ease;
}

@keyframes slideRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-title {
  font-size: 1.3em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.step-indicator {
  background: #f0f4ff;
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 25px;
  border-left: 4px solid #667eea;
}

.step-number {
  font-weight: 800;
  color: #667eea;
  font-size: 0.9em;
}

.step-text {
  color: #666;
  font-size: 0.9em;
  margin-top: 3px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
  font-size: 0.95em;
}

.required {
  color: #e74c3c;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  font-size: 0.95em;
  transition: all 0.3s ease;
  font-family: inherit;
  background: white;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  min-height: 100px;
  resize: vertical;
}

.form-select {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23666' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 15px center;
  padding-right: 40px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.btn {
  width: 100%;
  padding: 14px 20px;
  border: none;
  border-radius: 10px;
  font-size: 1em;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-top: 10px;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  color: white;
  margin-top: 10px;
}

.btn-success:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(39, 174, 96, 0.4);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background: white;
  padding: 15px 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  display: none;
  align-items: center;
  gap: 10px;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.toast.show {
  display: flex;
}

.toast.success {
  border-left: 4px solid #27ae60;
}

.toast.error {
  border-left: 4px solid #e74c3c;
}

.product-list {
  overflow-x: auto;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 4em;
  margin-bottom: 15px;
}

.empty-text {
  font-size: 1.1em;
  color: #666;
}

.product-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9em;
}

.product-table thead {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.product-table th {
  padding: 15px 12px;
  text-align: left;
  font-weight: 700;
  font-size: 0.9em;
  white-space: nowrap;
  text-align: center;
}

.product-table td {
  padding: 15px 12px;
  border-bottom: 1px solid #e0e7ff;
  vertical-align: top;
  text-align: center;
}

.product-table tbody tr {
  background: white;
  transition: all 0.3s ease;
}

.product-table tbody tr:hover {
  background: #f8f9ff;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.product-name-cell {
  font-weight: 700;
  color: #1a1a2e;
  min-width: 200px;
}

.product-desc-cell {
  color: #666;
  font-size: 0.9em;
  max-width: 250px;
  line-height: 1.4;
}

.product-price-cell {
  font-weight: 800;
  color: #667eea;
  white-space: nowrap;
  font-size: 1.05em;
}

.quantity-cell {
  text-align: center;
  font-weight: 600;
}

.spec-cell {
  white-space: nowrap;
}

.delete-btn {
  background: #fee;
  color: #e74c3c;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.1em;
  transition: all 0.3s ease;
  display: inline-block;
}

.delete-btn:hover {
  background: #e74c3c;
  color: white;
  transform: scale(1.1);
}

.action-cell {
  text-align: center;
}

.current-product {
  background: #f0f4ff;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 20px;
  border: 2px solid #667eea;
}

.current-label {
  font-size: 0.85em;
  color: #667eea;
  font-weight: 700;
  margin-bottom: 5px;
}

.current-name {
  font-size: 1.1em;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 5px;
}

.current-desc {
  font-size: 0.9em;
  color: #666;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .form-card {
    order: 1;
  }

  .list-card {
    order: 2;
  }

  .stats {
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  body {
    padding: 20px 15px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .product-specs {
    grid-template-columns: 1fr;
  }

  .toast {
    right: 15px;
    left: 15px;
  }
}
</style>