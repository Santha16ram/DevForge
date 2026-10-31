package com.devforge.dao;

import com.devforge.domain.CodeSnippet;
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
public class SnippetDao {

    private final JdbcTemplate jdbc;

    private final RowMapper<CodeSnippet> mapper = (rs, rowNum) -> new CodeSnippet(
            rs.getLong("id"),
            rs.getLong("project_id"),
            rs.getString("title"),
            rs.getString("language"),
            rs.getString("tags"),
            rs.getString("body"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    public SnippetDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CodeSnippet> findByProject(long projectId) {
        return jdbc.query(
                "SELECT * FROM snippets WHERE project_id = ? ORDER BY created_at DESC",
                mapper, projectId);
    }

    public List<CodeSnippet> findAll() {
        return jdbc.query("SELECT * FROM snippets ORDER BY created_at DESC", mapper);
    }

    public Optional<CodeSnippet> findById(long id) {
        List<CodeSnippet> rows = jdbc.query("SELECT * FROM snippets WHERE id = ?", mapper, id);
        return rows.stream().findFirst();
    }

    public CodeSnippet insert(CodeSnippet snippet) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO snippets (project_id, title, language, tags, body, created_at) VALUES (?,?,?,?,?,?)",
                    new String[] {"id"});
            ps.setLong(1, snippet.getProjectId());
            ps.setString(2, snippet.getTitle());
            ps.setString(3, snippet.getLanguage());
            ps.setString(4, snippet.getTags());
            ps.setString(5, snippet.getBody());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keys);
        snippet.setId(GeneratedIds.from(keys));
        return snippet;
    }

    public void update(CodeSnippet snippet) {
        jdbc.update(
                "UPDATE snippets SET title = ?, language = ?, tags = ?, body = ? WHERE id = ?",
                snippet.getTitle(), snippet.getLanguage(), snippet.getTags(),
                snippet.getBody(), snippet.getId());
    }

    public void delete(long id) {
        jdbc.update("DELETE FROM snippets WHERE id = ?", id);
    }
}
