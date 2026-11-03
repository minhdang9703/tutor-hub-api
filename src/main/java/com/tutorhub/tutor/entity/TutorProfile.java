package com.tutorhub.tutor.entity;

import com.tutorhub.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "tutor_profiles")
public class TutorProfile {
    @Id private Long userId;
    @MapsId @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    private String headline; private String bio; @Column(name="education_level") private String educationLevel; private String school; private String major; @Column(name="years_experience") private short yearsExperience;
    @Column(name = "hourly_rate", nullable = false) private long hourlyRate = 0;
    @Column(name = "teaching_mode", nullable = false) private String teachingMode = "BOTH";
    @Column(name = "verification_status", nullable = false) private String verificationStatus = "DRAFT";
    @Column(name = "rating_avg", insertable = false, updatable = false) private java.math.BigDecimal ratingAvg;
    @Column(name = "rating_count", insertable = false, updatable = false) private Integer ratingCount;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @Column(name="district_id") private Integer districtId;
    public Long getUserId(){return userId;} public User getUser(){return user;} public String getHeadline(){return headline;} public String getBio(){return bio;} public String getEducationLevel(){return educationLevel;} public String getSchool(){return school;} public String getMajor(){return major;} public short getYearsExperience(){return yearsExperience;} public long getHourlyRate(){return hourlyRate;} public String getTeachingMode(){return teachingMode;} public String getVerificationStatus(){return verificationStatus;} public Integer getDistrictId(){return districtId;}
    public void setUser(User user) { this.user = user; } public void setHeadline(String x){headline=x;} public void setBio(String x){bio=x;} public void setEducationLevel(String x){educationLevel=x;} public void setSchool(String x){school=x;} public void setMajor(String x){major=x;} public void setYearsExperience(short x){yearsExperience=x;} public void setHourlyRate(long x){hourlyRate=x;} public void setTeachingMode(String x){teachingMode=x;} public void setDistrictId(Integer x){districtId=x;} public void setVerificationStatus(String x){verificationStatus=x;}
}
