package com.tutorhub.tutor.dto;
import java.time.Instant;
public record TutorReviewResponse(Long id,short rating,String comment,String reply,Instant createdAt) {}
