package edu.xmu.gradpath.material.service;

import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.exception.BizException;
import edu.xmu.gradpath.material.domain.Material;
import edu.xmu.gradpath.material.repository.MaterialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
     * @return 保存后的 Material
     */
    public Material createMaterial(Long applicationId, String category, String content, String attachmentRef) {
        // 校验 Application 是否存在
        applicationService.getById(applicationId);

        // 校验写入规则：content 与 attachmentRef 至少有一个不为空
        if (content == null && attachmentRef == null) {
            throw new BizException(400, "content and attachmentRef cannot be both null");
        }

        // 构造 Material
        Material material = new Material(applicationId, category, content, attachmentRef);

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
        applicationService.getById(applicationId);

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

}