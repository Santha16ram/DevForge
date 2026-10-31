package com.devforge.domain;

import java.time.LocalDateTime;

/**
 * Shared shape for anything that lives on a DevForge board.
 * Concrete types (project, task, snippet) fill in the details.
 */
public abstract class WorkspaceItem implements PersistableEntity {

    private Long id;
    private String title;
    private LocalDateTime createdAt;

    protected WorkspaceItem(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String label() {
        return title;
    }

    public abstract String kind();

    public abstract String summarize();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        WorkspaceItem that = (WorkspaceItem) other;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}
