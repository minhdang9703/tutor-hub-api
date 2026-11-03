package com.tutorhub.user.service;

import com.tutorhub.common.exception.*;
import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.common.storage.FileStorage;
import com.tutorhub.user.dto.*;
import com.tutorhub.user.entity.*;
import com.tutorhub.user.repository.UserRepository;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {
    private final UserRepository users; private final FileStorage storage;
    public UserService(UserRepository users, FileStorage storage) { this.users = users; this.storage = storage; }
    @Transactional(readOnly = true) public UserResponse me(UserPrincipal principal) { return response(user(principal.id())); }
    @Transactional public UserResponse update(UserPrincipal principal, UpdateUserRequest request) { User user = user(principal.id()); user.setFullName(request.fullName()); user.setPhone(request.phone()); return response(user); }
    @Transactional public UserResponse uploadAvatar(UserPrincipal principal, MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024 || !("image/jpeg".equals(file.getContentType()) || "image/png".equals(file.getContentType()) || "image/webp".equals(file.getContentType()))) throw new BusinessRuleException("INVALID_AVATAR", "Avatar must be a JPEG, PNG, or WebP image up to 5 MB");
        try { User user = user(principal.id()); user.setAvatarKey(storage.store("avatars/" + user.getId() + "/" + UUID.randomUUID(), file.getInputStream(), file.getSize(), file.getContentType())); return response(user); } catch (IOException e) { throw new IllegalArgumentException("Could not read avatar", e); }
    }
    @Transactional public void delete(UserPrincipal principal) { User user = user(principal.id()); user.setStatus(UserStatus.DELETED); }
    private User user(Long id) { return users.findById(id).orElseThrow(() -> new NotFoundException("User not found")); }
    private UserResponse response(User user) { return new UserResponse(user.getId(), user.getEmail(), user.getPhone(), user.getFullName(), user.getAvatarKey(), user.getRole(), user.getStatus(), user.getEmailVerifiedAt()); }
}
