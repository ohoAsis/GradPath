package edu.xmu.gradpath.application.controller.dto;

import java.util.List;

/**
 * 申请提交校验解释模型
 */
public class ApplicationSubmissionCheckSummary {

    private Long applicationId;
    private Boolean canSubmit;
    private List<SubmissionCheckItem> checks;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Boolean getCanSubmit() {
        return canSubmit;
    }

    public void setCanSubmit(Boolean canSubmit) {
        this.canSubmit = canSubmit;
    }

    public List<SubmissionCheckItem> getChecks() {
        return checks;
    }

    public void setChecks(List<SubmissionCheckItem> checks) {
        this.checks = checks;
    }

    /**
     * 提交校验项
     */
    public static class SubmissionCheckItem {
        private SubmissionCheckType checkType;
        private Boolean passed;
        private String reason;

        public SubmissionCheckType getCheckType() {
            return checkType;
        }

        public void setCheckType(SubmissionCheckType checkType) {
            this.checkType = checkType;
        }

        public Boolean getPassed() {
            return passed;
        }

        public void setPassed(Boolean passed) {
            this.passed = passed;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 提交校验类型枚举
     */
    public enum SubmissionCheckType {
        STATUS_IS_DRAFT,       // 状态是否为草稿
        HAS_AT_LEAST_ONE_MATERIAL, // 是否至少有一个材料
        ACTION_ALLOWED         // 动作是否被允许
    }
}
