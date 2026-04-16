package org.example.websitesalephone.dto.order;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class OrderByUserRequest {

    private String id;

    private String searchText;

    private String status;

    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;


}
