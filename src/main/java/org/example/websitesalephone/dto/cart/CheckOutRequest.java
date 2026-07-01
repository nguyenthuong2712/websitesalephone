package org.example.websitesalephone.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequest {

    private String addressLine;
    private String paymentMethod;
    private List<String> cartItemIds;

}
