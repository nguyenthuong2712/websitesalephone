package org.example.websitesalephone.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String fullName;

    private String username;

    private String password;

    private String email;

}