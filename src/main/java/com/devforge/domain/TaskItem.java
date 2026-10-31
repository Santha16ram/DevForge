package com.devforge.domain;

import java.time.LocalDateTime;

public class TaskItem extends WorkspaceItem {

    private Long projectId;
    private String assignee;
    private TaskStatus status;
    private Priority priority;
    private String notes;

    public TaskItem(Long id, Long projectId, String title, String assignee,
                    TaskStatus status, Priority priority, String notes, LocalDateTime createdAt) {
        super(id, title, createdAt);
        this.projectId = projectId;
        this.assignee = assignee;
        this.status = status;
        this.priority = priority;
        this.notes = notes;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String kind() {
        return "task";
    }

    @Override
    public String summarize() {
        return status.getDisplayName() + " / " + priority.getDisplayName()
                + " · " + getTitle() + " → " + assignee;
    }
}
