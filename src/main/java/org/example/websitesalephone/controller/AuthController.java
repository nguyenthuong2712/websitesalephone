package org.example.websitesalephone.controller;

import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.auth.AuthUserDto;
import org.example.websitesalephone.dto.auth.RegisterRequest;
import org.example.websitesalephone.dto.auth.ResetPasswordRequest;
import org.example.websitesalephone.service.auth.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authService;

    @Autowired
    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public CommonResponse login(@RequestBody AuthUserDto request) throws Exception {
        return authService.authenticate(request);
    }

    @PostMapping("/logout")
    public CommonResponse logout(@RequestParam String token) {
        return authService.setTokenExpired(token);
    }

    @PostMapping("/forgot-password")
    public CommonResponse forgotPassword(@RequestParam String email,
                                         @RequestParam String tabletOrPc) throws Exception {
        return authService.forgotPassword(email, tabletOrPc);
    }

    @PostMapping("/reset-password")
    public CommonResponse resetPassword(@RequestBody ResetPasswordRequest request) throws Exception {
        return authService.resetPassword(request);
    }

    @GetMapping("/check-reset-token")
    public CommonResponse checkResetToken(@RequestParam String token) {
        return authService.getPasswordTokenReset(token);
    }

    @PostMapping("/register")
    public CommonResponse register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
}
