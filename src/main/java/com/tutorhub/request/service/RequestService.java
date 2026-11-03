package com.tutorhub.request.service;

import com.tutorhub.common.exception.*;
import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.notification.service.NotificationService;
import com.tutorhub.request.dto.*;
import com.tutorhub.user.entity.Role;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestService {
    private static final String REQUEST_COLUMNS = "id,parent_id,child_id,subject_id,grade,district_id,mode,budget_min,budget_max,sessions_per_week,schedule_note,description,status,expires_at,created_at,updated_at";
    private static final String APPLICATION_COLUMNS = "id,request_id,tutor_id,message,proposed_rate,status,created_at,updated_at";
    private final JdbcTemplate jdbc; private final NotificationService notifications;
    public RequestService(JdbcTemplate jdbc, NotificationService notifications) { this.jdbc = jdbc; this.notifications = notifications; }

    @Transactional public TutorRequestResponse create(UserPrincipal principal, TutorRequestInput input) {
        parent(principal); validate(input, principal.id());
        Long id = jdbc.queryForObject("insert into tutor_requests(parent_id,child_id,subject_id,grade,district_id,mode,budget_min,budget_max,sessions_per_week,schedule_note,description,expires_at) values(?,?,?,?,?,?,?,?,?,?,?,?) returning id", Long.class,
            principal.id(), input.childId(), input.subjectId(), input.grade(), input.districtId(), input.mode(), input.budgetMin(), input.budgetMax(), input.sessionsPerWeek(), input.scheduleNote(), input.description(), input.expiresAt());
        return request(id);
    }
    @Transactional(readOnly = true) public PageResponse<TutorRequestResponse> open(UserPrincipal principal, int page, int size) {
        tutor(principal); int safeSize = size(size), safePage = Math.max(0, page);
        long total = jdbc.queryForObject("select count(*) from tutor_requests where status='OPEN' and (expires_at is null or expires_at>now())", Long.class);
        List<TutorRequestResponse> content = jdbc.query("select " + REQUEST_COLUMNS + " from tutor_requests where status='OPEN' and (expires_at is null or expires_at>now()) order by created_at desc limit ? offset ?", (rs, n) -> request(rs), safeSize, safePage * safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, (int) Math.ceil((double) total / safeSize));
    }
    @Transactional(readOnly = true) public PageResponse<TutorRequestResponse> mine(UserPrincipal principal, int page, int size) {
        parent(principal); int safeSize = size(size), safePage = Math.max(0, page);
        long total = jdbc.queryForObject("select count(*) from tutor_requests where parent_id=?", Long.class, principal.id());
        List<TutorRequestResponse> content = jdbc.query("select " + REQUEST_COLUMNS + " from tutor_requests where parent_id=? order by created_at desc limit ? offset ?", (rs, n) -> request(rs), principal.id(), safeSize, safePage * safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, (int) Math.ceil((double) total / safeSize));
    }
    @Transactional(readOnly = true) public TutorRequestResponse get(UserPrincipal principal, Long id) {
        TutorRequestResponse request = request(id);
        if (principal.role() == Role.PARENT && request.parentId().equals(principal.id())) return request;
        tutor(principal);
        if (!"OPEN".equals(request.status()) || (request.expiresAt() != null && !request.expiresAt().isAfter(Instant.now()))) throw new NotFoundException("Request not found");
        return request;
    }
    @Transactional public TutorRequestResponse update(UserPrincipal principal, Long id, TutorRequestInput input) {
        parent(principal); validate(input, principal.id());
        if (jdbc.update("update tutor_requests set child_id=?,subject_id=?,grade=?,district_id=?,mode=?,budget_min=?,budget_max=?,sessions_per_week=?,schedule_note=?,description=?,expires_at=? where id=? and parent_id=? and status='OPEN'", input.childId(), input.subjectId(), input.grade(), input.districtId(), input.mode(), input.budgetMin(), input.budgetMax(), input.sessionsPerWeek(), input.scheduleNote(), input.description(), input.expiresAt(), id, principal.id()) == 0) throw new NotFoundException("Open request not found");
        return request(id);
    }
    @Transactional public void close(UserPrincipal principal, Long id) { parent(principal); if (jdbc.update("update tutor_requests set status='CLOSED' where id=? and parent_id=? and status='OPEN'", id, principal.id()) == 0) throw new NotFoundException("Open request not found"); }
    @Transactional public ApplicationResponse apply(UserPrincipal principal, Long requestId, ApplicationInput input) {
        tutor(principal); TutorRequestResponse request = request(requestId);
        if (!"OPEN".equals(request.status()) || (request.expiresAt() != null && !request.expiresAt().isAfter(Instant.now()))) throw new BusinessRuleException("REQUEST_NOT_OPEN", "Request is not open");
        try {
            Long id = jdbc.queryForObject("insert into applications(request_id,tutor_id,message,proposed_rate) values(?,?,?,?) returning id", Long.class, requestId, principal.id(), input.message(), input.proposedRate());
            notifications.create(request.parentId(), "APPLICATION_RECEIVED", "New tutor application", "A tutor applied to your request", "{\"requestId\":" + requestId + "}");
            return application(id);
        } catch (DataIntegrityViolationException e) { throw new ConflictException("APPLICATION_EXISTS", "Tutor already applied to this request"); }
    }
    @Transactional(readOnly = true) public List<ApplicationResponse> applications(UserPrincipal principal, Long requestId) { parentOwns(principal, requestId); return jdbc.query("select " + APPLICATION_COLUMNS + " from applications where request_id=? order by created_at desc", (rs, n) -> application(rs), requestId); }
    @Transactional(readOnly = true) public PageResponse<ApplicationResponse> myApplications(UserPrincipal principal, int page, int size) {
        tutor(principal); int safeSize = size(size), safePage = Math.max(0, page); long total = jdbc.queryForObject("select count(*) from applications where tutor_id=?", Long.class, principal.id());
        List<ApplicationResponse> content = jdbc.query("select " + APPLICATION_COLUMNS + " from applications where tutor_id=? order by created_at desc limit ? offset ?", (rs, n) -> application(rs), principal.id(), safeSize, safePage * safeSize);
        return new PageResponse<>(content, safePage, safeSize, total, (int) Math.ceil((double) total / safeSize));
    }
    @Transactional public void accept(UserPrincipal principal, Long applicationId) {
        parent(principal); long[] row = jdbc.query("select a.request_id,a.tutor_id from applications a join tutor_requests r on r.id=a.request_id where a.id=? and r.parent_id=? and r.status='OPEN' for update of r", rs -> rs.next() ? new long[]{rs.getLong(1), rs.getLong(2)} : null, applicationId, principal.id());
        if (row == null) throw new NotFoundException("Open application not found");
        if (jdbc.update("update applications set status='ACCEPTED' where id=? and status='PENDING'", applicationId) == 0) throw new ConflictException("APPLICATION_NOT_PENDING", "Application is not pending");
        List<Long> rejected = jdbc.query("select tutor_id from applications where request_id=? and id<>? and status='PENDING'", (rs, n) -> rs.getLong(1), row[0], applicationId);
        jdbc.update("update applications set status='REJECTED' where request_id=? and id<>? and status='PENDING'", row[0], applicationId);
        jdbc.update("update tutor_requests set status='MATCHED' where id=?", row[0]);
        notifications.create(row[1], "APPLICATION_ACCEPTED", "Application accepted", "You were selected for a tutoring request", "{\"requestId\":" + row[0] + "}");
        rejected.forEach(id -> notifications.create(id, "APPLICATION_REJECTED", "Application rejected", "Another tutor was selected", "{\"requestId\":" + row[0] + "}"));
    }
    @Transactional public void reject(UserPrincipal principal, Long applicationId) {
        parent(principal); long[] row = applicationForParent(applicationId, principal.id());
        if (jdbc.update("update applications set status='REJECTED' where id=? and status='PENDING'", applicationId) == 0) throw new ConflictException("APPLICATION_NOT_PENDING", "Application is not pending");
        notifications.create(row[1], "APPLICATION_REJECTED", "Application rejected", "Your application was rejected", "{\"requestId\":" + row[0] + "}");
    }
    @Transactional public void withdraw(UserPrincipal principal, Long applicationId) { tutor(principal); if (jdbc.update("update applications set status='WITHDRAWN' where id=? and tutor_id=? and status='PENDING'", applicationId, principal.id()) == 0) throw new NotFoundException("Pending application not found"); }
    @Scheduled(fixedDelayString="${app.requests.expiry-check-ms:60000}") @Transactional public void expire() { jdbc.update("update tutor_requests set status='EXPIRED' where status='OPEN' and expires_at is not null and expires_at<now()"); }
    private void validate(TutorRequestInput input, Long parentId) {
        if (input.childId() != null && jdbc.queryForObject("select count(*) from children where id=? and parent_id=?", Integer.class, input.childId(), parentId) == 0) throw new NotFoundException("Child not found");
        if (jdbc.queryForObject("select count(*) from subjects where id=? and active", Integer.class, input.subjectId()) == 0) throw new NotFoundException("Subject not found");
        if (input.districtId() != null && jdbc.queryForObject("select count(*) from districts where id=?", Integer.class, input.districtId()) == 0) throw new NotFoundException("District not found");
    }
    private void parent(UserPrincipal principal) { if (principal.role() != Role.PARENT) throw new ForbiddenException("Parent role required"); }
    private void tutor(UserPrincipal principal) { if (principal.role() != Role.TUTOR || jdbc.queryForObject("select count(*) from tutor_profiles where user_id=? and verification_status='VERIFIED'", Integer.class, principal.id()) == 0) throw new ForbiddenException("Verified tutor role required"); }
    private void parentOwns(UserPrincipal principal, Long requestId) { parent(principal); if (jdbc.queryForObject("select count(*) from tutor_requests where id=? and parent_id=?", Integer.class, requestId, principal.id()) == 0) throw new NotFoundException("Request not found"); }
    private long[] applicationForParent(Long applicationId, Long parentId) { long[] row = jdbc.query("select a.request_id,a.tutor_id from applications a join tutor_requests r on r.id=a.request_id where a.id=? and r.parent_id=?", rs -> rs.next() ? new long[]{rs.getLong(1),rs.getLong(2)} : null, applicationId, parentId); if (row == null) throw new NotFoundException("Application not found"); return row; }
    private TutorRequestResponse request(Long id) { TutorRequestResponse value = jdbc.query("select " + REQUEST_COLUMNS + " from tutor_requests where id=?", rs -> rs.next() ? request(rs) : null, id); if (value == null) throw new NotFoundException("Request not found"); return value; }
    private ApplicationResponse application(Long id) { ApplicationResponse value = jdbc.query("select " + APPLICATION_COLUMNS + " from applications where id=?", rs -> rs.next() ? application(rs) : null, id); if (value == null) throw new NotFoundException("Application not found"); return value; }
    private TutorRequestResponse request(ResultSet rs) throws SQLException { return new TutorRequestResponse(rs.getLong(1),rs.getLong(2),(Long)rs.getObject(3),rs.getInt(4),rs.getShort(5),(Integer)rs.getObject(6),rs.getString(7),(Long)rs.getObject(8),(Long)rs.getObject(9),(Short)rs.getObject(10),rs.getString(11),rs.getString(12),rs.getString(13),rs.getObject(14,Instant.class),rs.getObject(15,Instant.class),rs.getObject(16,Instant.class)); }
    private ApplicationResponse application(ResultSet rs) throws SQLException { return new ApplicationResponse(rs.getLong(1),rs.getLong(2),rs.getLong(3),rs.getString(4),rs.getLong(5),rs.getString(6),rs.getObject(7,Instant.class),rs.getObject(8,Instant.class)); }
    private int size(int value) { return Math.min(100, Math.max(1, value)); }
}
