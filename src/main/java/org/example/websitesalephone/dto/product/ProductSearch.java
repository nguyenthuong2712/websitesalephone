package org.example.websitesalephone.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.websitesalephone.comon.PagingRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductSearch  extends PagingRequest {

    private String searchText;

}
