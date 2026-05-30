package org.example.websitesalephone.service.user;

import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.user.CreateUserDto;
import org.example.websitesalephone.dto.user.ProfileUserRequest;
import org.example.websitesalephone.dto.user.UserSearchForm;

public interface UserService {

    CommonResponse getUserByLoginId(String loginId);

    CommonResponse createUser(CreateUserDto userDto);

    CommonResponse updateUser(CreateUserDto userDto);

    CommonResponse deleteUser(String userId);

    CommonResponse search(UserSearchForm searchForm);

    CommonResponse updateProfileUser(ProfileUserRequest profileUserRequest, String id);

    CommonResponse getUserById(String id);
}
