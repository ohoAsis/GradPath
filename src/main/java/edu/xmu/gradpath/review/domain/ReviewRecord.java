package edu.xmu.gradpath.review.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gp_review_record")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 材料 ID
     */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /**
     * 审核员 ID
     */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /**
     * 审核决策
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewDecision decision;

    /**
     * 审核意见
     */
    @Column(columnDefinition = "text")
    private String comment;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ReviewRecord() {
        // JPA only
    }

    /**
     * 构造器：创建 ReviewRecord
     */
    public ReviewRecord(Long materialId, Long reviewerId, ReviewDecision decision, String comment) {
        this.materialId = materialId;
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    // ===== getter =====

    public Long getId() {
        return id;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public ReviewDecision getDecision() {
        return decision;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}
