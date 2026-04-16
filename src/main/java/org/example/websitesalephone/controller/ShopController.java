package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.service.shop.ShopService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/register")
    public CommonResponse registerShop(@RequestParam("request") String requestJson,
                                       @RequestParam(value = "avatarShop", required = false) MultipartFile avatarShop,
                                       @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage,
                                       @RequestParam(value = "cccdImage", required = false) MultipartFile cccdImage) {
        return shopService.registerShop(requestJson, avatarShop, bannerImage, cccdImage);
    }
}
