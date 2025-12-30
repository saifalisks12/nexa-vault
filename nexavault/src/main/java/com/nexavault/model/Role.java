package com.nexavault.model;

import java.util.Arrays;
import java.util.Optional;

public enum Role {
	/**
     * Represents the admin role.
     */
    ADMIN("admin"),

    /**
     * Represents the user role.
     */
    USER("user");

    private String userType;

    private Role(String userType) {
        this.userType = userType;
    }

    /**
     * Get the user type associated with the role.
     *
     * @return The user type.
     */
    public String getUserType() {
        return this.userType;
    }

    /**
     * Check if the given user type exists as a valid UserRole.
     *
     * @param userType The user type to check.
     * @return {@code true} if the user type exists, {@code false} otherwise.
     */
    public static boolean hasUserRole(String userType) {
        for (Role role : Role.values()) {
            if (role.getUserType().equalsIgnoreCase(userType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve the UserRole based on the given user type.
     *
     * @param userType The user type to search for.
     * @return An Optional containing the matched UserRole, or an empty Optional if not found.
     */
    public static Optional<Role> fromString(String userType) {
        return Arrays.stream(Role.values())
                .filter(role -> role.getUserType().equalsIgnoreCase(userType))
                .findFirst();
    }
}
