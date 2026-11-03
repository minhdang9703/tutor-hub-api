package com.tutorhub.tutor.dto;
import java.math.BigDecimal;
import java.time.Instant;
public record TutorCardResponse(Long id,String fullName,String headline,long hourlyRate,String teachingMode,Integer districtId,BigDecimal ratingAvg,int ratingCount,Instant createdAt) {}
