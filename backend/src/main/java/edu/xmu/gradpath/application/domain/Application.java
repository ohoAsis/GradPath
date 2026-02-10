package edu.xmu.gradpath.application.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gp_application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 申请人标识（当前阶段用 userId 占位）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 申请状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Application() {
        // JPA only
    }

    /**
     * 工厂方法：创建 Application 草稿
     */
    public static Application createDraft(Long userId) {
        Application application = new Application();
        application.userId = userId;
        application.status = ApplicationStatus.DRAFT;
        application.createdAt = LocalDateTime.now();
        application.updatedAt = LocalDateTime.now();
        return application;
    }

    public void markSubmitted() {
        this.status = ApplicationStatus.SUBMITTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markApproved() {
        this.status = ApplicationStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markRejected() {
        this.status = ApplicationStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }
    /**
     * 标记申请进入审核中状态
     * 状态跃迁：SUBMITTED -> UNDER_REVIEW
     */
    public void markUnderReview() {
        this.status = ApplicationStatus.UNDER_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }
    

    // ===== getter =====

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}