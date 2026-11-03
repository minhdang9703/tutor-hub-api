package com.tutorhub.parent.entity;

import com.tutorhub.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "parent_profiles")
public class ParentProfile {
    @Id private Long userId;
    @MapsId @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    private String address;
    @Column(name = "district_id") private Integer districtId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    public void setUser(User user) { this.user = user; }
    public Long getUserId() { return userId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
}
