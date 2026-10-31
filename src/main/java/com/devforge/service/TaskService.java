package com.devforge.service;

import com.devforge.dao.TaskDao;
import com.devforge.domain.Priority;
import com.devforge.domain.TaskItem;
import com.devforge.domain.TaskStatus;
import com.devforge.util.InputPatterns;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TaskService {

    private final TaskDao taskDao;
    private final ProjectService projectService;

    public TaskService(TaskDao taskDao, ProjectService projectService) {
        this.taskDao = taskDao;
        this.projectService = projectService;
    }

    public List<TaskItem> list(long projectId) {
        projectService.require(projectId);
        return taskDao.findByProject(projectId);
    }

    public Map<TaskStatus, List<TaskItem>> board(long projectId) {
        EnumMap<TaskStatus, List<TaskItem>> columns = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            columns.put(status, new ArrayList<>());
        }
        for (TaskItem task : list(projectId)) {
            columns.get(task.getStatus()).add(task);
        }
        columns.replaceAll((status, tasks) -> Collections.unmodifiableList(tasks));
        return Collections.unmodifiableMap(columns);
    }

    public Set<String> assignees(long projectId) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (TaskItem task : list(projectId)) {
            names.add(task.getAssignee());
        }
        return Collections.unmodifiableSet(names);
    }

    public TaskItem create(long projectId, String title, String assignee, String status, String priority, String notes) {
        projectService.require(projectId);
        InputPatterns.require(InputPatterns.TITLE, title, "Task title should be 3-80 characters.");
        InputPatterns.require(InputPatterns.PERSON_NAME, assignee, "Assignee should be a person's name.");
        TaskStatus parsedStatus = parseStatus(status);
        Priority parsedPriority = parsePriority(priority);
        String noteText = notes == null || notes.isBlank() ? "No extra notes." : notes.trim();
        InputPatterns.require(InputPatterns.SHORT_TEXT, noteText, "Notes should stay under 240 characters.");
        TaskItem task = new TaskItem(null, projectId, title.trim(), assignee.trim(),
                parsedStatus, parsedPriority, noteText, LocalDateTime.now());
        return taskDao.insert(task);
    }

    public TaskItem update(long id, String title, String assignee, String status, String priority, String notes) {
        TaskItem existing = taskDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No task with that id."));
        InputPatterns.require(InputPatterns.TITLE, title, "Task title should be 3-80 characters.");
        InputPatterns.require(InputPatterns.PERSON_NAME, assignee, "Assignee should be a person's name.");
        String noteText = notes == null || notes.isBlank() ? "No extra notes." : notes.trim();
        InputPatterns.require(InputPatterns.SHORT_TEXT, noteText, "Notes should stay under 240 characters.");
        existing.setTitle(title.trim());
        existing.setAssignee(assignee.trim());
        existing.setStatus(parseStatus(status));
        existing.setPriority(parsePriority(priority));
        existing.setNotes(noteText);
        taskDao.update(existing);
        return existing;
    }

    public void delete(long id) {
        taskDao.findById(id).orElseThrow(() -> new IllegalArgumentException("No task with that id."));
        taskDao.delete(id);
    }

    private TaskStatus parseStatus(String status) {
        try {
            return TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Status must be BACKLOG, DOING, BLOCKED, or DONE.");
        }
    }

    private Priority parsePriority(String priority) {
        try {
            return Priority.valueOf(priority.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Priority must be LOW, NORMAL, or HIGH.");
        }
    }
}
