package com.devforge.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WorkspaceApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void dashboardAndSampleProjectAreAvailable() throws Exception {
        mvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectCount").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void regexRejectsABadProjectKey() throws Exception {
        mvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectKey":"camp1","title":"Bad Key Demo","ownerName":"Test User",
                                 "ownerEmail":"test.user@college.edu","summary":"This should fail the key regex."}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("capital letters")));
    }

    @Test
    void canCreateProjectTaskAndSearchSnippet() throws Exception {
        String key = randomKey();
        String created = mvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"projectKey":"%s","title":"Demo Board","ownerName":"Neha Patel",
                                 "ownerEmail":"neha.patel@college.edu","summary":"Temporary board used by the API test."}
                                """.formatted(key)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value(key))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = ((Number) JsonPath.read(created, "$.id")).longValue();

        mvc.perform(post("/api/projects/" + id + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Write JDBC insert","assignee":"Neha Patel","status":"DOING",
                                 "priority":"HIGH","notes":"Use a prepared statement."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOING"));

        mvc.perform(post("/api/projects/" + id + "/snippets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Count query","language":"sql","tags":"jdbc,mysql",
                                 "body":"SELECT COUNT(*) FROM tasks WHERE project_id = ?"}
                                """))
                .andExpect(status().isOk());

        mvc.perform(get("/api/projects/" + id + "/snippets/search").param("q", "COUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Count query"));
    }

    private static String randomKey() {
        String letters = Long.toString(System.nanoTime(), 36).replaceAll("[0-9]", "A").toUpperCase();
        return ("K" + letters + "ABCDEF").substring(0, 6);
    }
}
