package org.example.websitesalephone.service.dyanmic.impl;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.dto.product.ProductImageResponse;
import org.example.websitesalephone.entity.Product;
import org.example.websitesalephone.entity.ProductImage;
import org.example.websitesalephone.repository.ProductImageRepository;
import org.example.websitesalephone.repository.ProductRepository;
import org.example.websitesalephone.service.dyanmic.ProductImageService;
import org.example.websitesalephone.comon.CommonResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse createImages(List<MultipartFile> files, String productId) throws IOException {
        Optional<Product> optionalProduct = productRepository.findById(productId);

        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            List<ProductImage> findBySanPham = productImageRepository.findByProduct_idAndIsDeleted(productId, false);

            boolean hasDefaultImage = findBySanPham.stream().anyMatch(ProductImage::isActive);

            String folderName = "D:\\FE_DuAnTotNghiep\\assets\\ảnh giày";
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                ProductImage image = new ProductImage();
                image.setId(UUID.randomUUID().toString());
                image.setProduct(product);

                image.setActive(!hasDefaultImage && i == 0);

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), Paths.get(folderName, fileName), StandardCopyOption.REPLACE_EXISTING);
                image.setUrl(fileName);
                productImageRepository.saveAndFlush(image);
            }

            return CommonResponse
                    .builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .message("Thành công")
                    .build();
        } else {
            return CommonResponse
                    .builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Không tìm thấy sản phẩm")
                    .build();
        }
    }

    @Override
    public CommonResponse findByProductId(String id) {
        List<ProductImage> productImages = productImageRepository.findByProduct_idAndIsDeleted(id, false);
        return CommonResponse
                .builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(productImages.stream().map(ProductImageResponse::fromEntity).toList())
                .build();
    }

    @Override
    public CommonResponse removeImage(String id) {
        productImageRepository.deleteById(id);
        return CommonResponse
                .builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    public CommonResponse updateImage(String idImage, String idProduct) {
        List<ProductImage> imageList = productImageRepository.findByProduct_idAndIsDeleted(idProduct, false);
        for (ProductImage i : imageList) {
            if (i.isActive()) {
                i.setActive(false);
            }
            productImageRepository.save(i);
        }
        Optional<ProductImage> image = productImageRepository.findById(idImage);
        image.get().setActive(true);
        productImageRepository.save(image.get());
        return CommonResponse
                .builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Update thành công")
                .build();
    }
}
