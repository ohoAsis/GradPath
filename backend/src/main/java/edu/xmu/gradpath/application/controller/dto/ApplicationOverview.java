package edu.xmu.gradpath.application.controller.dto;

import edu.xmu.gradpath.application.domain.ApplicationStatus;

/**
 * Application 全局视角读模型
 */
public class ApplicationOverview {

    private Long applicationId;
    private ApplicationStatus applicationStatus;
    private ApplicationLifecycleSummary.ApplicationStage stage;
    private ApplicationReviewSummary.ApplicationConclusion overallConclusion;

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

    public ApplicationLifecycleSummary.ApplicationStage getStage() {
        return stage;
    }

    public void setStage(ApplicationLifecycleSummary.ApplicationStage stage) {
        this.stage = stage;
    }

    public ApplicationReviewSummary.ApplicationConclusion getOverallConclusion() {
        return overallConclusion;
    }

    public void setOverallConclusion(ApplicationReviewSummary.ApplicationConclusion overallConclusion) {
        this.overallConclusion = overallConclusion;
    }
}
