package com.devforge.service;

import com.devforge.dao.ProjectDao;
import com.devforge.dao.SnippetDao;
import com.devforge.dao.TaskDao;
import com.devforge.domain.CodeSnippet;
import com.devforge.domain.Project;
import com.devforge.domain.TaskItem;
import com.devforge.domain.TaskStatus;
import com.devforge.domain.WorkspaceItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    private final ProjectDao projectDao;
    private final TaskDao taskDao;
    private final SnippetDao snippetDao;

    public BoardService(ProjectDao projectDao, TaskDao taskDao, SnippetDao snippetDao) {
        this.projectDao = projectDao;
        this.taskDao = taskDao;
        this.snippetDao = snippetDao;
    }

    public Map<String, Object> dashboard() {
        List<Project> projects = projectDao.findAll();
        List<TaskItem> tasks = taskDao.findAll();
        List<CodeSnippet> snippets = snippetDao.findAll();

        EnumMap<TaskStatus, Integer> byStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            byStatus.put(status, 0);
        }
        for (TaskItem task : tasks) {
            byStatus.merge(task.getStatus(), 1, Integer::sum);
        }

        List<WorkspaceItem> mixed = new ArrayList<>();
        mixed.addAll(projects);
        mixed.addAll(tasks);
        mixed.addAll(snippets);
        mixed.sort(Comparator.comparing(WorkspaceItem::getCreatedAt).reversed());

        List<Map<String, String>> activity = new ArrayList<>();
        for (WorkspaceItem item : mixed.stream().limit(8).toList()) {
            Map<String, String> row = new HashMap<>();
            row.put("kind", item.kind());
            row.put("text", item.summarize());
            row.put("when", item.getCreatedAt() == null ? "" : item.getCreatedAt().toString());
            activity.add(row);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("projectCount", projects.size());
        payload.put("taskCount", tasks.size());
        payload.put("snippetCount", snippets.size());
        payload.put("openCount", tasks.stream().filter(t -> t.getStatus() != TaskStatus.DONE).count());
        payload.put("statusCounts", byStatus);
        payload.put("activity", activity);
        payload.put("projects", projects);
        return payload;
    }
}
