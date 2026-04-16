package org.example.websitesalephone.dto.cart;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckOutRequest {

    private String addressLine;

}
