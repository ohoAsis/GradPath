package edu.xmu.gradpath.material.controller.dto;

import edu.xmu.gradpath.material.domain.ScoreMode;
import java.math.BigDecimal;

public class MaterialCreateRequest {
    private String category;
    private String content;
    private String attachmentRef;
    private BigDecimal declaredScore;
    private ScoreMode scoreMode;

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

    public BigDecimal getDeclaredScore() {
        return declaredScore;
    }

    public void setDeclaredScore(BigDecimal declaredScore) {
        this.declaredScore = declaredScore;
    }

    public ScoreMode getScoreMode() {
        return scoreMode;
    }

    public void setScoreMode(ScoreMode scoreMode) {
        this.scoreMode = scoreMode;
    }
}