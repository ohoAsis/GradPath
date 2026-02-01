package edu.xmu.gradpath.material.controller.dto;

import edu.xmu.gradpath.material.domain.Material;

import java.time.LocalDateTime;

/**
 * Material 查询接口的返回 DTO
 * 只用于"读"，不承载任何业务行为
 */
public class MaterialQueryResponse {

    private Long id;
    private String category;
    private String content;
    private String attachmentRef;
    private LocalDateTime createdAt;

    private MaterialQueryResponse() {
    }

    public static MaterialQueryResponse from(Material material) {
        MaterialQueryResponse resp = new MaterialQueryResponse();
        resp.id = material.getId();
        resp.category = material.getCategory();
        resp.content = material.getContent();
        resp.attachmentRef = material.getAttachmentRef();
        resp.createdAt = material.getCreatedAt();
        return resp;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public String getAttachmentRef() {
        return attachmentRef;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}