package org.example.websitesalephone.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.example.websitesalephone.entity.Order;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.utils.Constants;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@FieldNameConstants
public class UserDto {

    private String id;

    private String loginId;

    private String fullName;

    private String fullNameKana;

    private String profileImg;

    private String email;

    private boolean emailLoginFlg;

    private String telNo;

    private String userCode;

    private String note;

    private String userId;

    private OffsetDateTime passwordExpiredAt;

    private String created;

    private boolean isDeleted;

    private String status;

    private String role;

    private String address;

    private String gender;

    private int orderCount;

    private BigDecimal totalSpent;

    public static UserDto fromEntity(User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .loginId(entity.getUsername())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .telNo(entity.getPhone())
                .note(entity.getDescription())
                .passwordExpiredAt(entity.getPasswordExpiredAt())
                .created(Constants.FORMATTER.format(entity.getCreatedAt()))
                .isDeleted(entity.isDeleted())
                .role(entity.getRole().getRoleEnums().getValue())
                .address(entity.getAddress())
                .gender(entity.getGender())
                .build();
    }

    public static UserDto fromEntitySearch(User user) {
        List<Order> orderList = user.getOrders();

        BigDecimal totalExpenditure = orderList.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UserDto.builder()
                .id(user.getId())
                .loginId(user.getUsername())
                .fullName(user.getFullName())
                .profileImg(user.getAvatar())
                .email(user.getEmail())
                .telNo(user.getPhone())
                .userCode(user.getCodeUser())
                .note(user.getDescription())
                .passwordExpiredAt(user.getPasswordExpiredAt())
                .created(Constants.FORMATTER.format(user.getCreatedAt()))
                .isDeleted(user.isDeleted())
                .address(user.getAddress())
                .gender(user.getGender())
                .orderCount(orderList.size())
                .totalSpent(totalExpenditure)
                .build();
    }

}
