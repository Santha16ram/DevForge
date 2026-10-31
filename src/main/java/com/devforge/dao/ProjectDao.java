package com.devforge.dao;

import com.devforge.domain.Project;
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
public class ProjectDao {

    private final JdbcTemplate jdbc;

    private final RowMapper<Project> mapper = (rs, rowNum) -> new Project(
            rs.getLong("id"),
            rs.getString("project_key"),
            rs.getString("title"),
            rs.getString("owner_name"),
            rs.getString("owner_email"),
            rs.getString("summary"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public ProjectDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Project> findAll() {
        return jdbc.query("SELECT * FROM projects ORDER BY created_at DESC", mapper);
    }

    public Optional<Project> findById(long id) {
        List<Project> rows = jdbc.query("SELECT * FROM projects WHERE id = ?", mapper, id);
        return rows.stream().findFirst();
    }

    public boolean keyExists(String key) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE project_key = ?", Integer.class, key);
        return count != null && count > 0;
    }

    public Project insert(Project project) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO projects (project_key, title, owner_name, owner_email, summary, created_at) VALUES (?,?,?,?,?,?)",
                    new String[] {"id"});
            ps.setString(1, project.getProjectKey());
            ps.setString(2, project.getTitle());
            ps.setString(3, project.getOwnerName());
            ps.setString(4, project.getOwnerEmail());
            ps.setString(5, project.getSummary());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keys);
        project.setId(GeneratedIds.from(keys));
        return project;
    }

    public void update(Project project) {
        jdbc.update(
                "UPDATE projects SET title = ?, owner_name = ?, owner_email = ?, summary = ? WHERE id = ?",
                project.getTitle(), project.getOwnerName(), project.getOwnerEmail(),
                project.getSummary(), project.getId());
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM projects WHERE id = ?", id);
    }

    public int count() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM projects", Integer.class);
        return n == null ? 0 : n;
    }
}
