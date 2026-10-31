package com.devforge.domain;

public enum Priority {
    LOW("Low"),
    NORMAL("Normal"),
    HIGH("High");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
