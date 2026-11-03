package com.tutorhub.chat.service;

import com.tutorhub.chat.dto.*;
import com.tutorhub.common.exception.*;
import com.tutorhub.common.pagination.PageResponse;
import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.user.entity.Role;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private static final Pattern OFF_PLATFORM = Pattern.compile("(?i)(https?://\\S+|\\b\\d[\\d .-]{7,}\\d\\b|\\b(momo|vietqr|ngan hang|bank)\\b)");
    private final JdbcTemplate jdbc;

    public ChatService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public ConversationResponse create(UserPrincipal principal, CreateConversationRequest request) {
        if (principal.role() != Role.PARENT && principal.role() != Role.TUTOR) throw new ForbiddenException("Only parents and tutors can chat");
        long parentId = principal.role() == Role.PARENT ? principal.id() : request.participantId();
        long tutorId = principal.role() == Role.TUTOR ? principal.id() : request.participantId();
        String expectedRole = principal.role() == Role.PARENT ? "TUTOR" : "PARENT";
        if (jdbc.queryForObject("select count(*) from users where id=? and role=? and status='ACTIVE'", Integer.class, request.participantId(), expectedRole) == 0) throw new NotFoundException("Participant not found");
        try {
            GeneratedKeyHolder keys = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                var statement = connection.prepareStatement("insert into conversations(parent_id,tutor_id,request_id) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
                statement.setLong(1, parentId); statement.setLong(2, tutorId);
                if (request.requestId() == null) statement.setNull(3, java.sql.Types.BIGINT); else statement.setLong(3, request.requestId());
                return statement;
            }, keys);
            return member(keys.getKey().longValue(), principal.id());
        } catch (DataIntegrityViolationException ignored) {
            Long id = jdbc.query("select id from conversations where parent_id=? and tutor_id=?", rs -> rs.next() ? rs.getLong(1) : null, parentId, tutorId);
            if (id == null) throw ignored;
            return member(id, principal.id());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ConversationResponse> list(UserPrincipal principal, int page, int size) {
        long total = jdbc.queryForObject("select count(*) from conversations where parent_id=? or tutor_id=?", Long.class, principal.id(), principal.id());
        List<ConversationResponse> content = jdbc.query("select id,parent_id,tutor_id,request_id,last_message_at,created_at from conversations where parent_id=? or tutor_id=? order by last_message_at desc nulls last,id desc limit ? offset ?", this::mapConversation, principal.id(), principal.id(), size, page * size);
        return new PageResponse<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }

    @Transactional(readOnly = true)
    public MessagePage messages(UserPrincipal principal, long conversationId, Long beforeId, int size) {
        member(conversationId, principal.id());
        String sql = beforeId == null ? "select id,sender_id,content,read_at,created_at from messages where conversation_id=? order by id desc limit ?" : "select id,sender_id,content,read_at,created_at from messages where conversation_id=? and id<? order by id desc limit ?";
        List<MessageResponse> content = beforeId == null ? jdbc.query(sql, (rs, row) -> mapMessage(rs, false), conversationId, size) : jdbc.query(sql, (rs, row) -> mapMessage(rs, false), conversationId, beforeId, size);
        return new MessagePage(content, content.size() == size ? content.getLast().id() : null);
    }

    @Transactional
    public MessageResponse send(UserPrincipal principal, long conversationId, MessageRequest request) {
        ConversationResponse conversation = member(conversationId, principal.id());
        boolean caution = !confirmed(conversation) && OFF_PLATFORM.matcher(request.content()).find();
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> { var statement = connection.prepareStatement("insert into messages(conversation_id,sender_id,content) values(?,?,?)", Statement.RETURN_GENERATED_KEYS); statement.setLong(1, conversationId); statement.setLong(2, principal.id()); statement.setString(3, request.content()); return statement; }, keys);
        jdbc.update("update conversations set last_message_at=now() where id=?", conversationId);
        return new MessageResponse(keys.getKey().longValue(), principal.id(), request.content(), null, Instant.now(), caution);
    }

    @Transactional
    public void markRead(UserPrincipal principal, long conversationId) { member(conversationId, principal.id()); jdbc.update("update messages set read_at=now() where conversation_id=? and sender_id<>? and read_at is null", conversationId, principal.id()); }

    private ConversationResponse member(long conversationId, long userId) {
        ConversationResponse result = jdbc.query("select id,parent_id,tutor_id,request_id,last_message_at,created_at from conversations where id=? and (parent_id=? or tutor_id=?)", rs -> rs.next() ? mapConversation(rs, 0) : null, conversationId, userId, userId);
        if (result == null) throw new NotFoundException("Conversation not found");
        return result;
    }
    private boolean confirmed(ConversationResponse conversation) { return jdbc.queryForObject("select exists(select 1 from bookings where parent_id=? and tutor_id=? and status='CONFIRMED')", Boolean.class, conversation.parentId(), conversation.tutorId()); }
    private ConversationResponse mapConversation(ResultSet rs, int ignored) throws java.sql.SQLException { return new ConversationResponse(rs.getLong(1), rs.getLong(2), rs.getLong(3), (Long) rs.getObject(4), rs.getObject(5, Instant.class), rs.getObject(6, Instant.class)); }
    private MessageResponse mapMessage(ResultSet rs, boolean caution) throws java.sql.SQLException { return new MessageResponse(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getObject(4, Instant.class), rs.getObject(5, Instant.class), caution); }
}
