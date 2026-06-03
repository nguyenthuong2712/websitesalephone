package org.example.websitesalephone.enums;

import java.util.Objects;

public enum RoleEnums {

    ADMIN("ADMIN", "ADMIN"),
    STAFF("STAFF", "STAFF"),
    CUSTOMER("CUSTOMER", "CUSTOMER"),
    PARTNER("PARTNER", "PARTNER");

    private final String id;
    private final String value;

    RoleEnums(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static RoleEnums fromId(String id) {
        for (RoleEnums role : values()) {
            if (Objects.equals(role.getId(), id)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid Role ID: " + id);
    }

    public static RoleEnums fromValue(String value) {
        for (RoleEnums role : values()) {
            if (role.getValue().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid Role Value: " + value);
    }
}
