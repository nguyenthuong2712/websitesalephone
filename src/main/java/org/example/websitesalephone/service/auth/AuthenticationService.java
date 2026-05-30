package org.example.websitesalephone.service.auth;

import org.example.websitesalephone.dto.auth.AuthUserDto;
import org.example.websitesalephone.dto.auth.RegisterRequest;
import org.example.websitesalephone.dto.auth.ResetPasswordRequest;
import org.example.websitesalephone.comon.CommonResponse;

public interface AuthenticationService {

    CommonResponse authenticate(AuthUserDto request) throws Exception;

    CommonResponse setTokenExpired(String token);

    CommonResponse forgotPassword(String email, String tabletOrPc) throws Exception;

    CommonResponse resetPassword(ResetPasswordRequest request) throws Exception;

    CommonResponse getPasswordTokenReset(String token);

    CommonResponse register(RegisterRequest registerRequest);
}
