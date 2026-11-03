package com.tutorhub.admin.dto;

public record AdminStatsResponse(long users, long activeTutors, long openRequests, long pendingBookings, long completedBookings, long openReports) {}
