package edu.xmu.gradpath.material.service;

import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.exception.BizException;
import edu.xmu.gradpath.material.domain.Material;
import edu.xmu.gradpath.material.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import edu.xmu.gradpath.application.domain.ApplicationStatus;
import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.controller.dto.ApplicationReviewSummary;
import edu.xmu.gradpath.material.controller.dto.ReviseMaterialRequest;
import edu.xmu.gradpath.material.domain.ScoreMode;
import java.math.BigDecimal;
import java.util.List;

@Service
public class MaterialService {

    private final ApplicationService applicationService;
    private final MaterialRepository materialRepository;

    public MaterialService(ApplicationService applicationService, MaterialRepository materialRepository) {
        this.applicationService = applicationService;
        this.materialRepository = materialRepository;
    }

    /**
     * 查询指定 Application 下的所有 Material
     * 排序：按 createdAt 升序
     * @param applicationId 申请 ID
     * @return Material 列表
     */
    public List<Material> getByApplicationId(Long applicationId) {
        // 查询 Material 列表
        return materialRepository.findByApplicationId(applicationId);
    }

    /**
     * 为指定 Application 新增一条 Material
     * @param applicationId 申请 ID
     * @param category 材料类别
     * @param content 材料内容描述
     * @param attachmentRef 附件引用
     * @param declaredScore 学生申报分值
     * @param scoreMode 计分模式
     * @return 保存后的 Material
     */
    public Material createMaterial(Long applicationId, String category, String content, String attachmentRef, BigDecimal declaredScore, ScoreMode scoreMode) {
        // 校验 Application 是否存在
        Application application = applicationService.getById(applicationId);

        // 当 Application.status 为 SUBMITTED 或 UNDER_REVIEW 时，禁止新增 Material
        ApplicationStatus status = application.getStatus();
        if (status == ApplicationStatus.SUBMITTED || 
            status == ApplicationStatus.UNDER_REVIEW) {
            throw new BizException(400, "cannot add material when application is submitted or under review");
        }

        // 校验写入规则：content 与 attachmentRef 至少有一个不为空
        if (content == null && attachmentRef == null) {
            throw new BizException(400, "content and attachmentRef cannot be both null");
        }

        // 处理 scoreMode 相关逻辑
        if (scoreMode == ScoreMode.NONE) {
            // 若 scoreMode == NONE：强制 declaredScore == 0
            declaredScore = BigDecimal.ZERO;
        } else if (scoreMode == ScoreMode.DECLARED) {
            // 若 scoreMode == DECLARED：declaredScore 必须非空且 >= 0
            if (declaredScore == null) {
                throw new BizException(400, "declaredScore cannot be null for DECLARED material");
            }
            if (declaredScore.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(400, "declaredScore cannot be negative");
            }
        }

        // 构造 Material
        Material material = new Material(applicationId, category, content, attachmentRef, declaredScore, scoreMode);

        // 保存并返回
        return materialRepository.save(material);
    }

    /**
     * 删除指定 Application 下的一条 Material
     * @param applicationId 申请 ID
     * @param materialId 材料 ID
     */
    public void deleteMaterial(Long applicationId, Long materialId) {
        // 校验 Application 是否存在
        Application application = applicationService.getById(applicationId);

        // 当 Application.status 为 SUBMITTED 或 UNDER_REVIEW 时，禁止删除 Material
        ApplicationStatus status = application.getStatus();
        if (status == ApplicationStatus.SUBMITTED || 
            status == ApplicationStatus.UNDER_REVIEW) {
            throw new BizException(400, "cannot delete material when application is submitted or under review");
        }

        // 查询 Material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BizException(404, "material not found"));

        // 校验归属关系
        if (!material.getApplicationId().equals(applicationId)) {
            throw new BizException(400, "material does not belong to this application");
        }

        // 执行删除
        materialRepository.delete(material);
    }

    /**
     * 更新 Material 内容
     * @param applicationId 申请 ID
     * @param materialId 材料 ID
     * @param content 材料内容描述
     * @param attachmentRef 附件引用
     * @return 更新后的 Material
     */
    public Material updateMaterialContent(Long applicationId, Long materialId, String content, String attachmentRef) {
        // 校验 Application 是否存在
        Application application = applicationService.getById(applicationId);

        // 当 Application.status 为 SUBMITTED 或 UNDER_REVIEW 时，禁止修改 Material
        ApplicationStatus status = application.getStatus();
        if (status == ApplicationStatus.SUBMITTED || 
            status == ApplicationStatus.UNDER_REVIEW) {
            throw new BizException(400, "cannot update material when application is submitted or under review");
        }

        // 查询 Material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BizException(404, "material not found"));

        // 校验归属关系
        if (!material.getApplicationId().equals(applicationId)) {
            throw new BizException(400, "material does not belong to this application");
        }

        // 校验写入规则：content 与 attachmentRef 至少有一个不为空
        if (content == null && attachmentRef == null) {
            throw new BizException(400, "content and attachmentRef cannot be both null");
        }

        // 更新 Material 内容
        material.setContent(content);
        material.setAttachmentRef(attachmentRef);

        // 每次成功修改，必须触发 material.version++
        material.incrementVersion();

        // 保存并返回
        return materialRepository.save(material);
    }

    /**
     * 修订材料：在 UNDER_REVIEW 阶段且材料聚合结果为 HAS_REJECT 时创建新版本
     * @param materialId 材料 ID
     * @param request 修订请求
     * @return 修订后的 Material
     */
    public Material reviseMaterial(Long materialId, ReviseMaterialRequest request) {
        // 查询 Material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BizException(404, "material not found"));

        // 获取 Application
        Long applicationId = material.getApplicationId();
        Application application = applicationService.getById(applicationId);

        // 状态守卫：仅允许 UNDER_REVIEW 状态
        ApplicationStatus status = application.getStatus();
        if (status != ApplicationStatus.UNDER_REVIEW) {
            throw new BizException(400, "can only revise material when application is under review");
        }

        // 聚合结果守卫：仅当材料聚合结果为 HAS_REJECT 时允许修订
        ApplicationReviewSummary reviewSummary = applicationService.getReviewSummary(applicationId);
        boolean isHasReject = false;
        for (ApplicationReviewSummary.MaterialReviewSummary materialSummary : reviewSummary.getMaterials()) {
            if (materialSummary.getMaterialId().equals(materialId)) {
                if (materialSummary.getAggregationResult() == ApplicationReviewSummary.AggregationResult.HAS_REJECT) {
                    isHasReject = true;
                }
                break;
            }
        }

        if (!isHasReject) {
            throw new BizException(400, "can only revise material when aggregation result is HAS_REJECT");
        }

        // 处理 scoreMode 相关逻辑
        if (material.getScoreMode() == ScoreMode.NONE) {
            // 若 scoreMode == NONE：revise 时 declaredScore 强制为 BigDecimal.ZERO
            request.setDeclaredScore(BigDecimal.ZERO);
        } else if (material.getScoreMode() == ScoreMode.DECLARED) {
            // 若 scoreMode == DECLARED：保持现有的 declaredScore 非空规则
            if (request.getDeclaredScore() == null) {
                throw new BizException(400, "declaredScore cannot be null when revising material");
            }
            if (request.getDeclaredScore().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(400, "declaredScore cannot be negative");
            }
        }

        // 至少修改一项
        if (request.getDescription() == null
            && request.getFilePath() == null
            && request.getDeclaredScore() == null) {
            throw new BizException(400, "nothing to revise");
        }
        // 修订行为：version++
        material.incrementVersion();
    
        // 更新字段
        if (request.getDeclaredScore() != null) {
            material.setDeclaredScore(request.getDeclaredScore());
        }
        if (request.getDescription() != null) {
            material.setContent(request.getDescription());
        }
        if (request.getFilePath() != null) {
            material.setAttachmentRef(request.getFilePath());
        }

        // 保存并返回最新 Material
        return materialRepository.save(material);
    }

}