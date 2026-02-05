package edu.xmu.gradpath.application.controller.dto;

import edu.xmu.gradpath.application.domain.ApplicationStatus;

import java.util.List;

/**
 * 审核解释结果视图对象
 */
public class ApplicationReviewSummary {

    private Long applicationId;
    private ApplicationStatus applicationStatus;
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
        private String reviewResult; // ALL_PASS / HAS_REJECT / INCOMPLETE / CONFLICT

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

        public String getReviewResult() {
            return reviewResult;
        }

        public void setReviewResult(String reviewResult) {
            this.reviewResult = reviewResult;
        }
    }
}
