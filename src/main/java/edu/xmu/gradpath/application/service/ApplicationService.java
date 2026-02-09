package edu.xmu.gradpath.application.service;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.repository.ApplicationRepository;
import edu.xmu.gradpath.common.exception.BizException;
import edu.xmu.gradpath.application.domain.ApplicationStatus;
import edu.xmu.gradpath.material.domain.Material;
import edu.xmu.gradpath.material.repository.MaterialRepository;
import edu.xmu.gradpath.review.domain.ReviewDecision;
import edu.xmu.gradpath.review.domain.ReviewRecord;
import edu.xmu.gradpath.review.repository.ReviewRecordRepository;
import edu.xmu.gradpath.application.controller.dto.ApplicationReviewSummary;
import edu.xmu.gradpath.application.controller.dto.ApplicationLifecycleSummary;
import edu.xmu.gradpath.application.controller.dto.ApplicationOverview;
import edu.xmu.gradpath.application.controller.dto.ApplicationSubmissionCheckSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
@Service
public class ApplicationService {

    /**
     * 审核聚合结果枚举
     * 仅供 Service 内部使用，不暴露给外部
     */
    private enum ReviewAggregationResult {

        /**
         * 不完整：当前审核事实不足以支持确定性结论
         */
        INCOMPLETE,

        /**
         * 全部通过：至少存在一条 ReviewRecord，且所有 decision = PASS
         */
        ALL_PASS,

        /**
         * 存在拒绝：存在任意一条 decision = REJECT
         */
        HAS_REJECT,

        /**
         * 冲突：同时存在 PASS 与 REJECT，且当前规则未定义裁决方式
         */
        CONFLICT
    }

    private final ApplicationRepository applicationRepository;
    private final MaterialRepository materialRepository;
    private final ReviewRecordRepository reviewRecordRepository;

    /**
     * 最小审核员数量
     */
    private static final int MIN_REVIEWERS = 2;

    /**
     * Material 不完整原因枚举
     */
    private enum MaterialIncompleteReason {
        NO_REVIEWS,            // 折叠后 reviewerCount == 0
        NOT_ENOUGH_REVIEWERS   // 0 < reviewerCount < MIN_REVIEWERS
    }

    /**
     * 审核员角色枚举
     */
    private enum ReviewerRole {
        NORMAL, ARBITER
    }

    /**
     * 审核聚合信息
     */
    private static class ReviewAggregationInfo {
        private final ReviewAggregationResult result;
        private final MaterialIncompleteReason incompleteReason;
        private final int reviewerCount;

        public ReviewAggregationInfo(ReviewAggregationResult result, MaterialIncompleteReason incompleteReason, int reviewerCount) {
            this.result = result;
            this.incompleteReason = incompleteReason;
            this.reviewerCount = reviewerCount;
        }

        public ReviewAggregationResult getResult() {
            return result;
        }

        public MaterialIncompleteReason getIncompleteReason() {
            return incompleteReason;
        }

        public int getReviewerCount() {
            return reviewerCount;
        }
    }

    /**
     * Application 审核阻塞原因枚举
     */
    private enum ApplicationReviewBlockReason {
        INSUFFICIENT_REVIEWERS,
        CONFLICTING_REVIEWS
    }

    /**
     * 仲裁审核员集合
     */
    private static final java.util.Set<Long> ARBITER_REVIEWER_IDS = java.util.Set.of(1L);

    /**
     * 获取审核员角色
     * @param reviewerId 审核员 ID
     * @return ReviewerRole
     */
    private ReviewerRole getReviewerRole(Long reviewerId) {
        if (ARBITER_REVIEWER_IDS.contains(reviewerId)) {
            return ReviewerRole.ARBITER;
        }
        return ReviewerRole.NORMAL;
    }

    public ApplicationService(ApplicationRepository applicationRepository, MaterialRepository materialRepository, ReviewRecordRepository reviewRecordRepository) {
        this.applicationRepository = applicationRepository;
        this.materialRepository = materialRepository;
        this.reviewRecordRepository = reviewRecordRepository;
    }

    /**
     * 创建 Application 草稿
     * 业务约束：
     * - 同一用户只能存在一个 DRAFT
     */
    @Transactional
    public Long createDraft(Long userId) {

        applicationRepository
                .findByUserIdAndStatus(userId, ApplicationStatus.DRAFT)
                .ifPresent(existing -> {
                    throw new BizException(
                            400,
                            "draft application already exists"
                    );
                });

        Application draft = Application.createDraft(userId);
        applicationRepository.save(draft);
        return draft.getId();
    }

    /**
     * 提交申请：DRAFT -> SUBMITTED
     */
    @Transactional
    public Long submit(Long userId, Long applicationId) {

        Application application = getById(applicationId);

        // MVP 阶段：不做完整鉴权，但至少防止提交他人申请
        if (!application.getUserId().equals(userId)) {
            throw new BizException(403, "not your application");
        }

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new BizException(
                    400,
                    "only draft application can be submitted"
            );
        }

        application.markSubmitted();
        applicationRepository.save(application);

        return application.getId();
    }

    /**
     * 启动审核流程：SUBMITTED -> UNDER_REVIEW
     * 说明：
     * - 这是 v2 新增动作
     * - 表示审核正式开始，但尚未给出结论
     */
    @Transactional
    public void startReview(Long applicationId) {

        Application application = getById(applicationId);

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new BizException(
                    400,
                    "only submitted application can start review"
            );
        }

        application.markUnderReview();
        applicationRepository.save(application);
    }

    /**
     * 给出审核结论：UNDER_REVIEW -> APPROVED / REJECTED
     *
     * @param approved 是否通过
     */
    @Transactional
    public void review(Long applicationId, boolean approved) {

        Application application = getById(applicationId);

        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BizException(
                    400,
                    "only under_review application can be reviewed"
            );
        }

        applyReviewResult(application, approved);

        applicationRepository.save(application);
    }

    /**
     * v3 扩展点（当前行为不变）
     * 说明：
     * - v2：只负责修改 Application 状态
     * - v3：将在此处引入审核记录、轮次判断等逻辑
     */
    private void applyReviewResult(Application application, boolean approved) {
        if (approved) {
            application.markApproved();
        } else {
            application.markRejected();
        }
    }

    /**
     * 聚合某个 Material 的 ReviewRecord 语义
     * @param reviewRecords ReviewRecord 列表
     * @param materialVersion Material 当前版本
     * @return ReviewAggregationInfo
     */
    private ReviewAggregationInfo aggregateReviewResults(List<ReviewRecord> reviewRecords, Integer materialVersion) {
        // 过滤出 materialVersion == material.version 的记录
        List<ReviewRecord> filteredRecords = reviewRecords.stream()
                .filter(record -> Objects.equals(record.getMaterialVersion(), materialVersion))
                .toList();

        // 对同一审核员在同一 MaterialVersion 下的多条 ReviewRecord 做折叠（last-write-wins）
        List<ReviewRecord> foldedRecords = filteredRecords.stream()
                // 对 reviewerId 做非空保护
                .filter(record -> {
                    if (record.getReviewerId() == null) {
                        throw new BizException(400, "reviewerId missing in review record");
                    }
                    return true;
                })
                // 按 reviewerId 分组
                .collect(java.util.stream.Collectors.groupingBy(ReviewRecord::getReviewerId))
                // 每组只保留 createdAt 最新的一条
                .values().stream()
                .flatMap(group -> group.stream()
                        .max(java.util.Comparator.comparing(ReviewRecord::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                        .stream())
                .toList();

        // reviewerCount = foldedRecords.size()
        int reviewerCount = foldedRecords.size();

        // 仲裁优先逻辑
        List<ReviewRecord> arbiterRecords = foldedRecords.stream()
                .filter(record -> getReviewerRole(record.getReviewerId()) == ReviewerRole.ARBITER)
                .toList();

        if (!arbiterRecords.isEmpty()) {
            // 取 arbiterRecords 中 createdAt 最新的一条
            ReviewRecord latestArbiterRecord = arbiterRecords.stream()
                    .max(java.util.Comparator.comparing(
                            ReviewRecord::getCreatedAt,
                            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
                    ))
                    .orElseThrow();

            if (latestArbiterRecord != null) {
                // 仲裁存在时，不再进入 MIN_REVIEWERS 与 CONFLICT 判定逻辑（仲裁直接裁决）
                if (latestArbiterRecord.getDecision() == ReviewDecision.REJECT) {
                    return new ReviewAggregationInfo(ReviewAggregationResult.HAS_REJECT, null, reviewerCount);
                } else {
                    return new ReviewAggregationInfo(ReviewAggregationResult.ALL_PASS, null, reviewerCount);
                }
            }
        }

        // 返回规则
        if (reviewerCount == 0) {
            return new ReviewAggregationInfo(ReviewAggregationResult.INCOMPLETE, MaterialIncompleteReason.NO_REVIEWS, reviewerCount);
        } else if (reviewerCount < MIN_REVIEWERS) {
            return new ReviewAggregationInfo(ReviewAggregationResult.INCOMPLETE, MaterialIncompleteReason.NOT_ENOUGH_REVIEWERS, reviewerCount);
        } else {
            boolean hasPass = false;
            boolean hasReject = false;

            // 检查是否存在 PASS 和 REJECT
            for (ReviewRecord reviewRecord : foldedRecords) {
                if (reviewRecord.getDecision() == ReviewDecision.PASS) {
                    hasPass = true;
                } else if (reviewRecord.getDecision() == ReviewDecision.REJECT) {
                    hasReject = true;
                }
            }

            // 同时存在 PASS 和 REJECT → CONFLICT
            if (hasPass && hasReject) {
                return new ReviewAggregationInfo(ReviewAggregationResult.CONFLICT, null, reviewerCount);
            }

            // 存在 REJECT → HAS_REJECT
            if (hasReject) {
                return new ReviewAggregationInfo(ReviewAggregationResult.HAS_REJECT, null, reviewerCount);
            }

            // 否则 → ALL_PASS
            return new ReviewAggregationInfo(ReviewAggregationResult.ALL_PASS, null, reviewerCount);
        }
    }

    /**
     * 查询 Application（只读）
     * 说明：
     * - 这是 Application 的唯一取数入口
     * - 不做任何业务判断
     */
    public Application getById(Long applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new BizException(404, "application not found")
                );
    }

    /**
     * 评估审核结果并更新 Application 状态
     * @param applicationId 申请 ID
     */
    @Transactional
    public void evaluateAfterReview(Long applicationId) {
        // 加载 Application
        Application application = getById(applicationId);

        // 如果当前 Application.status == SUBMITTED 且该 Application 已存在至少一条 ReviewRecord，迁移为 UNDER_REVIEW
        if (application.getStatus() == ApplicationStatus.SUBMITTED) {
            // 检查该 Application 是否存在至少一条 ReviewRecord
            List<Material> materials = materialRepository.findByApplicationId(applicationId);
            boolean hasReviewRecord = false;
            
            for (Material material : materials) {
                List<ReviewRecord> reviewRecords = reviewRecordRepository.findByMaterialId(material.getId());
                if (!reviewRecords.isEmpty()) {
                    hasReviewRecord = true;
                    break;
                }
            }
            
            if (hasReviewRecord) {
                application.markUnderReview();
                applicationRepository.save(application);
            } else {
                return;
            }
        } else if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            return;
        }

        // 查询 Application 下的所有 Material
        List<Material> materials = materialRepository.findByApplicationId(applicationId);

        // 若为空 → return
        if (materials.isEmpty()) {
            return;
        }

        // 标记是否所有 Material 都为 ALL_PASS
        boolean allAllPass = true;

        // 遍历每个 Material，收集 ReviewAggregationInfo
        List<ReviewAggregationInfo> aggregationInfos = new java.util.ArrayList<>();

        for (Material material : materials) {
            // 查询该 Material 的 ReviewRecord 列表
            List<ReviewRecord> reviewRecords = reviewRecordRepository.findByMaterialId(material.getId());

            // 聚合 ReviewRecord 语义
            ReviewAggregationInfo info = aggregateReviewResults(reviewRecords, material.getVersion());
            aggregationInfos.add(info);

            // 任意 Material 聚合结果为 HAS_REJECT → Application = REJECTED
            if (info.getResult() == ReviewAggregationResult.HAS_REJECT) {
                application.markRejected();
                applicationRepository.save(application);
                return;
            }

            // 检查是否所有 Material 都为 ALL_PASS
            if (info.getResult() != ReviewAggregationResult.ALL_PASS) {
                allAllPass = false;
            }
        }

        // 当且仅当所有 Material 聚合结果为 ALL_PASS → Application = APPROVED
        if (allAllPass) {
            application.markApproved();
            applicationRepository.save(application);
        } else {
            // 计算内部阻塞原因
            ApplicationReviewBlockReason blockReason = null;

            // 优先级：冲突优先于人数不足
            boolean hasConflict = aggregationInfos.stream()
                    .anyMatch(info -> info.getResult() == ReviewAggregationResult.CONFLICT);

            boolean hasIncomplete = aggregationInfos.stream()
                    .anyMatch(info -> info.getResult() == ReviewAggregationResult.INCOMPLETE);

            if (hasConflict) {
                blockReason = ApplicationReviewBlockReason.CONFLICTING_REVIEWS;
            } else if (hasIncomplete) {
                blockReason = ApplicationReviewBlockReason.INSUFFICIENT_REVIEWERS;
            }

            // 记录阻塞原因（用于未来扩展，不落库）
            // 可以在这里添加日志输出
            // logger.info("Application {} under review due to: {}", applicationId, blockReason);
        }

        // 其他情况 → Application 保持 UNDER_REVIEW
    }

    /**
     * 获取审核解释结果
     * @param applicationId 申请 ID
     * @return 审核解释结果视图对象
     */
    public ApplicationReviewSummary getReviewSummary(Long applicationId) {
        // 加载 Application
        Application application = getById(applicationId);

        // 查询 Application 下的所有 Material
        List<Material> materials = materialRepository.findByApplicationId(applicationId);

        // 构建审核解释结果
        ApplicationReviewSummary summary = new ApplicationReviewSummary();
        summary.setApplicationId(applicationId);
        summary.setApplicationStatus(application.getStatus());

        // 构建材料审核解释结果列表
        List<ApplicationReviewSummary.MaterialReviewSummary> materialSummaries = new java.util.ArrayList<>();

        for (Material material : materials) {
            // 查询该 Material 的 ReviewRecord 列表
            List<ReviewRecord> reviewRecords = reviewRecordRepository.findByMaterialId(material.getId());

            // 聚合 ReviewRecord 语义
            ReviewAggregationInfo info = aggregateReviewResults(reviewRecords, material.getVersion());

            // 构建材料审核解释结果
            ApplicationReviewSummary.MaterialReviewSummary materialSummary = new ApplicationReviewSummary.MaterialReviewSummary();
            materialSummary.setMaterialId(material.getId());
            materialSummary.setCurrentVersion(material.getVersion());
            
            // 设置 aggregationResult
            ApplicationReviewSummary.AggregationResult aggregationResult;
            switch (info.getResult()) {
                case ALL_PASS:
                    aggregationResult = ApplicationReviewSummary.AggregationResult.ALL_PASS;
                    break;
                case HAS_REJECT:
                    aggregationResult = ApplicationReviewSummary.AggregationResult.HAS_REJECT;
                    break;
                case INCOMPLETE:
                    aggregationResult = ApplicationReviewSummary.AggregationResult.INCOMPLETE;
                    break;
                case CONFLICT:
                    aggregationResult = ApplicationReviewSummary.AggregationResult.CONFLICT;
                    break;
                default:
                    aggregationResult = ApplicationReviewSummary.AggregationResult.INCOMPLETE;
            }
            materialSummary.setAggregationResult(aggregationResult);
            
            // 设置 blockingReason
            ApplicationReviewSummary.BlockingReason blockingReason = null;
            if (aggregationResult != ApplicationReviewSummary.AggregationResult.ALL_PASS) {
                if (info.getResult() == ReviewAggregationResult.INCOMPLETE) {
                    blockingReason = ApplicationReviewSummary.BlockingReason.NOT_ENOUGH_REVIEWERS;
                } else if (info.getResult() == ReviewAggregationResult.CONFLICT) {
                    blockingReason = ApplicationReviewSummary.BlockingReason.CONFLICT;
                } else if (info.getResult() == ReviewAggregationResult.HAS_REJECT) {
                    blockingReason = ApplicationReviewSummary.BlockingReason.HAS_REJECT;
                }
            }
            materialSummary.setBlockingReason(blockingReason);
            
            // 设置 effectiveReviewerCount
            materialSummary.setEffectiveReviewerCount(info.getReviewerCount());

            materialSummaries.add(materialSummary);
        }

        summary.setMaterials(materialSummaries);
        
        // 计算 overallConclusion
        ApplicationReviewSummary.ApplicationConclusion overallConclusion;
        boolean hasReject = false;
        boolean allPass = true;
        
        for (ApplicationReviewSummary.MaterialReviewSummary materialSummary : materialSummaries) {
            ApplicationReviewSummary.AggregationResult aggResult = materialSummary.getAggregationResult();
            if (aggResult == ApplicationReviewSummary.AggregationResult.HAS_REJECT) {
                hasReject = true;
                allPass = false;
            } else if (aggResult != ApplicationReviewSummary.AggregationResult.ALL_PASS) {
                allPass = false;
            }
        }
        
        if (hasReject) {
            overallConclusion = ApplicationReviewSummary.ApplicationConclusion.REJECTED;
        } else if (allPass) {
            overallConclusion = ApplicationReviewSummary.ApplicationConclusion.APPROVED;
        } else {
            overallConclusion = ApplicationReviewSummary.ApplicationConclusion.UNDER_REVIEW;
        }
        
        summary.setOverallConclusion(overallConclusion);
        
        return summary;
    }

    /**
     * 推导 ApplicationStage
     * @param status ApplicationStatus
     * @return ApplicationStage
     */
    private ApplicationLifecycleSummary.ApplicationStage deriveStage(ApplicationStatus status) {
        switch (status) {
            case DRAFT:
                return ApplicationLifecycleSummary.ApplicationStage.DRAFTING;
            case SUBMITTED:
                return ApplicationLifecycleSummary.ApplicationStage.SUBMISSION;
            case UNDER_REVIEW:
                return ApplicationLifecycleSummary.ApplicationStage.REVIEWING;
            case APPROVED:
            case REJECTED:
                return ApplicationLifecycleSummary.ApplicationStage.FINALIZED;
            default:
                return ApplicationLifecycleSummary.ApplicationStage.DRAFTING;
        }
    }

    /**
     * 获取申请生命周期语义解释
     * @param applicationId 申请 ID
     * @return 生命周期语义解释视图对象
     */
    public ApplicationLifecycleSummary getLifecycleSummary(Long applicationId) {
        // 加载 Application
        Application application = getById(applicationId);
        ApplicationStatus status = application.getStatus();
        
        // 获取审核解释结果
        ApplicationReviewSummary reviewSummary = getReviewSummary(applicationId);
        
        // 构建生命周期语义解释
        ApplicationLifecycleSummary lifecycleSummary = new ApplicationLifecycleSummary();
        lifecycleSummary.setApplicationId(applicationId);
        lifecycleSummary.setApplicationStatus(status);
        lifecycleSummary.setOverallConclusion(reviewSummary.getOverallConclusion());
        
        // 推导 stage
        ApplicationLifecycleSummary.ApplicationStage stage = deriveStage(status);
        lifecycleSummary.setStage(stage);
        
        // 构建 allowedActions 和 blockedActions
        java.util.List<ApplicationLifecycleSummary.ApplicationAction> allowedActions = new java.util.ArrayList<>();
        java.util.List<ApplicationLifecycleSummary.BlockedAction> blockedActions = new java.util.ArrayList<>();
        
        switch (stage) {
            case DRAFTING:
                // 允许的动作
                allowedActions.add(ApplicationLifecycleSummary.ApplicationAction.ADD_MATERIAL);
                allowedActions.add(ApplicationLifecycleSummary.ApplicationAction.REMOVE_MATERIAL);
                allowedActions.add(ApplicationLifecycleSummary.ApplicationAction.SUBMIT_APPLICATION);
                // 阻止的动作
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.CREATE_REVIEW,
                        "APPLICATION_NOT_SUBMITTED"
                ));
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.VIEW_RESULT,
                        "NO_REVIEW_YET"
                ));
                break;
            case SUBMISSION:
            case REVIEWING:
                // 允许的动作
                allowedActions.add(ApplicationLifecycleSummary.ApplicationAction.CREATE_REVIEW);
                allowedActions.add(ApplicationLifecycleSummary.ApplicationAction.VIEW_RESULT);
                // 阻止的动作
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.ADD_MATERIAL,
                        "MATERIAL_FROZEN_AFTER_SUBMISSION"
                ));
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.REMOVE_MATERIAL,
                        "MATERIAL_FROZEN_AFTER_SUBMISSION"
                ));
                break;
            case FINALIZED:
                // 允许的动作
                allowedActions.add(ApplicationLifecycleSummary.ApplicationAction.VIEW_RESULT);
                // 阻止的动作
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.ADD_MATERIAL,
                        "APPLICATION_FINALIZED"
                ));
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.REMOVE_MATERIAL,
                        "APPLICATION_FINALIZED"
                ));
                blockedActions.add(new ApplicationLifecycleSummary.BlockedAction(
                        ApplicationLifecycleSummary.ApplicationAction.CREATE_REVIEW,
                        "APPLICATION_FINALIZED"
                ));
                break;
        }
        
        lifecycleSummary.setAllowedActions(allowedActions);
        lifecycleSummary.setBlockedActions(blockedActions);
        
        return lifecycleSummary;
    }

    /**
     * 获取所有 Application 的全局视角读模型
     * @return ApplicationOverview 列表
     */
    public java.util.List<ApplicationOverview> getApplicationOverviews() {
        // 查询所有 Application
        List<Application> applications = applicationRepository.findAll();
        
        // 为每个 Application 构建 ApplicationOverview
        return applications.stream().map(application -> {
            ApplicationOverview overview = new ApplicationOverview();
            Long applicationId = application.getId();
            ApplicationStatus status = application.getStatus();
            
            // 设置 applicationId 和 applicationStatus
            overview.setApplicationId(applicationId);
            overview.setApplicationStatus(status);
            
            // 推导 stage（复用已有规则）
            ApplicationLifecycleSummary.ApplicationStage stage = deriveStage(status);
            overview.setStage(stage);
            
            // 获取 overallConclusion（复用已有规则）
            ApplicationReviewSummary reviewSummary = getReviewSummary(applicationId);
            overview.setOverallConclusion(reviewSummary.getOverallConclusion());
            
            return overview;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取申请提交校验解释
     * @param applicationId 申请 ID
     * @return 提交校验解释视图对象
     */
    public ApplicationSubmissionCheckSummary getSubmissionCheckSummary(Long applicationId) {
        // 加载 Application
        Application application = getById(applicationId);
        ApplicationStatus status = application.getStatus();
        
        // 查询该 Application 下的 Material
        List<Material> materials = materialRepository.findByApplicationId(applicationId);
        
        // 获取生命周期语义解释
        ApplicationLifecycleSummary lifecycleSummary = getLifecycleSummary(applicationId);
        
        // 构建提交校验项列表
        java.util.List<ApplicationSubmissionCheckSummary.SubmissionCheckItem> checks = new java.util.ArrayList<>();
        
        // STATUS_IS_DRAFT 检查
        ApplicationSubmissionCheckSummary.SubmissionCheckItem statusCheck = new ApplicationSubmissionCheckSummary.SubmissionCheckItem();
        statusCheck.setCheckType(ApplicationSubmissionCheckSummary.SubmissionCheckType.STATUS_IS_DRAFT);
        boolean isDraft = status == ApplicationStatus.DRAFT;
        statusCheck.setPassed(isDraft);
        if (!isDraft) {
            statusCheck.setReason("APPLICATION_NOT_IN_DRAFT");
        }
        checks.add(statusCheck);
        
        // HAS_AT_LEAST_ONE_MATERIAL 检查
        ApplicationSubmissionCheckSummary.SubmissionCheckItem materialCheck = new ApplicationSubmissionCheckSummary.SubmissionCheckItem();
        materialCheck.setCheckType(ApplicationSubmissionCheckSummary.SubmissionCheckType.HAS_AT_LEAST_ONE_MATERIAL);
        boolean hasMaterial = !materials.isEmpty();
        materialCheck.setPassed(hasMaterial);
        if (!hasMaterial) {
            materialCheck.setReason("NO_MATERIAL_ATTACHED");
        }
        checks.add(materialCheck);
        
        // ACTION_ALLOWED 检查
        ApplicationSubmissionCheckSummary.SubmissionCheckItem actionCheck = new ApplicationSubmissionCheckSummary.SubmissionCheckItem();
        actionCheck.setCheckType(ApplicationSubmissionCheckSummary.SubmissionCheckType.ACTION_ALLOWED);
        boolean actionAllowed = lifecycleSummary.getAllowedActions().contains(
                ApplicationLifecycleSummary.ApplicationAction.SUBMIT_APPLICATION
        );
        actionCheck.setPassed(actionAllowed);
        if (!actionAllowed) {
            actionCheck.setReason("SUBMISSION_NOT_ALLOWED_IN_CURRENT_STAGE");
        }
        checks.add(actionCheck);
        
        // 计算 canSubmit
        boolean canSubmit = checks.stream().allMatch(ApplicationSubmissionCheckSummary.SubmissionCheckItem::getPassed);
        
        // 构建并返回提交校验解释
        ApplicationSubmissionCheckSummary summary = new ApplicationSubmissionCheckSummary();
        summary.setApplicationId(applicationId);
        summary.setCanSubmit(canSubmit);
        summary.setChecks(checks);
        
        return summary;
    }
}

