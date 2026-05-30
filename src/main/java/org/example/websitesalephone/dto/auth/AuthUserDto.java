package org.example.websitesalephone.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

public class AuthUserDto {

    @Getter
    private final String loginId;

    private final String password;

    public AuthUserDto(@JsonProperty("loginId") String loginId, @JsonProperty("password") String password) {
        this.loginId = loginId == null ? "" : loginId.trim();
        this.password = password == null ? "" : password.trim();
    }

    public Optional<String> getPasswordLogin() {
        return Optional.ofNullable(password).filter(p -> !p.isBlank());
    }

    public UsernamePasswordAuthenticationToken toAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken(loginId, password);
    }

}
