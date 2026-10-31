package com.devforge.web.dto;

public record TaskRequest(String title, String assignee, String status, String priority, String notes) {
}
