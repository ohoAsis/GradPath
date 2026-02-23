package edu.xmu.gradpath.review.controller.dto;

import edu.xmu.gradpath.review.domain.ReviewDecision;

/**
 * 创建审核记录请求 DTO
 */
public class CreateReviewRequest {

    /**
     * 审核员 ID
     */
    private Long reviewerId;

    /**
     * 审核决策
     */
    private ReviewDecision decision;

    /**
     * 审核意见
     */
    private String comment;

    /**
     * 材料版本（可选）
     */
    private Integer materialVersion;

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public ReviewDecision getDecision() {
        return decision;
    }

    public void setDecision(ReviewDecision decision) {
        this.decision = decision;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getMaterialVersion() {
        return materialVersion;
    }

    public void setMaterialVersion(Integer materialVersion) {
        this.materialVersion = materialVersion;
    }
}
