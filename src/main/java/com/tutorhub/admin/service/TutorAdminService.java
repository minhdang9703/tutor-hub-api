package com.tutorhub.admin.service;

import com.tutorhub.common.audit.AuditService;
import com.tutorhub.common.exception.NotFoundException;
import com.tutorhub.notification.service.NotificationService;
import com.tutorhub.tutor.service.TutorService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TutorAdminService {
    private final TutorService tutors; private final JdbcTemplate jdbc; private final AuditService audit; private final NotificationService notifications;
    public TutorAdminService(TutorService tutors, JdbcTemplate jdbc, AuditService audit, NotificationService notifications) { this.tutors = tutors; this.jdbc = jdbc; this.audit = audit; this.notifications = notifications; }
    @Transactional public void verify(long adminId, long tutorId) { tutors.verify(tutorId); audit.record(adminId, "TUTOR_VERIFIED", "TUTOR", tutorId); notifications.create(tutorId, "TUTOR_VERIFIED", "Profile verified", "Your tutor profile is verified", "{}"); }
    @Transactional public void reject(long adminId, long tutorId, String reason) { tutors.reject(tutorId, reason); audit.record(adminId, "TUTOR_REJECTED", "TUTOR", tutorId); notifications.create(tutorId, "TUTOR_REJECTED", "Profile rejected", reason, "{}"); }
    @Transactional public void reviewDocument(long adminId, long documentId, String status, String reason) {
        Long tutorId = jdbc.query("select tutor_id from tutor_documents where id=?", rs -> rs.next() ? rs.getLong(1) : null, documentId);
        if (tutorId == null) throw new NotFoundException("Document not found");
        tutors.reviewDocument(documentId, status, reason); audit.record(adminId, "DOCUMENT_" + status, "TUTOR_DOCUMENT", documentId); notifications.create(tutorId, "DOCUMENT_" + status, "Document reviewed", reason, "{}");
    }
}
