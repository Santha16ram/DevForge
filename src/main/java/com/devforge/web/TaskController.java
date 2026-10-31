package com.devforge.web;

import com.devforge.domain.TaskItem;
import com.devforge.domain.TaskStatus;
import com.devforge.service.TaskService;
import com.devforge.web.dto.TaskRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskItem> list(@PathVariable long projectId) {
        return taskService.list(projectId);
    }

    @GetMapping("/projects/{projectId}/board")
    public Map<TaskStatus, List<TaskItem>> board(@PathVariable long projectId) {
        return taskService.board(projectId);
    }

    @GetMapping("/projects/{projectId}/assignees")
    public Set<String> assignees(@PathVariable long projectId) {
        return taskService.assignees(projectId);
    }

    @PostMapping("/projects/{projectId}/tasks")
    public TaskItem create(@PathVariable long projectId, @RequestBody TaskRequest req) {
        return taskService.create(projectId, req.title(), req.assignee(), req.status(), req.priority(), req.notes());
    }

    @PutMapping("/tasks/{id}")
    public TaskItem update(@PathVariable long id, @RequestBody TaskRequest req) {
        return taskService.update(id, req.title(), req.assignee(), req.status(), req.priority(), req.notes());
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskService.delete(id);
    }
}
