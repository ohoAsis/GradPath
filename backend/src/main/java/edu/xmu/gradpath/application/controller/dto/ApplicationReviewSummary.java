package edu.xmu.gradpath.application.controller.dto;

import edu.xmu.gradpath.application.domain.ApplicationStatus;
import edu.xmu.gradpath.material.domain.ScoreMode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 审核解释结果视图对象
 */
public class ApplicationReviewSummary {

    private Long applicationId;
    private ApplicationStatus applicationStatus;
    private ApplicationConclusion overallConclusion;
    private List<MaterialReviewSummary> materials;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public ApplicationConclusion getOverallConclusion() {
        return overallConclusion;
    }

    public void setOverallConclusion(ApplicationConclusion overallConclusion) {
        this.overallConclusion = overallConclusion;
    }

    public List<MaterialReviewSummary> getMaterials() {
        return materials;
    }

    public void setMaterials(List<MaterialReviewSummary> materials) {
        this.materials = materials;
    }

    /**
     * 材料审核解释结果
     */
    public static class MaterialReviewSummary {

        private Long materialId;
        private Integer currentVersion;
        private AggregationResult aggregationResult;
        private BlockingReason blockingReason;
        private Integer effectiveReviewerCount;
        private ScoreMode scoreMode;
        private boolean hasScore;
        private BigDecimal approvedScore;
        private boolean canCreateScore;

        public Long getMaterialId() {
            return materialId;
        }

        public void setMaterialId(Long materialId) {
            this.materialId = materialId;
        }

        public Integer getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(Integer currentVersion) {
            this.currentVersion = currentVersion;
        }

        public AggregationResult getAggregationResult() {
            return aggregationResult;
        }

        public void setAggregationResult(AggregationResult aggregationResult) {
            this.aggregationResult = aggregationResult;
        }

        public BlockingReason getBlockingReason() {
            return blockingReason;
        }

        public void setBlockingReason(BlockingReason blockingReason) {
            this.blockingReason = blockingReason;
        }

        public Integer getEffectiveReviewerCount() {
            return effectiveReviewerCount;
        }

        public void setEffectiveReviewerCount(Integer effectiveReviewerCount) {
            this.effectiveReviewerCount = effectiveReviewerCount;
        }

        public ScoreMode getScoreMode() {
            return scoreMode;
        }

        public void setScoreMode(ScoreMode scoreMode) {
            this.scoreMode = scoreMode;
        }

        public boolean isHasScore() {
            return hasScore;
        }

        public void setHasScore(boolean hasScore) {
            this.hasScore = hasScore;
        }

        public BigDecimal getApprovedScore() {
            return approvedScore;
        }

        public void setApprovedScore(BigDecimal approvedScore) {
            this.approvedScore = approvedScore;
        }

        public boolean isCanCreateScore() {
            return canCreateScore;
        }

        public void setCanCreateScore(boolean canCreateScore) {
            this.canCreateScore = canCreateScore;
        }
    }

    /**
     * 审核聚合结果枚举
     */
    public enum AggregationResult {
        ALL_PASS,
        HAS_REJECT,
        INCOMPLETE,
        CONFLICT
    }

    /**
     * 审核阻塞原因枚举
     */
    public enum BlockingReason {
        NOT_ENOUGH_REVIEWERS,
        CONFLICT,
        HAS_REJECT
    }

    /**
     * 应用审核结论枚举
     */
    public enum ApplicationConclusion {
        APPROVED,
        REJECTED,
        UNDER_REVIEW
    }
}
