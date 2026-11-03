package com.tutorhub.tutor.dto;
import java.math.BigDecimal;
import java.util.List;
public record TutorPublicResponse(Long id,String fullName,String headline,String bio,String educationLevel,String school,String major,short yearsExperience,long hourlyRate,String teachingMode,Integer districtId,BigDecimal ratingAvg,int ratingCount,List<TutorSubjectResponse> subjects) {}
