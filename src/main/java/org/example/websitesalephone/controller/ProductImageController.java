package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.service.dyanmic.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping("/upload")
    public CommonResponse uploadImages(@RequestParam("files") List<MultipartFile> files,
                                       @RequestParam("productId") String productId) throws IOException {
        return productImageService.createImages(files, productId);
    }

    @GetMapping("/{productId}")
    public CommonResponse getImagesByProduct(@PathVariable String productId) {
        return productImageService.findByProductId(productId);
    }

    @DeleteMapping("/{imageId}")
    public CommonResponse deleteImage(@PathVariable String imageId) {
        return productImageService.removeImage(imageId);
    }

    @PutMapping("/update")
    public CommonResponse updateImage(@RequestParam("imageId") String imageId,
                                      @RequestParam("productId") String productId) {
        return productImageService.updateImage(imageId, productId);
    }
}
