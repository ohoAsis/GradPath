package edu.xmu.gradpath.application.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请分值汇总解释模型
 * 只读，不写库，不改状态
 * 用于查看某个 Application 下各材料当前版本的 approvedScore 并计算总分
 */
public class ApplicationScoreSummary {

    private Long applicationId;
    private List<MaterialScoreItem> items;
    private BigDecimal totalApprovedScore;
    private List<Long> missingScoreMaterialIds;
    private LocalDateTime generatedAt;

    // getter 和 setter
    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public List<MaterialScoreItem> getItems() {
        return items;
    }

    public void setItems(List<MaterialScoreItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalApprovedScore() {
        return totalApprovedScore;
    }

    public void setTotalApprovedScore(BigDecimal totalApprovedScore) {
        this.totalApprovedScore = totalApprovedScore;
    }

    public List<Long> getMissingScoreMaterialIds() {
        return missingScoreMaterialIds;
    }

    public void setMissingScoreMaterialIds(List<Long> missingScoreMaterialIds) {
        this.missingScoreMaterialIds = missingScoreMaterialIds;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * 材料分值项
     */
    public static class MaterialScoreItem {
        private Long materialId;
        private Integer materialVersion;
        private BigDecimal declaredScore;
        private BigDecimal approvedScore;
        private boolean hasScore;

        // getter 和 setter
        public Long getMaterialId() {
            return materialId;
        }

        public void setMaterialId(Long materialId) {
            this.materialId = materialId;
        }

        public Integer getMaterialVersion() {
            return materialVersion;
        }

        public void setMaterialVersion(Integer materialVersion) {
            this.materialVersion = materialVersion;
        }

        public BigDecimal getDeclaredScore() {
            return declaredScore;
        }

        public void setDeclaredScore(BigDecimal declaredScore) {
            this.declaredScore = declaredScore;
        }

        public BigDecimal getApprovedScore() {
            return approvedScore;
        }

        public void setApprovedScore(BigDecimal approvedScore) {
            this.approvedScore = approvedScore;
        }

        public boolean isHasScore() {
            return hasScore;
        }

        public void setHasScore(boolean hasScore) {
            this.hasScore = hasScore;
        }
    }
}
