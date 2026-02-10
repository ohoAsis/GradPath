package edu.xmu.gradpath.material.repository;

import edu.xmu.gradpath.material.domain.MaterialScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 材料分值裁决记录仓库
 */
public interface MaterialScoreRepository extends JpaRepository<MaterialScore, Long> {

    /**
     * 根据材料 ID 查询分值裁决记录
     * @param materialId 材料 ID
     * @return 分值裁决记录列表
     */
    List<MaterialScore> findByMaterialId(Long materialId);

    /**
     * 根据材料 ID 和材料版本号查询某一版材料的裁决结果
     * @param materialId 材料 ID
     * @param materialVersion 材料版本号
     * @return 分值裁决记录列表
     */
    List<MaterialScore> findByMaterialIdAndMaterialVersion(Long materialId, Integer materialVersion);
}
