package com.tutorhub.tutor.dto;
import java.time.Instant;
public record TutorDocumentResponse(Long id,String type,String status,String url,Instant createdAt) {}
