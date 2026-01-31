package edu.xmu.gradpath.review.controller.dto;

/**
 * 审核请求 DTO
 */
public class ReviewRequest {

    /**
     * 是否通过
     * true  -> APPROVED
     * false -> REJECTED
     */
    private boolean approved;

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
