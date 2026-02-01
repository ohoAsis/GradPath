package edu.xmu.gradpath.review.controller;

import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.review.controller.dto.ReviewRecordQueryResponse;
import edu.xmu.gradpath.review.domain.ReviewRecord;
import edu.xmu.gradpath.review.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = "/materials",
        produces = "application/json"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 查询指定 Material 的 ReviewRecord 列表
     */
    @GetMapping("/{materialId}/reviews")
    public ApiResponse<List<ReviewRecordQueryResponse>> getReviewsByMaterialId(
            @PathVariable Long materialId
    ) {
        List<ReviewRecord> reviewRecords = reviewService.getByMaterialId(materialId);
        
        // 转换为 DTO 列表
        List<ReviewRecordQueryResponse> responses = reviewRecords.stream()
                .map(ReviewRecordQueryResponse::from)
                .collect(Collectors.toList());
        
        return ApiResponse.success(responses);
    }

}
