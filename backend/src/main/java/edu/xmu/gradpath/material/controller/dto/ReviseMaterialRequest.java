package edu.xmu.gradpath.material.controller.dto;

import java.math.BigDecimal;

public class ReviseMaterialRequest {
    private BigDecimal declaredScore;
    private String description;
    private String filePath;

    public BigDecimal getDeclaredScore() {
        return declaredScore;
    }

    public void setDeclaredScore(BigDecimal declaredScore) {
        this.declaredScore = declaredScore;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
