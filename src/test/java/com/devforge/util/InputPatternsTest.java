package com.devforge.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputPatternsTest {

    @Test
    void acceptsCampusStyleKeysAndEmails() {
        assertTrue(InputPatterns.matches(InputPatterns.PROJECT_KEY, "CAMP"));
        assertTrue(InputPatterns.matches(InputPatterns.EMAIL, "meera.iyer@college.edu"));
        assertTrue(InputPatterns.matches(InputPatterns.TAG, "spring-boot"));
        assertTrue(InputPatterns.matches(InputPatterns.LANGUAGE, "java"));
    }

    @Test
    void rejectsMessyInput() {
        assertFalse(InputPatterns.matches(InputPatterns.PROJECT_KEY, "camp1"));
        assertFalse(InputPatterns.matches(InputPatterns.EMAIL, "not-an-email"));
        assertFalse(InputPatterns.matches(InputPatterns.TAG, "Bad Tag"));
        assertFalse(InputPatterns.matches(InputPatterns.LANGUAGE, "kotlin"));
    }
}
