package org.example.websitesalephone.service.product.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.example.websitesalephone.comon.PageResponse;
import org.example.websitesalephone.dto.dynamic.CreateCartRequest;
import org.example.websitesalephone.dto.product.*;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.ProductStatus;
import org.example.websitesalephone.repository.*;
import org.example.websitesalephone.service.product.ProductService;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    @Transactional(readOnly = true)
    public CommonResponse getALl(ProductSearch productSearch) {

        // Determine sort parameters
        String sortByField = "createdAt";
        boolean sortDesc = true;

        if (productSearch.getSortBy() != null) {
            String sortBy = productSearch.getSortBy().trim();
            if ("newest".equalsIgnoreCase(sortBy)) {
                sortByField = "createdAt";
                sortDesc = true;
            } else if ("oldest".equalsIgnoreCase(sortBy)) {
                sortByField = "createdAt";
                sortDesc = false;
            } else if ("priceAsc".equalsIgnoreCase(sortBy)) {
                sortByField = "price";
                sortDesc = false;
            } else if ("priceDesc".equalsIgnoreCase(sortBy)) {
                sortByField = "price";
                sortDesc = true;
            } else if ("nameAsc".equalsIgnoreCase(sortBy)) {
                sortByField = "name";
                sortDesc = false;
            } else if ("nameDesc".equalsIgnoreCase(sortBy)) {
                sortByField = "name";
                sortDesc = true;
            } else {
                sortByField = productSearch.getSortBy();
                sortDesc = productSearch.isSortDesc();
            }
        }

        PageRequest pageRequest = PageRequest.of(
                productSearch.getPage() == null || productSearch.getPage() < 1 ? 0 : productSearch.getPage() - 1,
                productSearch.getSize() == null ? 10 : productSearch.getSize(),
                Sort.by(sortDesc ? Sort.Direction.DESC : Sort.Direction.ASC, sortByField)
        );

        Specification<Product> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (Strings.isNotEmpty(productSearch.getSearchText())) {
                String searchText = "%" + productSearch.getSearchText().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchText));
            }

            // Hãng sản xuất filter (shopId)
            if (Strings.isNotEmpty(productSearch.getShopId())) {
                predicates.add(cb.equal(root.get("shopRegistration").get("id"), productSearch.getShopId()));
            }

            predicates.add(cb.equal(root.get("isDeleted"), false));

            // Join variants if we have variant filters (ramId, cameraId, originId, minPrice, maxPrice)
            boolean hasVariantFilter = Strings.isNotEmpty(productSearch.getRamId())
                    || Strings.isNotEmpty(productSearch.getCameraId())
                    || Strings.isNotEmpty(productSearch.getOriginId());

            boolean hasPriceFilter = productSearch.getMinPrice() != null || productSearch.getMaxPrice() != null;

            if (hasVariantFilter || hasPriceFilter) {
                // If we only have price filters, use LEFT join to avoid excluding products without variants.
                // If we have variant filters (RAM, Camera, Origin), use INNER join because they are required attributes.
                jakarta.persistence.criteria.JoinType joinType = hasVariantFilter ? jakarta.persistence.criteria.JoinType.INNER : jakarta.persistence.criteria.JoinType.LEFT;
                jakarta.persistence.criteria.Join<Product, ProductVariant> variantJoin = root.join("variants", joinType);
                predicates.add(cb.equal(variantJoin.get("isDeleted"), false));

                if (Strings.isNotEmpty(productSearch.getRamId())) {
                    predicates.add(cb.equal(variantJoin.get("ram").get("id"), productSearch.getRamId()));
                }
                if (Strings.isNotEmpty(productSearch.getCameraId())) {
                    predicates.add(cb.equal(variantJoin.get("camera").get("id"), productSearch.getCameraId()));
                }
                if (Strings.isNotEmpty(productSearch.getOriginId())) {
                    predicates.add(cb.equal(variantJoin.get("origin").get("id"), productSearch.getOriginId()));
                }

                // Price range filter
                if (productSearch.getMinPrice() != null) {
                    predicates.add(cb.or(
                            cb.and(cb.isNotNull(root.get("price")), cb.greaterThanOrEqualTo(root.get("price"), productSearch.getMinPrice())),
                            cb.and(cb.or(cb.isNull(root.get("price")), cb.lessThanOrEqualTo(root.get("price"), BigDecimal.ZERO)),
                                    cb.greaterThanOrEqualTo(variantJoin.get("price"), productSearch.getMinPrice()))
                    ));
                }
                if (productSearch.getMaxPrice() != null) {
                    predicates.add(cb.or(
                            cb.and(cb.isNotNull(root.get("price")), cb.lessThanOrEqualTo(root.get("price"), productSearch.getMaxPrice())),
                            cb.and(cb.or(cb.isNull(root.get("price")), cb.lessThanOrEqualTo(root.get("price"), BigDecimal.ZERO)),
                                    cb.lessThanOrEqualTo(variantJoin.get("price"), productSearch.getMaxPrice()))
                    ));
                }
            }

            if (!query.getResultType().equals(Long.class)) {
                query.distinct(true);
            }

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
        Product product = productRepository.findById(productRequest.getIdProduct()).orElse(null);
        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
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
        Product product = productRepository.findById(productDetailRequest.getIdProduct()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        product.setDescription(productDetailRequest.getDescription());
        product.setName(productDetailRequest.getProductName());
        productRepository.saveAndFlush(product);

        Color color = colorRepository.findById(productDetailRequest.getColorId()).orElse(null);
        if (color == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Color not found")
                    .build();
        }

        Ram ram = ramRepository.findById(productDetailRequest.getRamId()).orElse(null);
        if (ram == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("RAM not found")
                    .build();
        }

        Origin origin = originRepository.findById(productDetailRequest.getOriginId()).orElse(null);
        if (origin == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Origin not found")
                    .build();
        }

        Camera camera = cameraRepository.findById(productDetailRequest.getCameraId()).orElse(null);
        if (camera == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Camera not found")
                    .build();
        }

        Screen screen = screenRepository.findById(productDetailRequest.getScreenId()).orElse(null);
        if (screen == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Screen not found")
                    .build();
        }
//
//        Battery battery = batteryRepository.findById(productDetailRequest.getBatteryId()).orElse(null);
//        if (battery == null) {
//            return CommonResponse.builder()
//                    .code(CommonResponse.CODE_NOT_FOUND)
//                    .message("Battery not found")
//                    .build();
//        }
//
//        Storage storage = storageRepository.findById(productDetailRequest.getStorageId()).orElse(null);
//        if (storage == null) {
//            return CommonResponse.builder()
//                    .code(CommonResponse.CODE_NOT_FOUND)
//                    .message("Storage not found")
//                    .build();
//        }
//
//        OperatingSystem operatingSystem = operatingSystemRepository.findById(productDetailRequest.getOperatorId()).orElse(null);
//        if (operatingSystem == null) {
//            return CommonResponse.builder()
//                    .code(CommonResponse.CODE_NOT_FOUND)
//                    .message("Operating System not found")
//                    .build();
//        }
//
//        Cpu cpu = cpuRepository.findById(productDetailRequest.getCpuId()).orElse(null);
//        if (cpu == null) {
//            return CommonResponse.builder()
//                    .code(CommonResponse.CODE_NOT_FOUND)
//                    .message("CPU not found")
//                    .build();
//        }

        ProductVariant productVariant = productVariantRepository.findById(productDetailRequest.getProductVariantId()).orElse(null);

        if (productVariant == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product variant is not found")
                    .build();
        }

        productVariant.setProduct(product);
//        productVariant.setBattery(battery);
        productVariant.setCamera(camera);
        productVariant.setColor(color);
//        productVariant.setCpu(cpu);
        productVariant.setScreen(screen);
//        productVariant.setOperatingSystem(operatingSystem);
        productVariant.setRam(ram);
        productVariant.setOrigin(origin);
//        productVariant.setStorage(storage);
        productVariant.setPrice(productDetailRequest.getPrice());
        productVariant.setQuantity(productDetailRequest.getQuantity());

        productVariantRepository.saveAndFlush(productVariant);

        return CommonResponse.builder()
                .data(ProductVariantResponse.fromEntity(productVariant))
                .code(CommonResponse.CODE_SUCCESS)
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse detail(ProductDetailRequest productDetailRequest) {
        Product product = productRepository.findById(productDetailRequest.getIdProduct()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
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
        Product product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        product.setDeleted(true);
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
        Product product = productRepository.findById(productImageRequest.getProductId()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        List<ProductImage> existingImages = productImageRepository.findByProduct_idAndIsDeleted(product.getId(), false);
        boolean hasActiveImage = existingImages.stream().anyMatch(ProductImage::isActive);
        boolean shouldActive = productImageRequest.isActive() || !hasActiveImage;

        if (shouldActive) {
            existingImages.stream().filter(ProductImage::isActive).forEach(i -> i.setActive(false));
            productImageRepository.saveAll(existingImages);
        }

        ProductImage productImage = new ProductImage();
        productImage.setId(UUID.randomUUID().toString());
        productImage.setActive(shouldActive);
        productImage.setUrl(productImageRequest.getUrl());
        productImage.setProduct(product);
        productImageRepository.saveAndFlush(productImage);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(ProductImageResponse.fromEntity(productImage))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse getAllImage(String productId) {
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
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
        ProductImage productImage = productImageRepository.findById(productImageRequest.getProductImageId()).orElse(null);

        if (productImage == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Image not found")
                    .build();
        }

        List<ProductImage> productImages = productImageRepository.findByProduct_idAndIsDeleted(productImage.getProduct().getId(), false);
        productImages.forEach(image -> image.setActive(false));
        productImageRepository.saveAll(productImages);

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
        ProductVariant productVariant = productVariantRepository.findById(id).orElse(null);

        if (productVariant == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
                    .build();
        }

        productVariant.setDeleted(true);
        productVariantRepository.saveAndFlush(productVariant);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse getAllProductVariant(String productId) {
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
    @Transactional(readOnly = true)
    public CommonResponse getProductVariantDetail(String productVariantId) {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId).orElse(null);
        if (productVariant == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
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
        ProductImage productImage = productImageRepository.findById(idImage).orElse(null);

        if (productImage == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
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
    @Transactional(readOnly = true)
    public CommonResponse getAllNewProduct() {
        List<Product> products = productRepository.findHomeProducts(ProductStatus.ACTIVE, PageRequest.of(0, 8));

        List<ProductListResponse> productListResponses = products.stream().map(ProductListResponse::fromEntity).toList();
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(productListResponses.isEmpty() ? new ArrayList<>() : productListResponses)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse createdProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
        return CommonResponse
                .builder()
                .data(product)
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }
}
