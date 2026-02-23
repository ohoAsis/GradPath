package edu.xmu.gradpath.material.controller;

import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.material.domain.MaterialScore;
import edu.xmu.gradpath.material.service.MaterialScoreService;
import org.springframework.web.bind.annotation.*;

/**
 * MaterialScore 控制器
 * 提供创建材料分值裁决记录的 HTTP 入口
 */
@RestController
@RequestMapping(
        value = "/materials",
        produces = "application/json"
)
public class MaterialScoreController {

    private final MaterialScoreService materialScoreService;

    public MaterialScoreController(MaterialScoreService materialScoreService) {
        this.materialScoreService = materialScoreService;
    }

    /**
     * 为指定材料创建分值裁决记录
     * 只接收 path 参数 materialId，不需要 request body
     * 调用 materialScoreService.createScore(materialId)
     * 返回创建的 MaterialScore
     */
    @PostMapping("/{materialId}/score")
    public ApiResponse<MaterialScore> createScore(
            @PathVariable Long materialId
    ) {
        MaterialScore materialScore = materialScoreService.createScore(materialId);
        return ApiResponse.success(materialScore);
    }
}
