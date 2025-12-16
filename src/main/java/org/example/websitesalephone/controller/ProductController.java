package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.dynamic.CreateCartRequest;
import org.example.websitesalephone.dto.product.ProductDetailRequest;
import org.example.websitesalephone.dto.product.ProductImageRequest;
import org.example.websitesalephone.dto.product.ProductRequest;
import org.example.websitesalephone.dto.product.ProductSearch;
import org.example.websitesalephone.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("search")
    public CommonResponse getALl(@RequestBody ProductSearch productSearch) {
        return productService.getALl(productSearch);
    }

    @PostMapping("create-product-detail")
    public CommonResponse created(@RequestBody ProductDetailRequest productRequest) {
        return productService.createdProductDetail(productRequest);
    }

    @PostMapping("create-product")
    public CommonResponse created(@RequestBody ProductRequest productRequest) {
        return productService.createdProduct(productRequest);
    }

    @PutMapping("update")
    public CommonResponse update(@RequestBody ProductDetailRequest productRequest) {
        return productService.updated(productRequest);
    }

    @PutMapping("deleted/{id}")
    public CommonResponse deleted(@PathVariable(name = "id") String id) {
        return productService.deleted(id);
    }

    @PutMapping("deleted-product-detail/{id}")
    public CommonResponse deletedProductDetail(@PathVariable(name = "id") String id) {
        return productService.deletedProductDetail(id);
    }

    @PutMapping("deleted-image/{id}")
    public CommonResponse deletedImage(@PathVariable(name = "id") String id) {
        return productService.deletedImage(id);
    }

    @PostMapping("detail")
    public CommonResponse detail(@RequestBody ProductDetailRequest productDetailRequest) {
        return productService.detail(productDetailRequest);
    }

    @PostMapping("get-quantity")
    public CommonResponse getQuantity(@RequestBody CreateCartRequest createCartRequest) {
        return productService.getQuantity(createCartRequest);
    }

    @PostMapping("/create-image")
    public CommonResponse createImage(@RequestBody ProductImageRequest productImageRequest) {
        return productService.createImage(productImageRequest);
    }

    @GetMapping("/list/{productId}")
    public CommonResponse getAllImage(@PathVariable String productId) {
        return productService.getAllImage(productId);
    }

    @PutMapping("/update-image")
    public CommonResponse updateImage(@RequestBody ProductImageRequest productImageRequest) {
        return productService.updateImage(productImageRequest);
    }

    @GetMapping("/get-all-product-variant/{id}")
    public CommonResponse getAllProductVariant(@PathVariable String id) {
        return productService.getAllProductVariant(id);
    }

    @GetMapping("/get-product-variant-detail/{id}")
    public CommonResponse getProductVariantDetail(@PathVariable String id) {
        return productService.getProductVariantDetail(id);
    }

    @PostMapping("/new-product")
    public CommonResponse getAllNewProduct() {
        return productService.getAllNewProduct();
    }

}
