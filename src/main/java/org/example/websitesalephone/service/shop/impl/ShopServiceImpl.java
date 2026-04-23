package org.example.websitesalephone.service.shop.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.shop.ShopPaymentMethodRequest;
import org.example.websitesalephone.dto.shop.ShopRegisterRequest;
import org.example.websitesalephone.entity.Role;
import org.example.websitesalephone.entity.ShopPaymentMethod;
import org.example.websitesalephone.entity.ShopRegistration;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.RoleEnums;
import org.example.websitesalephone.enums.ShopRegistrationStatus;
import org.example.websitesalephone.repository.RoleRepository;
import org.example.websitesalephone.repository.ShopRegistrationRepository;
import org.example.websitesalephone.repository.UserRepository;
import org.example.websitesalephone.service.shop.ShopService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private static final String SHOP_UPLOAD_FOLDER = "uploads/shop";

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShopRegistrationRepository shopRegistrationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse registerShop(String requestJson,
                                       MultipartFile avatarShop,
                                       MultipartFile bannerImage,
                                       MultipartFile cccdImage) {
        try {
            ShopRegisterRequest request = objectMapper.readValue(requestJson, ShopRegisterRequest.class);
            if (Strings.isBlank(request.getUsername()) || Strings.isBlank(request.getShopName())) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_BUSINESS)
                        .message("Username và tên shop là bắt buộc")
                        .build();
            }

            User user = userRepository.findByUsernameAndIsDeleted(request.getUsername(), false).orElse(null);
            if (user == null) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Không tìm thấy user")
                        .build();
            }

            Role staffRole = roleRepository.findById(RoleEnums.STAFF.getId()).orElse(null);
            if (staffRole == null) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Role STAFF chưa tồn tại")
                        .build();
            }

            ShopRegistration registration = new ShopRegistration();
            registration.setId(UUID.randomUUID().toString());
            registration.setUser(user);
            registration.setShopName(request.getShopName().trim());
            registration.setDescription(request.getDescription());
            registration.setStatus(ShopRegistrationStatus.PENDING);
            registration.setAvatarUrl(storeFile(avatarShop));
            registration.setBannerUrl(storeFile(bannerImage));
            registration.setCccdUrl(storeFile(cccdImage));

            List<ShopPaymentMethod> methods = new ArrayList<>();
            if (request.getPaymentMethods() != null) {
                for (ShopPaymentMethodRequest paymentMethodRequest : request.getPaymentMethods()) {
                    if (paymentMethodRequest == null || Strings.isBlank(paymentMethodRequest.getMethod())) {
                        continue;
                    }
                    ShopPaymentMethod paymentMethod = new ShopPaymentMethod();
                    paymentMethod.setId(UUID.randomUUID().toString());
                    paymentMethod.setShopRegistration(registration);
                    paymentMethod.setMethod(paymentMethodRequest.getMethod().trim());
                    paymentMethod.setQrCode(paymentMethodRequest.getQrCode());
                    paymentMethod.setNote(paymentMethodRequest.getNote());
                    methods.add(paymentMethod);
                }
            }
            registration.setPaymentMethods(methods);
            shopRegistrationRepository.saveAndFlush(registration);

            user.setRole(staffRole);
            userRepository.saveAndFlush(user);

            return CommonResponse.builder()
                    .code(CommonResponse.CODE_SUCCESS)
                    .message("Đăng ký shop thành công")
                    .data(registration.getId())
                    .build();
        } catch (Exception ex) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Đăng ký shop thất bại: " + ex.getMessage())
                    .build();
        }
    }

    private String storeFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Path folder = Paths.get(SHOP_UPLOAD_FOLDER);
        Files.createDirectories(folder);
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = folder.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }
}
