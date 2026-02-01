package edu.xmu.gradpath.material.controller.dto;

/**
 * 创建 Material 的请求 DTO
 */
public class MaterialCreateRequest {

    private String category;
    private String content;
    private String attachmentRef;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachmentRef() {
        return attachmentRef;
    }

    public void setAttachmentRef(String attachmentRef) {
        this.attachmentRef = attachmentRef;
    }
}