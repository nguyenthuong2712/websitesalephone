package org.example.websitesalephone.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING(0, "PENDING"),
    CONFIRMED(1, "CONFIRMED"),
    SHIPPING(2, "SHIPPING"),
    DELIVERED(3, "DELIVERED"),
    COMPLETED(4, "COMPLETED"),
    CANCELLED(99, "CANCELLED");

    private final int step;
    private final String code;

    OrderStatus(int step, String code) {
        this.step = step;
        this.code = code;
    }

    public int getStep() {
        return step;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status code");
    }

    public static OrderStatus fromStep(int step) {
        for (OrderStatus status : values()) {
            if (status.step == step) return status;
        }
        throw new IllegalArgumentException("Invalid step");
    }
}
