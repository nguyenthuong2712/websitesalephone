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
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public CommonResponse getALl(ProductSearch productSearch) {

        PageRequest pageRequest = Utils.getPaging(productSearch);

        Specification<Product> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (Strings.isNotEmpty(productSearch.getSearchText())) {
                String searchText = "%" + productSearch.getSearchText().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchText));
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
        Product product = productRepository.findById(productImageRequest.getProductId()).orElse(null);

        if (product == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Product not found")
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
    public CommonResponse getAllNewProduct() {
        List<Product> products = productRepository.findTop8ByOrderByCreatedAtDesc();

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
