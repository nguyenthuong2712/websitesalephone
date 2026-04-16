package org.example.websitesalephone.service.user.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.comon.PageResponse;
import org.example.websitesalephone.dto.user.ProfileUserRequest;
import org.example.websitesalephone.dto.user.UserDto;
import org.example.websitesalephone.entity.Cart;
import org.example.websitesalephone.entity.Role;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.RoleEnums;
import org.example.websitesalephone.repository.CartRepository;
import org.example.websitesalephone.repository.RoleRepository;
import org.example.websitesalephone.repository.UserRepository;
import org.example.websitesalephone.dto.user.CreateUserDto;
import org.example.websitesalephone.dto.user.UserSearchForm;
import org.example.websitesalephone.service.user.UserService;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.spe.UserSpecification;
import org.example.websitesalephone.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final CartRepository cartRepository;

    @Override
    public CommonResponse getUserByLoginId(String loginId) {
        User user = userRepository.findByUsernameAndIsDeleted(loginId, false).orElse(null);

        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(UserDto.fromEntity(user))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse createUser(CreateUserDto userDto) {
        User entity = CreateUserDto.toEntity(userDto);

        User userMailCheck = userRepository.findFirstByEmailAndIsDeleted(userDto.getEmail(), false).orElse(null);

        User userCheck = userRepository.findByUsernameAndIsDeleted(userDto.getLoginId(), false).orElse(null);

        if (userCheck != null && userMailCheck != null && Strings.isNotEmpty(userDto.getEmail())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_EMAIL_AND_ID_ALREADY_EXIST)
                    .message("Mail and id already exists")
                    .build();
        } else if (userMailCheck != null && Strings.isNotEmpty(userDto.getEmail())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_EMAIL_ALREADY_EXIST)
                    .message("Mail already exists")
                    .build();
        } else if (userCheck != null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_ALREADY_EXIST)
                    .message("User already exists")
                    .build();
        }

        Role userRole = roleRepository.findById(userDto.getRole()).orElse(null);
        if (userRole == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Role not found")
                    .build();
        }

        entity.setRole(userRole);
        if (userRole.getRoleEnums().getValue().equalsIgnoreCase(RoleEnums.STAFF.getValue())) {
            entity.setCodeUser(Utils.generateUniqueCode("STAFF-"));
        }
        if (userRole.getRoleEnums().getValue().equalsIgnoreCase(RoleEnums.CUSTOMER.getValue())) {
            entity.setCodeUser(Utils.generateUniqueCode("CUSTOMER-"));
        }
        entity.setId(UUID.randomUUID().toString());
        entity.setPasswordExpiredAt(OffsetDateTime.now().minusDays(1));
        userRepository.saveAndFlush(entity);

        Cart cart = new Cart();
        cart.setUser(entity);
        cart.setId(UUID.randomUUID().toString());
        cartRepository.saveAndFlush(cart);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse updateUser(CreateUserDto userDto) {
        User currentUser = userRepository.findByIdAndIsDeleted(userDto.getId(), false).orElse(null);
        if (currentUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        User checkLoginId = userRepository.findByUsernameAndIsDeleted(userDto.getLoginId(), false)
                .orElse(null);
        User userMailCheck = userRepository.findFirstByEmailAndIsDeleted(userDto.getEmail(), false).orElse(null);

        if (checkLoginId != null && userMailCheck != null
                && Strings.isNotEmpty(userDto.getEmail())
                && !checkLoginId.getId().equals(currentUser.getId())
                && !userMailCheck.getId().equals(currentUser.getId())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_EMAIL_AND_ID_ALREADY_EXIST)
                    .message("Mail and id already exists")
                    .build();
        } else if (userMailCheck != null && Strings.isNotEmpty(userDto.getEmail())
                && !userMailCheck.getId().equals(currentUser.getId())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_EMAIL_ALREADY_EXIST)
                    .message("Mail already exists")
                    .build();
        } else if (checkLoginId != null && !checkLoginId.getId().equals(currentUser.getId())) {
            return CommonResponse
                    .builder()
                    .code(CommonResponse.CODE_ALREADY_EXIST)
                    .build();
        }

        String roleId = userDto.getRole();
        Role userRole = null;
        if (Strings.isNotEmpty(roleId)) {
            userRole = roleRepository.findById(roleId).orElse(null);
            if (userRole == null) {
                return CommonResponse.builder()
                        .code(CommonResponse.CODE_NOT_FOUND)
                        .message("Role not found")
                        .build();
            }
        }

        currentUser.setRole(userRole);
        currentUser.setUsername(userDto.getLoginId());
        currentUser.setFullName(userDto.getFullName());
        currentUser.setPhone(userDto.getTelNo());
        currentUser.setGender(userDto.getGender());
        currentUser.setAddress(userDto.getAddress());
        currentUser.setDescription(userDto.getNote());
        currentUser.setEmail(userDto.getEmail());

        userRepository.saveAndFlush(currentUser);


        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(UserDto.fromEntity(currentUser))
                .build();
    }

    @Override
    public CommonResponse deleteUser(String userId) {
        User user = userRepository.findByIdAndIsDeleted(userId, false).orElse(null);
        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }
        user.setDeleted(true);
        userRepository.saveAndFlush(user);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(true)
                .build();
    }

    @Override
    public CommonResponse search(UserSearchForm searchForm) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetail userDetail = (UserDetail) auth.getPrincipal();

        User loginUser = userRepository
                .findByUsernameAndIsDeleted(userDetail.getLoginId(), false)
                .orElse(null);

        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        // xử lý searchText
        if (Strings.isNotEmpty(searchForm.getSearchText())) {
            searchForm.setSearchText("%" + searchForm.getSearchText() + "%");
        } else {
            searchForm.setSearchText(null);
        }

        PageRequest pageRequest = Utils.getPaging(searchForm);

        Page<UserDto> result = userRepository
                .findAll(
                        UserSpecification.search(searchForm, loginUser),
                        pageRequest
                )
                .map(UserDto::fromEntitySearch);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(PageResponse.from(result))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse updateProfileUser(ProfileUserRequest profileUserRequest, String id) {
        User loginUser = userRepository.findByIdAndIsDeleted(id, false).orElse(null);

        if (loginUser == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        loginUser.setPhone(profileUserRequest.getTelNo());
        loginUser.setAddress(profileUserRequest.getAddress());
        loginUser.setGender(profileUserRequest.getGender());
        loginUser.setEmail(profileUserRequest.getEmail());
        loginUser.setFullName(profileUserRequest.getFullName());

        userRepository.saveAndFlush(loginUser);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    public CommonResponse getUserById(String id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(UserDto.fromEntity(user))
                .build();
    }
}
