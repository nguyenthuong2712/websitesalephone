package org.example.websitesalephone.dto.user;

import lombok.Builder;
import lombok.Data;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.Gender;
import org.example.websitesalephone.utils.Constants;
import org.example.websitesalephone.utils.Utils;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class CreateUserDto {

    private String id;

    private String loginId;

    private String fullName;

    private String email;

    private String telNo;

    private String note;

    private String role;

    private String address;

    private String gender;

    public static User toEntity(CreateUserDto dto) {
        User user = new User();

        user.setId(UUID.randomUUID().toString());
        user.setUsername(dto.getLoginId());
        user.setFullName(dto.getFullName() == null ? null : dto.getFullName().trim());
        user.setEmail(dto.getEmail());
        user.setDescription(dto.getNote());
        user.setPasswordHash(BCrypt.hashpw("666666", BCrypt.gensalt()));
        user.setPasswordExpiredAt(OffsetDateTime.now().plusDays(Constants.PASSWORD_EXPIRE_DAYS));
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setPhone(dto.getTelNo());

        return user;
    }
}
