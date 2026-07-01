package org.example.websitesalephone.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.websitesalephone.comon.PagingRequest;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductSearch extends PagingRequest {

    private String searchText;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String ramId;
    private String cameraId;
    private String originId;
    private String shopId;

}
