package org.example.websitesalephone.dto.user;


import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.websitesalephone.comon.PagingRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserSearchForm extends PagingRequest {

    private String role;

    private String searchText;

    private String checkLoginId;
}

