package org.example.websitesalephone.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

public class AuthUserDto {

    /** The username. */
    @Getter
    private final String loginId;

    /** The password. */
    private final String password;

    /**
     * Instantiates a new admin user detail.
     *
     * @param loginId the loginId
     * @param password the password
     */
    public AuthUserDto(@JsonProperty("loginId") String loginId, @JsonProperty("password") String password) {
        this.loginId = loginId.trim();
        this.password = password.trim();
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public Optional<String> getPassword() {
        return Optional.ofNullable(password).map(p -> new BCryptPasswordEncoder().encode(p));
    }

    public Optional<String> getPasswordLogin() {
        return Optional.ofNullable(password);
    }

    /**
     * To authentication token.
     *
     * @return the username password authentication token
     */
    public UsernamePasswordAuthenticationToken toAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken(loginId, password);
    }

}
