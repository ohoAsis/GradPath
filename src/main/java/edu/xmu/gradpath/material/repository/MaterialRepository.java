package edu.xmu.gradpath.material.repository;

import edu.xmu.gradpath.material.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    /**
     * 根据 applicationId 查询材料列表
     */
    List<Material> findByApplicationId(Long applicationId);

}