package com.devforge.service;

import com.devforge.dao.ProjectDao;
import com.devforge.domain.Project;
import com.devforge.util.InputPatterns;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectDao projectDao;

    public ProjectService(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public List<Project> list() {
        return projectDao.findAll();
    }

    public Project require(long id) {
        return projectDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No project with that id."));
    }

    public Project create(String key, String title, String ownerName, String ownerEmail, String summary) {
        String normalizedKey = key == null ? "" : key.trim().toUpperCase();
        InputPatterns.require(InputPatterns.PROJECT_KEY, normalizedKey, "Key must be 2-6 capital letters, e.g. CAMP or LIB.");
        InputPatterns.require(InputPatterns.TITLE, title, "Title should be 3-80 letters, numbers, and basic punctuation.");
        InputPatterns.require(InputPatterns.PERSON_NAME, ownerName, "Owner name looks off. Use letters and spaces.");
        InputPatterns.require(InputPatterns.EMAIL, ownerEmail, "Owner email is not a valid address.");
        InputPatterns.require(InputPatterns.SHORT_TEXT, summary, "Summary should be a short sentence (3-240 chars).");
        if (projectDao.keyExists(normalizedKey)) {
            throw new IllegalArgumentException("That project key is already in use.");
        }
        Project project = new Project(null, normalizedKey, title.trim(), ownerName.trim(),
                ownerEmail.trim(), summary.trim(), LocalDateTime.now());
        return projectDao.insert(project);
    }

    public Project update(long id, String title, String ownerName, String ownerEmail, String summary) {
        Project existing = require(id);
        InputPatterns.require(InputPatterns.TITLE, title, "Title should be 3-80 letters, numbers, and basic punctuation.");
        InputPatterns.require(InputPatterns.PERSON_NAME, ownerName, "Owner name looks off. Use letters and spaces.");
        InputPatterns.require(InputPatterns.EMAIL, ownerEmail, "Owner email is not a valid address.");
        InputPatterns.require(InputPatterns.SHORT_TEXT, summary, "Summary should be a short sentence (3-240 chars).");
        existing.setTitle(title.trim());
        existing.setOwnerName(ownerName.trim());
        existing.setOwnerEmail(ownerEmail.trim());
        existing.setSummary(summary.trim());
        projectDao.update(existing);
        return existing;
    }

    public void delete(long id) {
        require(id);
        projectDao.delete(id);
    }
}
