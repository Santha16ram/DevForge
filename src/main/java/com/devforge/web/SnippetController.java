package com.devforge.web;

import com.devforge.domain.CodeSnippet;
import com.devforge.service.SnippetService;
import com.devforge.web.dto.SnippetRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class SnippetController {

    private final SnippetService snippetService;

    public SnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    @GetMapping("/projects/{projectId}/snippets")
    public List<CodeSnippet> list(@PathVariable long projectId) {
        return snippetService.list(projectId);
    }

    @GetMapping("/projects/{projectId}/snippet-tags")
    public Set<String> tags(@PathVariable long projectId) {
        return snippetService.uniqueTags(projectId);
    }

    @GetMapping("/projects/{projectId}/snippets/search")
    public List<CodeSnippet> search(@PathVariable long projectId, @RequestParam String q) {
        return snippetService.search(projectId, q);
    }

    @PostMapping("/projects/{projectId}/snippets")
    public CodeSnippet create(@PathVariable long projectId, @RequestBody SnippetRequest req) {
        return snippetService.create(projectId, req.title(), req.language(), req.tags(), req.body());
    }

    @PutMapping("/snippets/{id}")
    public CodeSnippet update(@PathVariable long id, @RequestBody SnippetRequest req) {
        return snippetService.update(id, req.title(), req.language(), req.tags(), req.body());
    }

    @DeleteMapping("/snippets/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        snippetService.delete(id);
    }
}
