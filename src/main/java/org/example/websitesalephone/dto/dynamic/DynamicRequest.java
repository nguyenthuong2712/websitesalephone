package org.example.websitesalephone.dto.dynamic;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DynamicRequest {

    private String id;

    private String name;

    private String dynamicName;
}
