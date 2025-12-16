package org.example.websitesalephone.service.dyanmic;

import org.example.websitesalephone.dto.dynamic.DynamicRequest;
import org.example.websitesalephone.dto.dynamic.DynamicSearch;
import org.example.websitesalephone.comon.CommonResponse;

public interface DynamicAttributeProductService {

    CommonResponse getALl(DynamicSearch dynamicSearch);

    CommonResponse created(DynamicRequest dynamicRequest);

    CommonResponse updated(DynamicRequest dynamicRequest);

    CommonResponse deleted(DynamicRequest dynamicRequest);
}
