package com.tutorhub.common.util;
import java.sql.ResultSet;import java.sql.SQLException;import java.time.Instant;import java.time.OffsetDateTime;
public final class JdbcTime {
 private JdbcTime(){}
 // pgjdbc không chuyển trực tiếp timestamptz sang Instant; phải qua OffsetDateTime rồi quy về UTC.
 public static Instant instant(ResultSet rs,int column)throws SQLException{OffsetDateTime v=rs.getObject(column,OffsetDateTime.class);return v==null?null:v.toInstant();}
}
