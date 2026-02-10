package edu.xmu.gradpath.application.domain;

public enum ApplicationStatus {

    /**
     * 草稿：学生可反复修改，未进入审核流程
     */
    DRAFT,

    /**
     * 已提交：学生确认提交，等待审核
     */
    SUBMITTED,

    /**
     * 审核中
     */
    UNDER_REVIEW,

    /**
     * 审核通过
     */
    APPROVED,

    /**
     * 审核拒绝
     */
    REJECTED
}
