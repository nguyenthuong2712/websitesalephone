package org.example.websitesalephone.dto.user;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.example.websitesalephone.entity.Role;

@Data
@Builder
@FieldNameConstants
public class RoleDto {

    private Role role;
}
