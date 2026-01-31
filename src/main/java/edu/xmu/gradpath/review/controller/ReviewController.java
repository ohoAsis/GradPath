package edu.xmu.gradpath.review.controller;

import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.review.controller.dto.ReviewRequest;
import edu.xmu.gradpath.review.service.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/applications",
        produces = "application/json"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 审核申请
     */
    @PostMapping("/{id}/review")
    public ApiResponse<Void> review(
            @PathVariable("id") Long applicationId,
            @RequestBody ReviewRequest request
    ) {
        reviewService.review(applicationId, request.isApproved());
        return ApiResponse.success(null);
    }
}
