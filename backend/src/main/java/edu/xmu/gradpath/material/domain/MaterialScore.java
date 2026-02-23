package edu.xmu.gradpath.material.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 材料分值裁决结果实体
 * 表示针对某一份材料（某一版本）的一次分值裁决结果
 */
@Entity
@Table(name = "gp_material_score", uniqueConstraints = @UniqueConstraint(columnNames = {"material_id", "material_version"}))
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
     * 审核确认分值
     */
    @Column(name = "approved_score", nullable = false)
    private BigDecimal approvedScore;

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
    public MaterialScore(Long materialId, Integer materialVersion, BigDecimal approvedScore) {
        this.materialId = materialId;
        this.materialVersion = materialVersion;
        this.approvedScore = approvedScore;
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

    public BigDecimal getApprovedScore() {
        return approvedScore;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }
}
