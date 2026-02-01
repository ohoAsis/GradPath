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

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final MaterialRepository materialRepository;
    private final ReviewRecordRepository reviewRecordRepository;

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

        // 标记是否所有 Material 都有审核记录
        boolean allReviewed = true;

        // 遍历每个 Material
        for (Material material : materials) {
            // 查询该 Material 的 ReviewRecord 列表
            List<ReviewRecord> reviewRecords = reviewRecordRepository.findByMaterialId(material.getId());

            // 若 ReviewRecord 为空
            if (reviewRecords.isEmpty()) {
                allReviewed = false;
                continue;
            }

            // 只要发现任意 decision == REJECT
            for (ReviewRecord reviewRecord : reviewRecords) {
                if (reviewRecord.getDecision() == ReviewDecision.REJECT) {
                    // 标记为 REJECT
                    application.markRejected();
                    applicationRepository.save(application);
                    return;
                }
            }
        }

        // 若存在任意 Material 的 ReviewRecord 为空 → return
        if (!allReviewed) {
            return;
        }

        // 满足"全部有审核且全部 PASS"
        application.markApproved();
        applicationRepository.save(application);
    }
}

