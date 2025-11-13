package cn.ElysianArena.ElySecurity.security;

import java.util.List;

public class BaiduViolation {
    public boolean violated;
    public int type; // 违规类型
    public int subType; // 子类型
    public List<String> violatedWords; // 违规关键词
    public double confidence; // 置信度
    public String message; // 违规描述

    public BaiduViolation() {
        this.violated = false;
        this.confidence = 0.0;
    }
}