package org.example.websitesalephone.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.example.websitesalephone.auth.JwtService;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.dto.auth.*;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.RoleEnums;
import org.example.websitesalephone.enums.RsTokenStatus;
import org.example.websitesalephone.repository.*;
import org.example.websitesalephone.service.auth.AuthenticationService;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.utils.Constants;
import org.example.websitesalephone.utils.Utils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final TokenExpiredRepository tokenExpiredRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final CartRepository cartRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResponse authenticate(AuthUserDto request) throws Exception {
        // Tìm user theo username hoặc email
        List<User> users = userRepository.findByUsernameOrEmail(request.getLoginId(), request.getLoginId());
        if (users.isEmpty()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Bạn chưa có tài khoản")
                    .build();
        }

        Optional<User> activeUserOpt = users.stream().filter(u -> !u.isDeleted()).findFirst();
        if (activeUserOpt.isEmpty()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Bạn chưa có tài khoản")
                    .build();
        }

        User activeUser = activeUserOpt.get();

        String rawPassword = request.getPasswordLogin().get();
        String encodedPassword = activeUser.getPasswordHash();
        if (!BCrypt.checkpw(rawPassword, encodedPassword)) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_PASSWORD)
                    .message("Nhập sai password")
                    .build();
        }

        this.authenticationManager.authenticate(request.toAuthenticationToken());

        userRepository.save(activeUser);

        // Sinh token
        String token = jwtService.generateToken(UserDetail.fromEntity(activeUser));
        return CommonResponse.builder()
                .data(new AuthTokenResponse(token, activeUser.getRole().getRoleEnums().name()))
                .build();
    }

    @Override
    public CommonResponse setTokenExpired(String token) {
        ExpiredToken entity = new ExpiredToken();
        entity.setAccessToken(token);
        tokenExpiredRepository.save(entity);
        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .build();
    }

    @Override
    public CommonResponse forgotPassword(String email, String tabletOrPc) throws Exception {
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User email not found")
                    .build();
        }
        String userId = users.getFirst().getId();
        PasswordResetToken token = new PasswordResetToken();
        PasswordResetToken currentToken = passwordResetTokenRepository.findByUserId(userId).orElse(null);
        if (currentToken != null) {
            token = currentToken;
            token.setToken(RandomStringUtils.randomNumeric(6).toLowerCase(Locale.ROOT));
            token.setExpireTime(OffsetDateTime.now().plusHours(Constants.PASSWORD_RESET_MAIL_EXPIRED));
        } else {
            token.setToken(RandomStringUtils.randomNumeric(6).toLowerCase(Locale.ROOT));
            token.setExpireTime(OffsetDateTime.now().plusHours(Constants.PASSWORD_RESET_MAIL_EXPIRED));
            token.setUserId(userId);
        }

        token.setStatus(RsTokenStatus.WAITING);
        this.passwordResetTokenRepository.save(token);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(true)
                .build();
    }

    @Override
    public CommonResponse resetPassword(ResetPasswordRequest request) throws Exception {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null || user.isDeleted()) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("User email not found")
                    .build();
        }
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getResetToken())
                .orElse(null);
        if (token == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("token not found")
                    .build();
        }

        if (token.getExpireTime().isBefore(OffsetDateTime.now())) {
            return CommonResponse.builder()
                    .code(CommonResponse.TOKEN_IS_EXPIRED)
                    .message("Token expired")
                    .build();
        }

        if (!StringUtils.equals(request.getNewPassword(), request.getNewPasswordConfirm())) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_BUSINESS)
                    .message("Password not match")
                    .build();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpiredAt(OffsetDateTime.now().plusDays(Constants.PASSWORD_EXPIRE_DAYS));
        this.userRepository.saveAndFlush(user);

        token.setStatus(RsTokenStatus.USED);
        this.passwordResetTokenRepository.saveAndFlush(token);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(true)
                .build();
    }

    @Override
    public CommonResponse getPasswordTokenReset(String token) {
        PasswordResetToken pwToken = passwordResetTokenRepository.findByToken(token).orElse(null);
        if (pwToken == null) {
            return CommonResponse.builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .data(null)
                    .build();
        }

        if (pwToken.getExpireTime().isBefore(OffsetDateTime.now())) {
            pwToken.setStatus(RsTokenStatus.EXPIRED);
            passwordResetTokenRepository.save(pwToken);
        }
        ResetPasswordTokenDto resetPasswordTokenDto = ResetPasswordTokenDto.fromEntity(pwToken);
        userRepository.findById(resetPasswordTokenDto.getUserId()).ifPresent(user -> resetPasswordTokenDto.setUserLoginId(user.getUsername()));

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(resetPasswordTokenDto)
                .build();
    }

    @Override
    public CommonResponse register(RegisterRequest registerRequest) {
        List<User> user = userRepository.findByUsername(registerRequest.getUsername());
        Role role = roleRepository.findById(RoleEnums.CUSTOMER.getId()).orElse(null);
        if (!user.isEmpty()) {
            return CommonResponse
                    .builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Tài khoản đã tồn tại").build();
        }

        if (role == null) {
            return CommonResponse
                    .builder()
                    .code(CommonResponse.CODE_NOT_FOUND)
                    .message("Role not found").build();
        }

        User user1 = new User();

        user1.setId(UUID.randomUUID().toString());
        user1.setUsername(registerRequest.getUsername());
        user1.setPhone(registerRequest.getUsername());
        user1.setFullName(registerRequest.getFullName() == null ? null : registerRequest.getFullName().trim());
        user1.setEmail(registerRequest.getEmail());
        user1.setPasswordHash(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt()));
        user1.setPasswordExpiredAt(OffsetDateTime.now().plusDays(Constants.PASSWORD_EXPIRE_DAYS));
        user1.setRole(role);
        user1.setCodeUser(Utils.generateUniqueCode("USER"));

        userRepository.saveAndFlush(user1);

        Cart cart = new Cart();

        cart.setUser(user1);
        cart.setId(UUID.randomUUID().toString());
        cartRepository.saveAndFlush(cart);

        return CommonResponse
                .builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Registered Successfully").build();

    }
}
