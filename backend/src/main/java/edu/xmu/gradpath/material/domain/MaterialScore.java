package edu.xmu.gradpath.material.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 材料分值裁决结果实体
 * 表示针对某一份材料（某一版本）的一次分值裁决结果
 */
@Entity
@Table(name = "gp_material_score")
public class MaterialScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 对应的材料 ID
     */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /**
     * 材料版本号
     */
    @Column(name = "material_version", nullable = false)
    private Integer materialVersion;

    /**
     * 学生申报分值
     */
    @Column(name = "declared_score", nullable = false)
    private Double declaredScore;

    /**
     * 审核确认分值
     */
    @Column(name = "approved_score", nullable = false)
    private Double approvedScore;

    /**
     * 审核人 ID
     */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /**
     * 审核说明 / 修正理由
     */
    @Column(columnDefinition = "text")
    private String comment;

    /**
     * 裁决时间
     */
    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    protected MaterialScore() {
        // JPA only
    }

    /**
     * 构造器：创建 MaterialScore
     */
    public MaterialScore(Long materialId, Integer materialVersion, Double declaredScore, 
                        Double approvedScore, Long reviewerId, String comment) {
        this.materialId = materialId;
        this.materialVersion = materialVersion;
        this.declaredScore = declaredScore;
        this.approvedScore = approvedScore;
        this.reviewerId = reviewerId;
        this.comment = comment;
        this.decidedAt = LocalDateTime.now();
    }

    // ===== getter =====

    public Long getId() {
        return id;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public Integer getMaterialVersion() {
        return materialVersion;
    }

    public Double getDeclaredScore() {
        return declaredScore;
    }

    public Double getApprovedScore() {
        return approvedScore;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
}
