package com.tutorhub.notification.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final JdbcTemplate jdbc;
    public NotificationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public void create(Long userId, String type, String title, String body, String data) {
        jdbc.update("insert into notifications(user_id,type,title,body,data) values(?,?,?,?,cast(? as jsonb))", userId, type, title, body, data);
    }
}
