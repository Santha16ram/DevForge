package com.devforge.domain;

import java.time.LocalDateTime;

public class Project extends WorkspaceItem {

    private String projectKey;
    private String ownerName;
    private String ownerEmail;
    private String summary;

    public Project(Long id, String projectKey, String title, String ownerName,
                   String ownerEmail, String summary, LocalDateTime createdAt) {
        super(id, title, createdAt);
        this.projectKey = projectKey;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.summary = summary;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String kind() {
        return "project";
    }

    @Override
    public String summarize() {
        return projectKey + " · " + getTitle() + " (" + ownerName + ")";
    }
}
