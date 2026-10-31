package com.devforge.util;

import java.util.regex.Pattern;

/**
 * Central regex rules used by forms and search.
 * Compiled once, reused everywhere.
 */
public final class InputPatterns {

    public static final Pattern PROJECT_KEY = Pattern.compile("^[A-Z]{2,6}$");
    public static final Pattern PERSON_NAME = Pattern.compile("^[A-Za-z][A-Za-z .']{1,58}$");
    public static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Pattern TITLE = Pattern.compile("^[\\p{L}0-9][\\p{L}0-9 .,_()\\-]{2,79}$");
    public static final Pattern SHORT_TEXT = Pattern.compile("^[\\p{L}0-9 .,;:!?()_\\-]{3,240}$");
    public static final Pattern TAG = Pattern.compile("^[a-z][a-z0-9-]{0,19}$");
    public static final Pattern LANGUAGE = Pattern.compile("^(java|sql|js|html|css|python|bash)$");
    public static final Pattern SEARCH = Pattern.compile("^[\\p{L}0-9 .,_-]{1,40}$");

    private InputPatterns() {
    }

    public static boolean matches(Pattern pattern, String value) {
        return value != null && pattern.matcher(value.trim()).matches();
    }

    public static void require(Pattern pattern, String value, String message) {
        if (!matches(pattern, value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
