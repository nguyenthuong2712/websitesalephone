package org.example.websitesalephone.service.product.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.example.websitesalephone.comon.PageResponse;
import org.example.websitesalephone.dto.dynamic.CreateCartRequest;
import org.example.websitesalephone.dto.product.*;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.ProductStatus;
import org.example.websitesalephone.enums.RoleEnums;
import org.example.websitesalephone.repository.*;
import org.example.websitesalephone.service.product.ProductService;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ColorRepository colorRepository;

    private final OriginRepository originRepository;

    private final ScreenRepository screenRepository;

    private final CameraRepository cameraRepository;

    private final RamRepository ramRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductRepository productRepository;

    private final ProductVariantRepository productVariantRepository;

    private final CartItemRepository cartItemRepository;

    private final InventoryRepository inventoryRepository;

    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;

    @Override
    public CommonResponse getALl(ProductSearch productSearch) {
        User loginUser = getCurrentUser();

        PageRequest pageRequest = Utils.getPaging(productSearch);

        Specification<Product> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (Strings.isNotEmpty(productSearch.getSearchText())) {
                String searchText = "%" + productSearch.getSearchText().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchText));
            }

            if (Strings.isNotEmpty(productSearch.getShopId())) {
                predicates.add(cb.equal(root.get("shopRegistration").get("id"), productSearch.getShopId()));
            }

            if (Strings.isNotEmpty(productSearch.getUserId())) {
                predicates.add(cb.equal(root.get("shopRegistration").get("user").get("id"), productSearch.getUserId()));
            }

            if (isStaff(loginUser)) {
                predicates.add(cb.equal(root.get("shopRegistration").get("user").get("id"), loginUser.getId()));
            }

            predicates.add(cb.isNotEmpty(root.get("variants")));

            Objects.requireNonNull(query).orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<Product> products = productRepository.findAll(spec, pageRequest);

        Page<ProductListResponse> result = products.map(ProductListResponse::fromEntity);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(PageResponse.from(result))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse createdProductDetail(ProductDetailRequest productRequest) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(productRequest.getIdProduct()).orElse(null);
        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        Color color = colorRepository.findById(productRequest.getColorId()).orElse(null);
        if (color == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Color not found")
                    .build();
        }

        Ram ram = ramRepository.findById(productRequest.getRamId()).orElse(null);
        if (ram == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("RAM not found")
                    .build();
        }

        Origin origin = originRepository.findById(productRequest.getOriginId()).orElse(null);
        if (origin == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Origin not found")
                    .build();
        }

        Camera camera = cameraRepository.findById(productRequest.getCameraId()).orElse(null);
        if (camera == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Camera not found")
                    .build();
        }

        Screen screen = screenRepository.findById(productRequest.getScreenId()).orElse(null);
        if (screen == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Screen not found")
                    .build();
        }

        Optional<ProductVariant> existOpt = productVariantRepository
                .findByProduct_IdAndOrigin_IdAndColor_IdAndRam_IdAndScreen_idAndCamera_id(
                        product.getId(),
                        origin.getId(),
                        color.getId(),
                        ram.getId(),
                        screen.getId(),
                        camera.getId()
                );

        ProductVariant variant;

        if (existOpt.isPresent()) {
            // 🔄 Update nếu trùng
            variant = existOpt.get();
            variant.setPrice(productRequest.getPrice());
            variant.setQuantity(productRequest.getQuantity());
        } else {
            // ➕ Create nếu không trùng
            variant = new ProductVariant();
            variant.setId(UUID.randomUUID().toString());
            variant.setProduct(product);
            variant.setColor(color);
            variant.setRam(ram);
            variant.setOrigin(origin);
            variant.setCamera(camera);
            variant.setScreen(screen);
            variant.setPrice(productRequest.getPrice());
            variant.setQuantity(productRequest.getQuantity());
            variant.setQuantityUnitSold(0);
        }

        productVariantRepository.saveAndFlush(variant);

        return CommonResponse.builder()
                .data(ProductVariantResponse.fromEntity(variant))
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse updated(ProductDetailRequest productDetailRequest) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(productDetailRequest.getIdProduct()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        product.setDescription(productDetailRequest.getDescription());
        product.setLocation(productDetailRequest.getLocation());
        product.setStorage(productDetailRequest.getStorage());
        product.setDeviceMake(productDetailRequest.getDeviceMake());
        product.setName(productDetailRequest.getProductName());
        product.setStatus(ProductStatus.valueOf(productDetailRequest.getStatus()));
        productRepository.saveAndFlush(product);

        return CommonResponse.builder()
                .data(ProductVariantResponse.fromProduct(product))
                .code(CommonResponse.CODE_SUCCESS)
                .build();

    }

    @Override
    public CommonResponse detail(ProductDetailRequest productDetailRequest) {
        Product product = productRepository.findById(productDetailRequest.getIdProduct()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        List<ProductVariant> productVariants = productVariantRepository.findByProduct_Id(product.getId());

        if (productVariants == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product variant is not found")
                    .build();
        }

        List<ProductImage> productImages = productImageRepository.findByProduct_idAndIsDeleted(product.getId(), false);

        if (productImages == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("ProductImage variant is not found")
                    .build();
        }

        ProductDetailResponse productDetailResponse = ProductDetailResponse.fromEntity(product, productVariants, productImages);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(productDetailResponse)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse deleted(String id) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        product.setDeleted(true);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.saveAndFlush(product);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    public CommonResponse getQuantity(CreateCartRequest createCartRequest) {
        ProductVariant productVariant = productVariantRepository
                .findByProduct_IdAndOrigin_IdAndColor_IdAndRam_IdAndScreen_idAndCamera_id(
                        createCartRequest.getIdProduct(),
                        createCartRequest.getIdOrigin(),
                        createCartRequest.getIdColor(),
                        createCartRequest.getIdRam(),
                        createCartRequest.getScreenId(),
                        createCartRequest.getCameraId()
                ).orElse(null);

        if (productVariant == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product variant not found")
                    .build();
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(ProductVariantResponse.from(productVariant))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse createImage(ProductImageRequest productImageRequest) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(productImageRequest.getProductId()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }
        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        ProductImage productImage = new ProductImage();
        productImage.setId(UUID.randomUUID().toString());
        productImage.setActive(false);
        productImage.setUrl(productImageRequest.getUrl());
        productImage.setProduct(product);
        productImageRepository.saveAndFlush(productImage);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(ProductImageResponse.fromEntity(productImage))
                .build();
    }

    @Override
    public CommonResponse getAllImage(String productId) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }
        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        List<ProductImage> images = productImageRepository.findByProduct_idAndIsDeleted(productId, false);

        List<ProductImageResponse> responseList = images
                .stream()
                .map(ProductImageResponse::fromEntity)
                .toList();

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(responseList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse updateImage(ProductImageRequest productImageRequest) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        ProductImage findByActive = productImageRepository.findByActiveAndId(true, productImageRequest.getProductImageId());
        if (findByActive == null) {
            System.out.println("hiện không có cái ảnh nào đang active");
        } else {
            findByActive.setActive(false);
            productImageRepository.saveAndFlush(findByActive);
        }

        ProductImage productImage = productImageRepository.findById(productImageRequest.getProductImageId())
                .orElse(null);

        if (productImage == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Image not found")
                    .build();
        }
        if (!hasPermissionForProduct(productImage.getProduct())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }
        productImage.setActive(true);
        productImageRepository.saveAndFlush(productImage);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(ProductImageResponse.fromEntity(productImage))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse deletedProductDetail(String id) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        ProductVariant productVariant = productVariantRepository.findById(id).orElse(null);

        if (productVariant == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }
        if (!hasPermissionForProduct(productVariant.getProduct())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        productVariant.setDeleted(true);
        productVariantRepository.saveAndFlush(productVariant);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    public CommonResponse getAllProductVariant(String productId) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }
        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        List<ProductVariant> productVariants = productVariantRepository.findByProduct_Id(productId);

        if (productVariants.isEmpty()) {
            return CommonResponse.builder()
                    .data(new ArrayList<>())
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .build();
        }

        List<ProductVariantResponse> productVariantResponses = productVariants.stream().filter(p -> !p.isDeleted()).map(ProductVariantResponse::fromEntity).toList();

        return CommonResponse.builder()
                .data(productVariantResponses)
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    public CommonResponse getProductVariantDetail(String productVariantId) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        ProductVariant productVariant = productVariantRepository.findById(productVariantId).orElse(null);
        if (productVariant == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .build();
        }
        if (!hasPermissionForProduct(productVariant.getProduct())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        return CommonResponse.builder()
                .data(ProductVariantDetailResponse.from(productVariant))
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse deletedImage(String idImage) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        ProductImage productImage = productImageRepository.findById(idImage).orElse(null);

        if (productImage == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .build();
        }
        if (!hasPermissionForProduct(productImage.getProduct())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }
        productImage.setDeleted(true);
        productImageRepository.saveAndFlush(productImage);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(ProductImageResponse.fromEntity(productImage))
                .build();
    }

    @Override
    public CommonResponse getAllNewProduct() {
        List<Product> products = productRepository.findTop8ByIsDeletedFalseAndStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE);

        List<ProductListResponse> productListResponses = products.stream().map(ProductListResponse::fromEntity).toList();
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(productListResponses.isEmpty() ? new ArrayList<>() : productListResponses)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse createdProduct(ProductRequest productRequest) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = new Product();
        ShopRegistration shopRegistration = resolveShopRegistration(productRequest);
        User loginUser = getCurrentUser();
        if (isStaff(loginUser) && shopRegistration == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn cần đăng ký shop trước khi tạo sản phẩm")
                    .build();
        }
        product.setId(UUID.randomUUID().toString());
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setLocation(productRequest.getLocation());
        product.setStorage(productRequest.getStorage());
        product.setDeviceMake(productRequest.getDeviceMake());
        product.setStatus(ProductStatus.ACTIVE);
        product.setShopRegistration(shopRegistration);
        productRepository.saveAndFlush(product);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("id", product.getId());
        responseData.put("name", product.getName());
        responseData.put("description", product.getDescription());

        return CommonResponse
                .builder()
                .data(responseData)
                .code(CommonResponse.CODE_SUCCESS)
                .message("Tạo sản phẩm thành công")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse hardDelete(String id) {
        CommonResponse staffAccess = validateStaffAccess();
        if (staffAccess != null) {
            return staffAccess;
        }

        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        if (!hasPermissionForProduct(product)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Bạn không có quyền quản lý sản phẩm của shop khác")
                    .build();
        }

        List<String> variantIds = productVariantRepository.findIdsByProduct_Id(id);

        if (!variantIds.isEmpty() && orderItemRepository.existsByProductVariant_IdIn(variantIds)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Sản phẩm đã phát sinh đơn hàng, không thể xóa hẳn")
                    .build();
        }

        if (!variantIds.isEmpty()) {
            cartItemRepository.deleteByProductVariant_IdIn(variantIds);
            inventoryRepository.deleteByProductVariant_IdIn(variantIds);
            productVariantRepository.deleteAllByIdInBatch(variantIds);
        }

        productImageRepository.deleteByProduct_Id(id);
        productRepository.deleteById(id);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Đã xóa hẳn sản phẩm")
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByUsernameAndIsDeleted(auth.getName(), false).orElse(null);
    }

    private boolean hasPermissionForProduct(Product product) {
        User loginUser = getCurrentUser();
        if (loginUser == null || loginUser.getRole() == null) {
            return true;
        }

        if (isAdmin(loginUser)) {
            return true;
        }

        if (!isStaff(loginUser)) {
            return false;
        }

        return product.getShopRegistration() != null
                && product.getShopRegistration().getUser() != null
                && Objects.equals(product.getShopRegistration().getUser().getId(), loginUser.getId());
    }

    private ShopRegistration resolveShopRegistration(ProductRequest request) {
        User loginUser = getCurrentUser();

        if (isStaff(loginUser)) {
            if (Strings.isNotEmpty(request.getShopId())) {
                return shopRegistrationRepository.findByIdAndUser_Id(request.getShopId(), loginUser.getId()).orElse(null);
            }
            return shopRegistrationRepository.findFirstByUser_IdOrderByCreatedAtDesc(loginUser.getId()).orElse(null);
        }
        if (Strings.isNotEmpty(request.getShopId())) {
            return shopRegistrationRepository.findById(request.getShopId()).orElse(null);
        }
        if (Strings.isNotEmpty(request.getUserId())) {
            return shopRegistrationRepository.findFirstByUser_IdOrderByCreatedAtDesc(request.getUserId()).orElse(null);
        }
        if (loginUser != null) {
            return shopRegistrationRepository.findFirstByUser_IdOrderByCreatedAtDesc(loginUser.getId()).orElse(null);
        }
        return null;
    }


    private CommonResponse validateStaffAccess() {
        User loginUser = getCurrentUser();
        if (loginUser == null || loginUser.getRole() == null || (!isAdmin(loginUser) && !isStaff(loginUser))) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Chỉ tài khoản STAFF hoặc ADMIN mới được quản lý sản phẩm")
                    .build();
        }
        return null;
    }

    private boolean isStaff(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getRoleEnums() == RoleEnums.STAFF;
    }

    private boolean isAdmin(User user) {
        return user != null
                && user.getRole() != null
                && user.getRole().getRoleEnums() == RoleEnums.ADMIN;
    }
}
