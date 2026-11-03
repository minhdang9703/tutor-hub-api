package com.tutorhub.parent.service;

import com.tutorhub.common.exception.ForbiddenException;
import com.tutorhub.common.exception.NotFoundException;
import com.tutorhub.common.security.UserPrincipal;
import com.tutorhub.parent.dto.ParentProfileRequest;
import com.tutorhub.parent.dto.ParentProfileResponse;
import com.tutorhub.parent.entity.ParentProfile;
import com.tutorhub.parent.repository.ParentProfileRepository;
import com.tutorhub.user.entity.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParentProfileService {
    private final ParentProfileRepository profiles;
    public ParentProfileService(ParentProfileRepository profiles) { this.profiles = profiles; }
    @Transactional(readOnly = true) public ParentProfileResponse get(UserPrincipal principal) { return response(profile(principal)); }
    @Transactional public ParentProfileResponse update(UserPrincipal principal, ParentProfileRequest request) { ParentProfile profile = profile(principal); profile.setAddress(request.address()); profile.setDistrictId(request.districtId()); return response(profile); }
    private ParentProfile profile(UserPrincipal principal) { if (principal.role() != Role.PARENT) throw new ForbiddenException("Parent role required"); return profiles.findById(principal.id()).orElseThrow(() -> new NotFoundException("Parent profile not found")); }
    private ParentProfileResponse response(ParentProfile profile) { return new ParentProfileResponse(profile.getUserId(), profile.getAddress(), profile.getDistrictId()); }
}
