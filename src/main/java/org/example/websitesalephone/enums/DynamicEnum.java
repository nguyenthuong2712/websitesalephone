package org.example.websitesalephone.enums;

public enum DynamicEnum {

    COLOR("COLOR"),

    OPS("OPS"),

    SCREEN("SCREEN"),

    STORAGE("STORAGE"),

    RAM("RAM"),

    SUPPLIER("SUPPLIER"),

    SPU("CPU"),

    ORIGIN("ORIGIN"),

    BATTERY("BATTERY"),

    CAMERA("CAMERA");

    private final String value;

    DynamicEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
