package org.example.websitesalephone.dto.order;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class OrderHistoryStatusResponse {

    private String status;

    private String createdAt;

}
