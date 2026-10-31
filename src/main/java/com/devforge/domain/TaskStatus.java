package com.devforge.domain;

public enum TaskStatus {
    BACKLOG("Backlog"),
    DOING("Doing"),
    BLOCKED("Blocked"),
    DONE("Done");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
