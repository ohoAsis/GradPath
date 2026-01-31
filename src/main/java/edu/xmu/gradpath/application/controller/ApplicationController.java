package edu.xmu.gradpath.application.controller;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.service.ApplicationService;
import edu.xmu.gradpath.common.response.ApiResponse;
import edu.xmu.gradpath.application.controller.dto.ApplicationQueryResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
