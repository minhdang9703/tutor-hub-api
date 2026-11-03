package com.tutorhub.common.security;

import com.tutorhub.user.entity.Role;
public record UserPrincipal(Long id, Role role) {}
