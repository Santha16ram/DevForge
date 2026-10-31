package com.devforge.web;

import com.devforge.domain.Project;
import com.devforge.service.ProjectService;
import com.devforge.web.dto.ProjectRequest;
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

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> list() {
        return projectService.list();
    }

    @GetMapping("/{id}")
    public Project one(@PathVariable long id) {
        return projectService.require(id);
    }

    @PostMapping
    public Project create(@RequestBody ProjectRequest req) {
        return projectService.create(req.projectKey(), req.title(), req.ownerName(), req.ownerEmail(), req.summary());
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable long id, @RequestBody ProjectRequest req) {
        return projectService.update(id, req.title(), req.ownerName(), req.ownerEmail(), req.summary());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        projectService.delete(id);
    }
}
