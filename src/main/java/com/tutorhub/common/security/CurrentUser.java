package com.tutorhub.common.security;

import com.tutorhub.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
@Component public class CurrentUser { public UserPrincipal require() { Authentication auth = SecurityContextHolder.getContext().getAuthentication(); if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) throw new UnauthorizedException("Authentication required"); return principal; } }
