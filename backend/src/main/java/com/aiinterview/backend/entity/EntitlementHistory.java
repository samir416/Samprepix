package com.aiinterview.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entitlement_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntitlementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String planName;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String grantedBy;

    private String reason;

    private LocalDateTime effectiveAt;

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        effectiveAt = LocalDateTime.now();
    }
}
