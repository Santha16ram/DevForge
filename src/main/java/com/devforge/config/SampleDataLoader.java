package com.devforge.config;

import com.devforge.dao.ProjectDao;
import com.devforge.service.ProjectService;
import com.devforge.service.SnippetService;
import com.devforge.service.TaskService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleDataLoader implements ApplicationRunner {

    private final ProjectDao projectDao;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final SnippetService snippetService;

    public SampleDataLoader(ProjectDao projectDao, ProjectService projectService,
                            TaskService taskService, SnippetService snippetService) {
        this.projectDao = projectDao;
        this.projectService = projectService;
        this.taskService = taskService;
        this.snippetService = snippetService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (projectDao.count() > 0) {
            return;
        }

        var campus = projectService.create(
                "CAMP",
                "Campus Lost and Found",
                "Meera Iyer",
                "meera.iyer@college.edu",
                "Track found cards and keys around the hostel and canteen."
        );
        var lab = projectService.create(
                "LAB",
                "Java Lab Tracker",
                "Arjun Rao",
                "arjun.rao@college.edu",
                "Keep lab programs, viva notes, and leftover bugs in one place."
        );

        taskService.create(campus.getId(), "Map hostel notice boards", "Meera Iyer", "DONE", "NORMAL", "Photos from A and B block are already in Drive.");
        taskService.create(campus.getId(), "Add claim form for ID cards", "Rohit Sen", "DOING", "HIGH", "Need registrar email before we go live.");
        taskService.create(campus.getId(), "Ask warden about duplicate keys", "Meera Iyer", "BLOCKED", "HIGH", "Waiting on Saturday meeting.");
        taskService.create(campus.getId(), "Write weekly recap mail", "Priya Nair", "BACKLOG", "LOW", "Keep it under one paragraph.");

        taskService.create(lab.getId(), "Finish JDBC DAO for books", "Arjun Rao", "DOING", "HIGH", "Use prepared statements only.");
        taskService.create(lab.getId(), "Regex demo for register numbers", "Kavya Shah", "BACKLOG", "NORMAL", "Pattern should allow 2 letters then 6 digits.");
        taskService.create(lab.getId(), "Print screenshots for record", "Arjun Rao", "DONE", "LOW", "Binder tab 4.");

        snippetService.create(campus.getId(), "Claim code check", "java", "regex,validation",
                "boolean ok = claimCode.matches(\"^[A-Z]{3}-[0-9]{4}$\");");
        snippetService.create(lab.getId(), "Select by register no", "sql", "jdbc,mysql",
                "SELECT id, title FROM lab_work WHERE register_no = ?");
        snippetService.create(lab.getId(), "Tiny fetch helper", "js", "frontend",
                "async function api(path) {\n  const res = await fetch(path);\n  if (!res.ok) throw new Error('request failed');\n  return res.json();\n}");
    }
}
