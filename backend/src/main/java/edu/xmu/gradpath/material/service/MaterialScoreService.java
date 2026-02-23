package edu.xmu.gradpath.material.service;

import edu.xmu.gradpath.application.controller.dto.ApplicationReviewSummary;
import edu.xmu.gradpath.application.domain.ApplicationStatus;
import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.exception.BizException;
import edu.xmu.gradpath.material.domain.Material;
import edu.xmu.gradpath.material.domain.MaterialScore;
import edu.xmu.gradpath.material.repository.MaterialRepository;
import edu.xmu.gradpath.material.repository.MaterialScoreRepository;
import org.springframework.stereotype.Service;
import edu.xmu.gradpath.material.domain.ScoreMode;
import java.util.List;

@Service
public class MaterialScoreService {

    private final MaterialRepository materialRepository;
    private final MaterialScoreRepository materialScoreRepository;
    private final ApplicationService applicationService;

    public MaterialScoreService(MaterialRepository materialRepository, MaterialScoreRepository materialScoreRepository, ApplicationService applicationService) {
        this.materialRepository = materialRepository;
        this.materialScoreRepository = materialScoreRepository;
        this.applicationService = applicationService;
    }

    /**
     * 创建材料分值裁决记录
     * @param materialId 材料 ID
     * @return 创建的 MaterialScore
     */
    public MaterialScore createScore(Long materialId) {
        // 1. 查询 Material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BizException(404, "material not found"));

        // 2. 校验材料是否计分
        if (material.getScoreMode() == ScoreMode.NONE) {
            throw new BizException(400, "material is non-scoring");
        }

        // 3. 查询 Application 并校验状态
        Long applicationId = material.getApplicationId();
        if (applicationService.getById(applicationId).getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BizException(400, "application must be under review");
        }

        // 4. 校验当前材料聚合结果
        ApplicationReviewSummary reviewSummary = applicationService.getReviewSummary(applicationId);
        boolean isAllPass = false;
        for (ApplicationReviewSummary.MaterialReviewSummary materialSummary : reviewSummary.getMaterials()) {
            if (materialSummary.getMaterialId().equals(materialId)) {
                if (materialSummary.getAggregationResult() == ApplicationReviewSummary.AggregationResult.ALL_PASS) {
                    isAllPass = true;
                }
                break;
            }
        }

        if (!isAllPass) {
            throw new BizException(400, "material aggregation result must be ALL_PASS");
        }

        // 5. 校验唯一性
        List<MaterialScore> scores =
                materialScoreRepository.findByMaterialIdAndMaterialVersion(
                        materialId,
                        material.getVersion()
                );
        if (!scores.isEmpty()) {
            throw new BizException(400, "material score already exists for this version");
        }

        // 6. 创建 MaterialScore
        MaterialScore materialScore = new MaterialScore(
                materialId,
                material.getVersion(),
                material.getDeclaredScore()
        );

        // 保存并返回
        return materialScoreRepository.save(materialScore);
    }
}
