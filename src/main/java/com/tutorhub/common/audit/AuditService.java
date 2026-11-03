package com.tutorhub.common.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final JdbcTemplate jdbc;
    public AuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public void record(Long actorId, String action, String entityType, Long entityId) {
        jdbc.update("insert into audit_logs(actor_id,action,entity_type,entity_id,details) values(?,?,?,?,cast(? as jsonb))", actorId, action, entityType, entityId, "{}");
    }
}
