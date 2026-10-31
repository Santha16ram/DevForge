package com.devforge.service;

import com.devforge.dao.SnippetDao;
import com.devforge.domain.CodeSnippet;
import com.devforge.util.InputPatterns;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SnippetService {

    private final SnippetDao snippetDao;
    private final ProjectService projectService;

    public SnippetService(SnippetDao snippetDao, ProjectService projectService) {
        this.snippetDao = snippetDao;
        this.projectService = projectService;
    }

    public List<CodeSnippet> list(long projectId) {
        projectService.require(projectId);
        return snippetDao.findByProject(projectId);
    }

    public Set<String> uniqueTags(long projectId) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        for (CodeSnippet snippet : list(projectId)) {
            tags.addAll(snippet.getTagSet());
        }
        return tags;
    }

    public List<CodeSnippet> search(long projectId, String query) {
        InputPatterns.require(InputPatterns.SEARCH, query, "Search only allows letters, numbers, spaces, and .,_-");
        Pattern needle = Pattern.compile(Pattern.quote(query.trim()), Pattern.CASE_INSENSITIVE);
        List<CodeSnippet> hits = new ArrayList<>();
        for (CodeSnippet snippet : list(projectId)) {
            String haystack = snippet.getTitle() + " " + snippet.getLanguage() + " " + snippet.getTags() + " " + snippet.getBody();
            if (needle.matcher(haystack).find()) {
                hits.add(snippet);
            }
        }
        return hits;
    }

    public CodeSnippet create(long projectId, String title, String language, String tags, String body) {
        projectService.require(projectId);
        InputPatterns.require(InputPatterns.TITLE, title, "Snippet title should be 3-80 characters.");
        String lang = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        InputPatterns.require(InputPatterns.LANGUAGE, lang, "Language must be java, sql, js, html, css, python, or bash.");
        String cleanedTags = cleanTags(tags);
        if (body == null || body.trim().length() < 4) {
            throw new IllegalArgumentException("Paste a bit more code (at least 4 characters).");
        }
        CodeSnippet snippet = new CodeSnippet(null, projectId, title.trim(), lang,
                cleanedTags, body, LocalDateTime.now());
        return snippetDao.insert(snippet);
    }

    public CodeSnippet update(long id, String title, String language, String tags, String body) {
        CodeSnippet existing = snippetDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No snippet with that id."));
        InputPatterns.require(InputPatterns.TITLE, title, "Snippet title should be 3-80 characters.");
        String lang = language == null ? "" : language.trim().toLowerCase(Locale.ROOT);
        InputPatterns.require(InputPatterns.LANGUAGE, lang, "Language must be java, sql, js, html, css, python, or bash.");
        if (body == null || body.trim().length() < 4) {
            throw new IllegalArgumentException("Paste a bit more code (at least 4 characters).");
        }
        existing.setTitle(title.trim());
        existing.setLanguage(lang);
        existing.setTags(cleanTags(tags));
        existing.setBody(body);
        snippetDao.update(existing);
        return existing;
    }

    public void delete(long id) {
        snippetDao.findById(id).orElseThrow(() -> new IllegalArgumentException("No snippet with that id."));
        snippetDao.delete(id);
    }

    private String cleanTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return "general";
        }
        LinkedHashSet<String> cleaned = Arrays.stream(tags.split("[,\\s]+"))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Add at least one tag, e.g. jdbc or regex.");
        }
        for (String tag : cleaned) {
            InputPatterns.require(InputPatterns.TAG, tag, "Tags must look like jdbc or spring-boot (lowercase, hyphens ok).");
        }
        return String.join(",", cleaned);
    }
}
