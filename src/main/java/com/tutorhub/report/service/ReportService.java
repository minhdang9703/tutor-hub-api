package com.tutorhub.report.service;

import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.report.dto.ReportRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final JdbcTemplate jdbc;
    public ReportService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Transactional public void create(UserPrincipal principal, ReportRequest request) { jdbc.update("insert into reports(reporter_id,target_type,target_id,reason,description) values(?,?,?,?,?)", principal.id(), request.targetType(), request.targetId(), request.reason(), request.description()); }
}
