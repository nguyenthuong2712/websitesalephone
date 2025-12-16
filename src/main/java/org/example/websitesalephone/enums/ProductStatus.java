package org.example.websitesalephone.enums;

public enum ProductStatus {

    ACTIVE("ACTIVE", "Đang hoạt động"),
    INACTIVE("INACTIVE", "Không hoạt động"),
    OUT_OF_STOCK("OUT_OF_STOCK", "Hết hàng"),
    DISCONTINUED("DISCONTINUED", "Ngừng kinh doanh"),
    DRAFT("DRAFT", "Nháp");

    private final String code;
    private final String description;

    ProductStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
