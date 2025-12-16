package org.example.websitesalephone.comon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PagingRequest {
    Integer page;
    Integer size;
    int offset;
    int tempRecords;
    String filter;
    String sortBy;
    boolean sortDesc;
}
