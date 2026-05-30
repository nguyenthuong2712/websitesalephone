package org.example.websitesalephone.service.dyanmic;

import org.example.websitesalephone.comon.CommonResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductImageService {

    CommonResponse createImages(List<MultipartFile> files, String productId) throws IOException;

    CommonResponse findByProductId(String id);

    CommonResponse removeImage(String id);

    CommonResponse updateImage(String idImage, String idProduct);
}
