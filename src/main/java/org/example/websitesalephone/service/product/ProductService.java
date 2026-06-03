package org.example.websitesalephone.service.product;

import org.example.websitesalephone.dto.dynamic.CreateCartRequest;
import org.example.websitesalephone.dto.product.ProductDetailRequest;
import org.example.websitesalephone.dto.product.ProductImageRequest;
import org.example.websitesalephone.dto.product.ProductRequest;
import org.example.websitesalephone.dto.product.ProductSearch;
import org.example.websitesalephone.comon.CommonResponse;

public interface ProductService {

    CommonResponse getALl(ProductSearch productSearch);

    CommonResponse createdProduct(ProductRequest productRequest);

    CommonResponse createdProductDetail(ProductDetailRequest productRequest);

    CommonResponse updated(ProductDetailRequest productDetailRequest);

    CommonResponse detail(ProductDetailRequest productDetailRequest);

    CommonResponse deleted(String id);

    CommonResponse getQuantity(CreateCartRequest createCartRequest);

    CommonResponse createImage(ProductImageRequest productImageRequest);

    CommonResponse getAllImage(String productId);

    CommonResponse updateImage(ProductImageRequest productImageRequest);

    CommonResponse deletedProductDetail(String id);

    CommonResponse getAllProductVariant(String productId);

    CommonResponse getProductVariantDetail(String productVariantId);

    CommonResponse deletedImage(String idImage);

    CommonResponse getAllNewProduct();
}
