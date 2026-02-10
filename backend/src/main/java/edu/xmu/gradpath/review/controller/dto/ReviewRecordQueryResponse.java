package edu.xmu.gradpath.review.controller.dto;

import edu.xmu.gradpath.review.domain.ReviewDecision;
import edu.xmu.gradpath.review.domain.ReviewRecord;

import java.time.LocalDateTime;

/**
 * ReviewRecord 查询接口的返回 DTO
 * 只用于"读"，不承载任何业务行为
 */
public class ReviewRecordQueryResponse {

    private Long id;
    private Long reviewerId;
    private ReviewDecision decision;
    private String comment;
    private LocalDateTime createdAt;

    private ReviewRecordQueryResponse() {
    }

    /**
     * 从 ReviewRecord 转换为 ReviewRecordQueryResponse
     */
    public static ReviewRecordQueryResponse from(ReviewRecord reviewRecord) {
        ReviewRecordQueryResponse resp = new ReviewRecordQueryResponse();
        resp.id = reviewRecord.getId();
        resp.reviewerId = reviewRecord.getReviewerId();
        resp.decision = reviewRecord.getDecision();
        resp.comment = reviewRecord.getComment();
        resp.createdAt = reviewRecord.getCreatedAt();
        return resp;
    }

    // ===== getter =====

    public Long getId() {
        return id;
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
