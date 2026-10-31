package com.devforge.domain;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeSnippet extends WorkspaceItem {

    private Long projectId;
    private String language;
    private String tags;
    private String body;

    public CodeSnippet(Long id, Long projectId, String title, String language,
                       String tags, String body, LocalDateTime createdAt) {
        super(id, title, createdAt);
        this.projectId = projectId;
        this.language = language;
        this.tags = tags;
        this.body = body;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Set<String> getTagSet() {
        if (tags == null || tags.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String kind() {
        return "snippet";
    }

    @Override
    public String summarize() {
        return language + " snippet · " + getTitle();
    }
}
