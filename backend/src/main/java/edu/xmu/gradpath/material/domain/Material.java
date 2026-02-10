package edu.xmu.gradpath.material.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gp_material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属申请 ID
     */
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    /**
     * 材料类别
     */
    @Column(nullable = false)
    private String category;

    /**
     * 材料内容描述
     */
    @Column
    private String content;

    /**
     * 附件引用（如文件路径或存储标识）
     */
    @Column(name = "attachment_ref")
    private String attachmentRef;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 版本号
     */
    @Column(nullable = false, columnDefinition = "int default 1")
    private Integer version = 1;

    protected Material() {
        // JPA only
    }

    /**
     * 构造器：创建 Material
     */
    public Material(Long applicationId, String category, String content, String attachmentRef) {
        this.applicationId = applicationId;
        this.category = category;
        this.content = content;
        this.attachmentRef = attachmentRef;
        /**
        * Material 的时间字段现在由构造器负责，
        * 未来如果引入 JPA Auditing，需要把这一段迁走。
        */
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1;
    }

    public void incrementVersion() {
    this.version++;
}
    public Long getId() {
        return id;
    }

    public Long getApplicationId() {
        return applicationId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAttachmentRef(String attachmentRef) {
        this.attachmentRef = attachmentRef;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}