package com.tutorhub.booking.service;
import com.tutorhub.common.util.JdbcTime;

import com.tutorhub.booking.dto.*;
import com.tutorhub.booking.entity.BookingStatus;
import com.tutorhub.common.audit.AuditService;
import com.tutorhub.common.exception.*;
import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.user.entity.Role;
import java.sql.ResultSet;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class BookingService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final JdbcTemplate jdbc; private final BookingStateMachine states; private final AuditService audit; private final TransactionTemplate transactions; private final Duration cancellationWindow;
    public BookingService(JdbcTemplate jdbc, BookingStateMachine states, AuditService audit, TransactionTemplate transactions, @Value("${app.booking.cancellation-minutes:360}") long cancellationMinutes) { this.jdbc=jdbc; this.states=states; this.audit=audit; this.transactions=transactions; this.cancellationWindow=Duration.ofMinutes(cancellationMinutes); }

    @Transactional public BookingResponse create(UserPrincipal principal, CreateBookingRequest request) { return createInternal(principal, request); }
    public RecurringBookingResponse recurring(UserPrincipal principal, RecurringBookingRequest request) {
        List<BookingResponse> created=new ArrayList<>(); List<RecurringBookingFailure> failed=new ArrayList<>(); CreateBookingRequest base=request.booking(); Duration duration=Duration.between(base.startAt(),base.endAt());
        for(int index=0;index<request.occurrences();index++) { Instant start=base.startAt().plus(Duration.ofDays(7L*index)); CreateBookingRequest occurrence=new CreateBookingRequest(base.tutorId(),base.childId(),base.subjectId(),start,start.plus(duration),base.mode(),base.location(),base.meetingUrl(),base.price(),base.isTrial()); try { created.add(transactions.execute(status->createInternal(principal,occurrence))); } catch(ApiException exception) { failed.add(new RecurringBookingFailure(start,exception.getCode(),exception.getMessage())); } }
        return new RecurringBookingResponse(created,failed);
    }
    @Transactional public BookingResponse action(UserPrincipal principal, Long id, String action, String reason) {
        BookingResponse booking=one(id,principal); boolean tutor=principal.id().equals(booking.tutorId()),parent=principal.id().equals(booking.parentId());
        if((action.equals("confirm")||action.equals("complete")||action.equals("no-show"))&&!tutor)throw new NotFoundException("Booking not found"); if(action.equals("cancel")&&!tutor&&!parent)throw new NotFoundException("Booking not found");
        if(action.equals("cancel")&&Instant.now().plus(cancellationWindow).isAfter(booking.startAt()))throw new BusinessRuleException("CANCELLATION_WINDOW_CLOSED","Bookings can only be cancelled at least "+cancellationWindow.toHours()+" hours before they start");
        BookingStatus target=states.transition(BookingStatus.valueOf(booking.status()),action,booking.startAt(),Instant.now()); jdbc.update("update bookings set status=?,cancelled_by=?,cancel_reason=?,completed_at=case when ?='COMPLETED' then now() else completed_at end where id=?",target.name(),target==BookingStatus.CANCELLED?principal.id():null,target==BookingStatus.CANCELLED?reason:null,target.name(),id);
        audit.record(principal.id(),"BOOKING_"+target.name(),"BOOKING",id); notify(tutor?booking.parentId():booking.tutorId(),"BOOKING_"+target.name(),"Booking updated","Your booking #"+id+" is "+target.name()); return one(id,principal);
    }
    @Transactional(readOnly=true) public List<BookingResponse> mine(UserPrincipal principal) { return jdbc.query(select()+" where b.tutor_id=? or b.parent_id=? order by b.start_at desc",(rs,row)->map(rs,principal),principal.id(),principal.id()); }
    @Transactional(readOnly=true) public BookingResponse one(Long id,UserPrincipal principal) { BookingResponse booking=jdbc.query(select()+" where b.id=? and (b.tutor_id=? or b.parent_id=?)",rs->rs.next()?map(rs,principal):null,id,principal.id(),principal.id()); if(booking==null)throw new NotFoundException("Booking not found"); return booking; }

    private BookingResponse createInternal(UserPrincipal principal,CreateBookingRequest request) {
        if(principal.role()!=Role.PARENT)throw new ForbiddenException("Parent role required"); validate(principal,request);
        try { GeneratedKeyHolder key=new GeneratedKeyHolder(); jdbc.update(connection->{var statement=connection.prepareStatement("insert into bookings(tutor_id,parent_id,child_id,subject_id,start_at,end_at,mode,location,meeting_url,price,is_trial) values(?,?,?,?,?,?,?,?,?,?,?)",new String[]{"id"}); statement.setLong(1,request.tutorId()); statement.setLong(2,principal.id()); if(request.childId()==null)statement.setNull(3,java.sql.Types.BIGINT);else statement.setLong(3,request.childId()); statement.setInt(4,request.subjectId()); statement.setObject(5,request.startAt()); statement.setObject(6,request.endAt()); statement.setString(7,request.mode()); statement.setString(8,request.location()); statement.setString(9,request.meetingUrl()); statement.setLong(10,request.price()); statement.setBoolean(11,request.isTrial()); return statement;},key); long id=((Number) key.getKeys().get("id")).longValue(); notify(request.tutorId(),"BOOKING_CREATED","New booking","You have a new booking request"); return one(id,principal); } catch(DataIntegrityViolationException exception) { if(String.valueOf(exception.getMostSpecificCause()).contains("ex_bookings_no_overlap"))throw new ConflictException("BOOKING_OVERLAP","Tutor already has a booking in this time range"); throw exception; }
    }
    private void validate(UserPrincipal principal,CreateBookingRequest request) {
        if(!request.endAt().isAfter(request.startAt()))throw new BusinessRuleException("INVALID_TIME_RANGE","endAt must be after startAt"); if(!request.startAt().isAfter(Instant.now()))throw new BusinessRuleException("BOOKING_IN_PAST","Booking must start in the future"); long minutes=Duration.between(request.startAt(),request.endAt()).toMinutes(); if(minutes<30||minutes>240)throw new BusinessRuleException("INVALID_DURATION","Booking duration must be between 30 and 240 minutes");
        if(request.childId()!=null&&jdbc.queryForObject("select count(*) from children where id=? and parent_id=?",Integer.class,request.childId(),principal.id())==0)throw new NotFoundException("Child not found"); if(jdbc.queryForObject("select count(*) from tutor_profiles where user_id=? and verification_status='VERIFIED'",Integer.class,request.tutorId())==0)throw new BusinessRuleException("TUTOR_NOT_VERIFIED","Tutor is not verified"); if(jdbc.queryForObject("select count(*) from subjects where id=? and active",Integer.class,request.subjectId())==0)throw new NotFoundException("Subject not found"); if(jdbc.queryForObject("select count(*) from tutor_subjects where tutor_id=? and subject_id=?",Integer.class,request.tutorId(),request.subjectId())==0)throw new BusinessRuleException("TUTOR_DOES_NOT_TEACH_SUBJECT","Tutor does not teach this subject");
        ZonedDateTime start=request.startAt().atZone(BUSINESS_ZONE),end=request.endAt().atZone(BUSINESS_ZONE); if(!start.toLocalDate().equals(end.toLocalDate())||jdbc.queryForObject("select count(*) from tutor_availability where tutor_id=? and day_of_week=? and start_time<=? and end_time>=?",Integer.class,request.tutorId(),start.getDayOfWeek().getValue(),java.sql.Time.valueOf(start.toLocalTime()),java.sql.Time.valueOf(end.toLocalTime()))==0)throw new BusinessRuleException("TUTOR_UNAVAILABLE","Tutor is not available in this time range");
    }
    private String select(){return "select b.id,b.tutor_id,b.parent_id,b.child_id,b.subject_id,b.start_at,b.end_at,b.mode,b.location,b.meeting_url,b.price,b.is_trial,b.status,b.cancel_reason,tu.phone,pu.phone from bookings b join users tu on tu.id=b.tutor_id join users pu on pu.id=b.parent_id";}
    private BookingResponse map(ResultSet rs,UserPrincipal principal)throws java.sql.SQLException { String status=rs.getString(13); boolean confirmed="CONFIRMED".equals(status); String tutorPhone=confirmed&&principal.id().equals(rs.getLong(3))?rs.getString(15):null; String parentPhone=confirmed&&principal.id().equals(rs.getLong(2))?rs.getString(16):null; return new BookingResponse(rs.getLong(1),rs.getLong(2),rs.getLong(3),(Long)rs.getObject(4),rs.getInt(5),JdbcTime.instant(rs,6),JdbcTime.instant(rs,7),rs.getString(8),rs.getString(9),rs.getString(10),rs.getLong(11),rs.getBoolean(12),status,rs.getString(14),tutorPhone,parentPhone); }
    private void notify(Long userId,String type,String title,String body){jdbc.update("insert into notifications(user_id,type,title,body) values(?,?,?,?)",userId,type,title,body);}
}
