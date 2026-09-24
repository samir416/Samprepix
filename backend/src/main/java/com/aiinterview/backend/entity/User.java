package com.aiinterview.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @JsonIgnore
    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthenticationProvider provider =
            AuthenticationProvider.EMAIL;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    @Column(length = 255)
    private String profilePicturePublicId;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus accountStatus =
            AccountStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(length = 4)
    private String otp;

    @JsonIgnore
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    private UserProfile profile;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<InterviewSession> interviewSessions;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Subscription> subscriptions;

    private LocalDateTime otpExpiry;

    @Column
    private LocalDateTime lastNotificationsReadAt;

    @Column
    private LocalDateTime dismissedAllNotificationsBefore;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_dismissed_notifications",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "notification_id")
    private Set<String> dismissedNotificationIds =
            new HashSet<>();

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (role == null) {
            role = Role.USER;
        }

        if (provider == null) {
            provider = AuthenticationProvider.EMAIL;
        }

        if (accountStatus == null) {
            accountStatus = AccountStatus.PENDING;
        }

        if (dismissedNotificationIds == null) {
            dismissedNotificationIds = new HashSet<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AuthenticationProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthenticationProvider provider) {
        this.provider = provider;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePicturePublicId() {
        return profilePicturePublicId;
    }

    public void setProfilePicturePublicId(
            String profilePicturePublicId
    ) {
        this.profilePicturePublicId = profilePicturePublicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(
            AccountStatus accountStatus
    ) {
        this.accountStatus = accountStatus;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(
            LocalDateTime otpExpiry
    ) {
        this.otpExpiry = otpExpiry;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public List<InterviewSession> getInterviewSessions() {
        return interviewSessions;
    }

    public void setInterviewSessions(
            List<InterviewSession> interviewSessions
    ) {
        this.interviewSessions = interviewSessions;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(
            List<Subscription> subscriptions
    ) {
        this.subscriptions = subscriptions;
    }

    public LocalDateTime getLastNotificationsReadAt() {
        return lastNotificationsReadAt;
    }

    public void setLastNotificationsReadAt(
            LocalDateTime lastNotificationsReadAt
    ) {
        this.lastNotificationsReadAt =
                lastNotificationsReadAt;
    }

    public LocalDateTime getDismissedAllNotificationsBefore() {
        return dismissedAllNotificationsBefore;
    }

    public void setDismissedAllNotificationsBefore(
            LocalDateTime dismissedAllNotificationsBefore
    ) {
        this.dismissedAllNotificationsBefore =
                dismissedAllNotificationsBefore;
    }

    public Set<String> getDismissedNotificationIds() {
        if (dismissedNotificationIds == null) {
            dismissedNotificationIds = new HashSet<>();
        }

        return dismissedNotificationIds;
    }

    public void setDismissedNotificationIds(
            Set<String> dismissedNotificationIds
    ) {
        this.dismissedNotificationIds =
                dismissedNotificationIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(
            LocalDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }
}