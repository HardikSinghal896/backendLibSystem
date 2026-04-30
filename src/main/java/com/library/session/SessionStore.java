package com.library.session;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();
    private final Map<String, Boolean> tokenToAdmin = new ConcurrentHashMap<>();

    public String createSession(Long userId, boolean isAdmin) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        tokenToAdmin.put(token, isAdmin);
        return token;
    }

    public Long getUserId(String token) {
        if (token == null) return null;
        return tokenToUserId.get(token);
    }

    public boolean isAdmin(String token) {
        if (token == null) return false;
        return Boolean.TRUE.equals(tokenToAdmin.get(token));
    }

    public void invalidate(String token) {
        tokenToUserId.remove(token);
        tokenToAdmin.remove(token);
    }
}
