package com.tutorhub.admin.service;
import com.tutorhub.common.util.JdbcTime;

import com.tutorhub.admin.dto.*;
import com.tutorhub.common.exception.NotFoundException;
import com.tutorhub.common.pagination.PageResponse;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final JdbcTemplate jdbc;
    public AdminService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true) public PageResponse<AdminUserResponse> users(int page, int size) {
        long total = jdbc.queryForObject("select count(*) from users", Long.class);
        List<AdminUserResponse> content = jdbc.query("select id,email,phone,full_name,role,status,created_at from users order by created_at desc limit ? offset ?", this::user, size, page * size);
        return page(content, page, size, total);
    }
    @Transactional public void setUserStatus(long actorId, long id, String status) {
        if (jdbc.update("update users set status=? where id=?", status, id) == 0) throw new NotFoundException("User not found");
        audit(actorId, "USER_" + status, "USER", id);
    }
    @Transactional(readOnly = true) public PageResponse<AdminReportResponse> reports(int page, int size) {
        long total = jdbc.queryForObject("select count(*) from reports", Long.class);
        List<AdminReportResponse> content = jdbc.query("select id,reporter_id,target_type,target_id,reason,description,status,resolved_by,resolution,created_at,resolved_at from reports order by created_at desc limit ? offset ?", this::report, size, page * size);
        return page(content, page, size, total);
    }
    @Transactional public void resolve(long actorId, long id, String resolution) {
        if (jdbc.update("update reports set status='RESOLVED',resolved_by=?,resolution=?,resolved_at=now() where id=? and status='OPEN'", actorId, resolution, id) == 0) throw new NotFoundException("Open report not found");
        audit(actorId, "REPORT_RESOLVED", "REPORT", id);
    }
    @Transactional public void hideReview(long actorId, long id) {
        Long tutorId = jdbc.query("select tutor_id from reviews where id=? and visible", rs -> rs.next() ? rs.getLong(1) : null, id);
        if (tutorId == null) throw new NotFoundException("Visible review not found");
        jdbc.update("update reviews set visible=false where id=?", id);
        recalculateRating(tutorId); audit(actorId, "REVIEW_HIDDEN", "REVIEW", id);
    }
    @Transactional(readOnly = true) public PageResponse<AuditLogResponse> auditLogs(int page, int size) {
        long total = jdbc.queryForObject("select count(*) from audit_logs", Long.class);
        List<AuditLogResponse> content = jdbc.query("select id,actor_id,action,entity_type,entity_id,ip_address,created_at from audit_logs order by created_at desc limit ? offset ?", this::auditLog, size, page * size);
        return page(content, page, size, total);
    }
    @Transactional(readOnly = true) public AdminStatsResponse stats() {
        return new AdminStatsResponse(count("users"), count("tutor_profiles where verification_status='VERIFIED'"), count("tutor_requests where status='OPEN'"), count("bookings where status='PENDING'"), count("bookings where status='COMPLETED'"), count("reports where status='OPEN'"));
    }
    private long count(String table) { return jdbc.queryForObject("select count(*) from " + table, Long.class); }
    private void recalculateRating(long tutorId) { jdbc.update("update tutor_profiles set rating_avg=(select coalesce(avg(rating),0) from reviews where tutor_id=? and visible),rating_count=(select count(*) from reviews where tutor_id=? and visible) where user_id=?", tutorId, tutorId, tutorId); }
    private void audit(long actorId, String action, String type, long id) { jdbc.update("insert into audit_logs(actor_id,action,entity_type,entity_id) values(?,?,?,?)", actorId, action, type, id); }
    private AdminUserResponse user(ResultSet rs, int row) throws java.sql.SQLException { return new AdminUserResponse(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), JdbcTime.instant(rs,7)); }
    private AdminReportResponse report(ResultSet rs, int row) throws java.sql.SQLException { return new AdminReportResponse(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getLong(4), rs.getString(5), rs.getString(6), rs.getString(7), (Long) rs.getObject(8), rs.getString(9), JdbcTime.instant(rs,10), JdbcTime.instant(rs,11)); }
    private AuditLogResponse auditLog(ResultSet rs, int row) throws java.sql.SQLException { return new AuditLogResponse(rs.getLong(1), (Long) rs.getObject(2), rs.getString(3), rs.getString(4), (Long) rs.getObject(5), rs.getString(6), JdbcTime.instant(rs,7)); }
    private <T> PageResponse<T> page(List<T> content, int page, int size, long total) { return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size)); }
}
