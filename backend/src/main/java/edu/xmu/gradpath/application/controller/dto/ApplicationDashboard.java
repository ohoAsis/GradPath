package edu.xmu.gradpath.application.controller.dto;

import edu.xmu.gradpath.application.domain.ApplicationStatus;
import java.time.LocalDateTime;

/**
 * 申请统一进度解释模型
 * 聚合现有解释模型，方便前端/演示一次性获取申请全貌
 */
public class ApplicationDashboard {

    private Long applicationId;
    private ApplicationStatus status;
    private ApplicationReviewSummary reviewSummary;
    private ApplicationScoreSummary scoreSummary;
    private ApplicationSubmissionCheckSummary submissionCheck;
    private LocalDateTime generatedAt;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationReviewSummary getReviewSummary() {
        return reviewSummary;
    }

    public void setReviewSummary(ApplicationReviewSummary reviewSummary) {
        this.reviewSummary = reviewSummary;
    }

    public ApplicationScoreSummary getScoreSummary() {
        return scoreSummary;
    }

    public void setScoreSummary(ApplicationScoreSummary scoreSummary) {
        this.scoreSummary = scoreSummary;
    }

    public ApplicationSubmissionCheckSummary getSubmissionCheck() {
        return submissionCheck;
    }

    public void setSubmissionCheck(ApplicationSubmissionCheckSummary submissionCheck) {
        this.submissionCheck = submissionCheck;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
