package com.devforge.web.dto;

public record ProjectRequest(String projectKey, String title, String ownerName, String ownerEmail, String summary) {
}
