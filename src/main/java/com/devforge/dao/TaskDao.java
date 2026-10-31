package com.devforge.dao;

import com.devforge.domain.Priority;
import com.devforge.domain.TaskItem;
import com.devforge.domain.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskDao {

    private final JdbcTemplate jdbc;

    private final RowMapper<TaskItem> mapper = (rs, rowNum) -> new TaskItem(
            rs.getLong("id"),
            rs.getLong("project_id"),
            rs.getString("title"),
            rs.getString("assignee"),
            TaskStatus.valueOf(rs.getString("status")),
            Priority.valueOf(rs.getString("priority")),
            rs.getString("notes"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public TaskDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TaskItem> findByProject(long projectId) {
        return jdbc.query(
                "SELECT * FROM tasks WHERE project_id = ? ORDER BY created_at DESC",
                mapper, projectId);
    }

    public List<TaskItem> findAll() {
        return jdbc.query("SELECT * FROM tasks ORDER BY created_at DESC", mapper);
    }

    public Optional<TaskItem> findById(long id) {
        List<TaskItem> rows = jdbc.query("SELECT * FROM tasks WHERE id = ?", mapper, id);
        return rows.stream().findFirst();
    }

    public TaskItem insert(TaskItem task) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO tasks (project_id, title, assignee, status, priority, notes, created_at) VALUES (?,?,?,?,?,?,?)",
                    new String[] {"id"});
            ps.setLong(1, task.getProjectId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getAssignee());
            ps.setString(4, task.getStatus().name());
            ps.setString(5, task.getPriority().name());
            ps.setString(6, task.getNotes());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keys);
        task.setId(GeneratedIds.from(keys));
        return task;
    }

    public void update(TaskItem task) {
        jdbc.update(
                "UPDATE tasks SET title = ?, assignee = ?, status = ?, priority = ?, notes = ? WHERE id = ?",
                task.getTitle(), task.getAssignee(), task.getStatus().name(),
                task.getPriority().name(), task.getNotes(), task.getId());
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM tasks WHERE id = ?", id);
    }
}
