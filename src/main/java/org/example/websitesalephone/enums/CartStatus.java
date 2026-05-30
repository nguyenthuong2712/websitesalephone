package org.example.websitesalephone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CartStatus {

    ACTIVE("ACTIVE", "Đang hoạt động", "🛒"),
    CHECKED_OUT("CHECKED_OUT", "Đã thanh toán", "💳"),
    CANCELLED("CANCELLED", "Đã hủy", "❌");

    private final String code;
    private final String description;
    private final String icon;
}
