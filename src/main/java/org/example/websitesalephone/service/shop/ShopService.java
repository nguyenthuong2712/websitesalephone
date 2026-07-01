package org.example.websitesalephone.service.shop;

import org.example.websitesalephone.comon.CommonResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ShopService {
    CommonResponse registerShop(String requestJson,
                                MultipartFile avatarShop,
                                MultipartFile bannerImage,
                                MultipartFile cccdImage);

    CommonResponse getActiveShops();
}
