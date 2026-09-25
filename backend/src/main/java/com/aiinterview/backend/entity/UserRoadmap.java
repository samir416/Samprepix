package com.aiinterview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_roadmap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "track_id", nullable = false, length = 50)
    private String trackId;

    @Column(name = "track_title", nullable = false, length = 100)
    private String trackTitle;

    @Column(name = "detected_track", length = 100)
    private String detectedTrack;

    private int totalMilestones;
    private int completedMilestonesCount;
    private int xpEarned;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String completedMilestoneIdsJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String roadmapPhasesJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
