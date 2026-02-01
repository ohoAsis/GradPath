package edu.xmu.gradpath.review.service;

import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.exception.BizException;
import edu.xmu.gradpath.material.domain.Material;
import edu.xmu.gradpath.material.repository.MaterialRepository;
import edu.xmu.gradpath.review.domain.ReviewDecision;
import edu.xmu.gradpath.review.domain.ReviewRecord;
import edu.xmu.gradpath.review.repository.ReviewRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ReviewService {

    private final ApplicationService applicationService;
    private final MaterialRepository materialRepository;
    private final ReviewRecordRepository reviewRecordRepository;

    public ReviewService(ApplicationService applicationService, MaterialRepository materialRepository, ReviewRecordRepository reviewRecordRepository) {
        this.applicationService = applicationService;
        this.materialRepository = materialRepository;
        this.reviewRecordRepository = reviewRecordRepository;
    }

    /**
     * 启动对 Application 的审核流程
     *
     * @param applicationId 被审核的申请 id
     */
    @Transactional
    public void startReview(Long applicationId) {
        // 审核域发起动作，但状态机规则由 ApplicationService 统一控制
        applicationService.startReview(applicationId);
    }

    /**
     * 对 Application 给出一次审核结论
     *
     * @param applicationId 被审核的申请 id
     * @param approved      是否通过
     */
    @Transactional
    public void review(Long applicationId, boolean approved) {
        applicationService.review(applicationId, approved);
    }

    /**
     * 创建并保存 ReviewRecord
     * @param applicationId 申请 ID
     * @param materialId 材料 ID
     * @param reviewerId 审核员 ID
     * @param decision 审核决策
     * @param comment 审核意见
     * @return 保存后的 ReviewRecord
     */
    @Transactional
    public ReviewRecord createReviewRecord(Long applicationId, Long materialId, Long reviewerId, ReviewDecision decision, String comment) {
        // 校验 Application 是否存在
        applicationService.getById(applicationId);

        // 校验 Material 是否存在
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BizException(404, "material not found"));

        // 校验 Material 属于该 Application
        if (!material.getApplicationId().equals(applicationId)) {
            throw new BizException(400, "material does not belong to this application");
        }

        // 创建并保存 ReviewRecord
        ReviewRecord reviewRecord = new ReviewRecord(materialId, reviewerId, decision, comment);
        return reviewRecordRepository.save(reviewRecord);
    }

    /**
     * 查询指定 Material 的 ReviewRecord 列表
     * @param materialId 材料 ID
     * @return ReviewRecord 列表
     */
    public List<ReviewRecord> getByMaterialId(Long materialId) {
        // 校验 Material 是否存在
        materialRepository.findById(materialId)
                .orElseThrow(() -> new BizException(404, "material not found"));

        // 查询并返回 ReviewRecord 列表
        return reviewRecordRepository.findByMaterialId(materialId);
    }
}

