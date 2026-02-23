package edu.xmu.gradpath.material.domain;

/**
 * 材料计分模式枚举
 */
public enum ScoreMode {
    /**
     * 不计分：只验通过/驳回（资格型材料）
     */
    NONE,
    
    /**
     * 计分：学生申报分数（加分型材料）
     */
    DECLARED
}
