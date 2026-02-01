package edu.xmu.gradpath.material.controller;

import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.material.controller.dto.MaterialCreateRequest;
import edu.xmu.gradpath.material.controller.dto.MaterialQueryResponse;
import edu.xmu.gradpath.material.domain.Material;
import edu.xmu.gradpath.material.service.MaterialService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = "/applications",
        produces = "application/json"
)
public class MaterialController {

    private final ApplicationService applicationService;
    private final MaterialService materialService;

    public MaterialController(ApplicationService applicationService, MaterialService materialService) {
        this.applicationService = applicationService;
        this.materialService = materialService;
    }

    /**
     * 查询指定 Application 下的所有 Material
     * 排序：按 createdAt 升序
     */
    @GetMapping("/{applicationId}/materials")
    public ApiResponse<List<MaterialQueryResponse>> getMaterialsByApplicationId(
            @PathVariable Long applicationId
    ) {
        // 校验 Application 是否存在
        applicationService.getById(applicationId);
        
        List<Material> materials = materialService.getByApplicationId(applicationId);
        
        // 转换为 DTO 列表
        List<MaterialQueryResponse> responses = materials.stream()
                .map(MaterialQueryResponse::from)
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses);
    }

    /**
     * 为指定 Application 新增一条 Material
     */
    @PostMapping("/{applicationId}/materials")
    public ApiResponse<MaterialQueryResponse> createMaterial(
            @PathVariable Long applicationId,
            @RequestBody MaterialCreateRequest request
    ) {
        Material material = materialService.createMaterial(
                applicationId,
                request.getCategory(),
                request.getContent(),
                request.getAttachmentRef()
        );
        
        return ApiResponse.success(MaterialQueryResponse.from(material));
    }

    /**
     * 删除指定 Application 下的一条 Material
     */
    @DeleteMapping("/{applicationId}/materials/{materialId}")
    public ApiResponse<Void> deleteMaterial(
            @PathVariable Long applicationId,
            @PathVariable Long materialId
    ) {
        materialService.deleteMaterial(applicationId, materialId);
        return ApiResponse.success(null);
    }

}