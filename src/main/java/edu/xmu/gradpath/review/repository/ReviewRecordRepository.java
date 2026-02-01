package edu.xmu.gradpath.review.repository;

import edu.xmu.gradpath.review.domain.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

    /**
     * 根据 materialId 查询 ReviewRecord 列表
     */
    List<ReviewRecord> findByMaterialId(Long materialId);

}
