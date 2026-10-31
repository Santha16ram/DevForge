package com.devforge.dao;

import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

final class GeneratedIds {

    private GeneratedIds() {
    }

    static long from(KeyHolder keys) {
        // Use the key list only. getKey()/getKeys() throw when H2 returns extra
        // columns such as created_at alongside the auto-increment id.
        if (keys.getKeyList() == null || keys.getKeyList().isEmpty()) {
            throw new IllegalStateException("Insert did not return an id.");
        }
        Long fromList = readId(keys.getKeyList().getFirst());
        if (fromList != null) {
            return fromList;
        }
        throw new IllegalStateException("Insert did not return an id.");
    }

    private static Long readId(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Object id = map.get("id");
        if (id == null) {
            id = map.get("ID");
        }
        if (id instanceof Number n) {
            return n.longValue();
        }
        for (Object value : map.values()) {
            if (value instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }
}
