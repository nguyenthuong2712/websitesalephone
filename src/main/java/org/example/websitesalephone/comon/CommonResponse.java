package org.example.websitesalephone.comon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommonResponse {

    public static final int CODE_SUCCESS = 0;

    public static final int CODE_NOT_FOUND = 2;

    public static final int CODE_ALREADY_EXIST = 3;

    public static final int CODE_BUSINESS = 4;

    public static final int CODE_ACCOUNT_EXCEPTION = 5;

    public static final int CODE_LIKE_OLD_PASSWORD = 5;

    public static final int CODE_PASSWORD_EXPIRED = 6;

    public static final int CODE_EMAIL_ALREADY_EXIST = 7;

    public static final int CODE_EMAIL_AND_ID_ALREADY_EXIST = 8;

    public static final int CODE_OVERFLOW_TIRE_IN_MONTH = 9;

    public static final int CODE_DATA_IS_DELETED = 10;

    public static final int CODE_PASSWORD = 11;

    public static final int TOKEN_IS_EXPIRED = 12;

    public static final int CODE_INTERNAL_ERROR = 9999;

    @JsonProperty("code")
    @Builder.Default
    private Integer code = CODE_SUCCESS;

    @JsonProperty("message")
    @Builder.Default
    private String message = "";

    @JsonProperty("errorFileLog")
    @Builder.Default
    private String errorFileLog = "";

    @JsonProperty("data")
    @Builder.Default
    private Object data = null;

    private Integer totalPage;

    private Integer totalRecordsPickupDelivery;

}
