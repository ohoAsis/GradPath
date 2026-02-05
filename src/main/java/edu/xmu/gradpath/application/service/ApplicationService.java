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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Comparator;

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

        // status != UNDER_REVIEW → 直接 return
        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
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
}

