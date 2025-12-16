package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.user.CreateUserDto;
import org.example.websitesalephone.dto.user.ProfileUserRequest;
import org.example.websitesalephone.dto.user.UserSearchForm;
import org.example.websitesalephone.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get/user-detail")
    public ResponseEntity<CommonResponse> getUserDetail() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetail userDetail = (UserDetail) auth.getPrincipal();
            return ResponseEntity.ok(userService.getUserByLoginId(userDetail.getLoginId()));
        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/get/user/{id}")
    public ResponseEntity<CommonResponse> getUser(@PathVariable String id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("create")
    public CommonResponse createUser(@RequestBody CreateUserDto createUserDto) {
        return userService.createUser(createUserDto);
    }

    @PutMapping("update")
    public CommonResponse updateUser(@RequestBody CreateUserDto createUserDto) {
        return userService.updateUser(createUserDto);
    }

    @PutMapping("delete/{userId}")
    public CommonResponse deleteUser(@PathVariable(name = "userId") String userId) {
        return userService.deleteUser(userId);
    }

    @PostMapping("search")
    public CommonResponse search(@RequestBody UserSearchForm searchForm) {
        return userService.search(searchForm);
    }

    @PutMapping("update-profile-user")
    public ResponseEntity<CommonResponse> updateProfileUser(@RequestBody ProfileUserRequest profileUserRequest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserDetail userDetail = (UserDetail) auth.getPrincipal();
            return ResponseEntity.ok(userService.updateProfileUser(profileUserRequest, userDetail.getUserId()));
        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.builder()
                    .code(CommonResponse.CODE_INTERNAL_ERROR)
                    .message(e.getMessage())
                    .build());
        }
    }
}
