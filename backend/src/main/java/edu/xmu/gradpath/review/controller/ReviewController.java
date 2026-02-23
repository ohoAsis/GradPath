package edu.xmu.gradpath.review.controller;

import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.review.controller.dto.CreateReviewRequest;
import edu.xmu.gradpath.review.controller.dto.ReviewRecordQueryResponse;
import edu.xmu.gradpath.review.domain.ReviewRecord;
import edu.xmu.gradpath.review.service.ReviewService;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 为指定 Material 创建 ReviewRecord
     */
    @PostMapping("/{materialId}/reviews")
    public ApiResponse<ReviewRecordQueryResponse> createReviewRecord(
            @PathVariable Long materialId,
            @RequestBody CreateReviewRequest request
    ) {
        ReviewRecord reviewRecord = reviewService.createReviewRecord(
                materialId,
                request.getReviewerId(),
                request.getDecision(),
                request.getComment(),
                request.getMaterialVersion()
        );
        
        return ApiResponse.success(ReviewRecordQueryResponse.from(reviewRecord));
    }

}
