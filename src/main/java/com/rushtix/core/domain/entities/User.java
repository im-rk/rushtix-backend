package com.rushtix.core.domain.entities;

import com.rushtix.core.domain.enums.Role;
import com.rushtix.core.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="Users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="email",nullable=false,unique=true)
    private String email;

    @Column(name = "full_name",nullable=false)
    private String fullName;

    @Column(name="password_hash",nullable=false)
    private String passward_hash;

    @Enumerated(EnumType.STRING)
    @Column(name="role",nullable=false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "reset_token_hash")
    private String resetTokenHash;

    @Column(name = "reset_token_expires")
    private OffsetDateTime resetTokenExpires;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

}
