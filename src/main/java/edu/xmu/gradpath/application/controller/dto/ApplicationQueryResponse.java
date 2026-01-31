package edu.xmu.gradpath.application.controller.dto;

import edu.xmu.gradpath.application.domain.Application;
import edu.xmu.gradpath.application.domain.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Application 查询接口的返回 DTO
 * 只用于“读”，不承载任何业务行为
 */
public class ApplicationQueryResponse {

    private Long id;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ApplicationQueryResponse() {
    }

    public static ApplicationQueryResponse from(Application application) {
        ApplicationQueryResponse resp = new ApplicationQueryResponse();
        resp.id = application.getId();
        resp.status = application.getStatus();
        resp.createdAt = application.getCreatedAt();
        resp.updatedAt = application.getUpdatedAt();
        return resp;
    }

    // ===== getter =====

    public Long getId() {
        return id;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
