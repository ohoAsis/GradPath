package edu.xmu.gradpath.application.controller.dto;

import edu.xmu.gradpath.application.domain.ApplicationStatus;
import java.util.List;

/**
 * 申请生命周期语义解释模型
 */
public class ApplicationLifecycleSummary {

    private Long applicationId;
    private ApplicationStatus applicationStatus;
    private ApplicationReviewSummary.ApplicationConclusion overallConclusion;
    private ApplicationStage stage;
    private List<ApplicationAction> allowedActions;
    private List<BlockedAction> blockedActions;

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

    public ApplicationReviewSummary.ApplicationConclusion getOverallConclusion() {
        return overallConclusion;
    }

    public void setOverallConclusion(ApplicationReviewSummary.ApplicationConclusion overallConclusion) {
        this.overallConclusion = overallConclusion;
    }

    public ApplicationStage getStage() {
        return stage;
    }

    public void setStage(ApplicationStage stage) {
        this.stage = stage;
    }

    public List<ApplicationAction> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<ApplicationAction> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public List<BlockedAction> getBlockedActions() {
        return blockedActions;
    }

    public void setBlockedActions(List<BlockedAction> blockedActions) {
        this.blockedActions = blockedActions;
    }

    /**
     * 被阻止的动作
     */
    public static class BlockedAction {
        private ApplicationAction action;
        private String reason;

        public BlockedAction(ApplicationAction action, String reason) {
            this.action = action;
            this.reason = reason;
        }

        public ApplicationAction getAction() {
            return action;
        }

        public void setAction(ApplicationAction action) {
            this.action = action;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 申请阶段枚举
     */
    public enum ApplicationStage {
        DRAFTING,  // 草稿阶段
        SUBMISSION, // 提交阶段
        REVIEWING,  // 审核阶段
        FINALIZED   // 最终阶段
    }

    /**
     * 申请动作枚举
     */
    public enum ApplicationAction {
        ADD_MATERIAL,         // 添加材料
        REMOVE_MATERIAL,      // 移除材料
        SUBMIT_APPLICATION,   // 提交申请
        CREATE_REVIEW,        // 创建审核
        VIEW_RESULT           // 查看结果
    }
}
