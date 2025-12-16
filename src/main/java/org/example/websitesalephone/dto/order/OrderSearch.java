package org.example.websitesalephone.dto.order;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.websitesalephone.comon.PagingRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderSearch extends PagingRequest {

    private String searchText;

    private String status;
}
