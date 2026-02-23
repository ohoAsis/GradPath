package edu.xmu.gradpath.application.controller;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.application.controller.dto.ApplicationQueryResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.xmu.gradpath.application.controller.dto.ApplicationReviewSummary;
import edu.xmu.gradpath.application.controller.dto.ApplicationLifecycleSummary;
import edu.xmu.gradpath.application.controller.dto.ApplicationOverview;
import edu.xmu.gradpath.application.controller.dto.ApplicationSubmissionCheckSummary;
import edu.xmu.gradpath.application.controller.dto.ApplicationScoreSummary;
import edu.xmu.gradpath.application.controller.dto.ApplicationDashboard;
import java.util.List;

@RestController
@RequestMapping(
        value = "/applications",
        produces = "application/json"
)
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 创建 Application 草稿
     */
    @PostMapping("/draft")
    public ApiResponse<Long> createDraft() {
        // MVP 阶段：先写死 userId，后续接入登录体系再替换
        Long mockUserId = 1L;

        Long applicationId = applicationService.createDraft(mockUserId);
        return ApiResponse.success(applicationId);
    }
    /**
     * 提交申请
     */
    @PostMapping("/{id}/submit")
    public ApiResponse<Long> submit(@PathVariable("id") Long applicationId) {
        Long mockUserId = 1L; // MVP：临时写死
        Long id = applicationService.submit(mockUserId, applicationId);
        return ApiResponse.success(id);
    }
    /**
     * GET接口
     */
    @GetMapping("/{id}")
    public ApiResponse<ApplicationQueryResponse> getById(
            @PathVariable("id") Long applicationId
    ) {
        Application application = applicationService.getById(applicationId);
        ApplicationQueryResponse resp = ApplicationQueryResponse.from(application);
        return ApiResponse.success(resp);
    }

    /**
     * 获取审核解释结果
     */
    @GetMapping("/{id}/review-summary")
    public ApiResponse<ApplicationReviewSummary> getReviewSummary(
            @PathVariable("id") Long applicationId
    ) {
        ApplicationReviewSummary summary = applicationService.getReviewSummary(applicationId);
        return ApiResponse.success(summary);
    }

    /**
     * 获取申请生命周期语义解释
     */
    @GetMapping("/{id}/lifecycle-summary")
    public ApiResponse<ApplicationLifecycleSummary> getLifecycleSummary(
            @PathVariable("id") Long applicationId
    ) {
        ApplicationLifecycleSummary summary = applicationService.getLifecycleSummary(applicationId);
        return ApiResponse.success(summary);
    }

    /**
     * 获取 Application 全局视角列表
     */
    @GetMapping("/overview")
    public ApiResponse<List<ApplicationOverview>> getApplicationOverviews() {
        List<ApplicationOverview> overviews = applicationService.getApplicationOverviews();
        return ApiResponse.success(overviews);
    }

    /**
     * 获取申请提交校验解释
     */
    @GetMapping("/{id}/submission-check")
    public ApiResponse<ApplicationSubmissionCheckSummary> getSubmissionCheckSummary(
            @PathVariable("id") Long applicationId
    ) {
        ApplicationSubmissionCheckSummary summary = applicationService.getSubmissionCheckSummary(applicationId);
        return ApiResponse.success(summary);
    }

    /**
     * 获取申请分值汇总解释
     */
    @GetMapping("/{id}/scores")
    public ApiResponse<ApplicationScoreSummary> getScoreSummary(
            @PathVariable("id") Long applicationId
    ) {
        ApplicationScoreSummary summary = applicationService.getScoreSummary(applicationId);
        return ApiResponse.success(summary);
    }

    /**
     * 获取申请统一进度解释
     */
    @GetMapping("/{id}/dashboard")
    public ApiResponse<ApplicationDashboard> getDashboard(
            @PathVariable("id") Long applicationId
    ) {
        ApplicationDashboard dashboard = applicationService.getDashboard(applicationId);
        return ApiResponse.success(dashboard);
    }
}
