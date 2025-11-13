package cn.ElysianArena.ElySecurity.security;

import java.util.List;

public class ViolationResult {
    private boolean violated; // 是否违规
    private int violationType; // 违规类型ID
    private int subType; // 子类型
    private List<String> violationDetails; // 违规详情(如命中的关键词)
    private String source; // 检测来源 local/baidu
    private double confidence; // 置信度
    private String message; // 附加消息

    public ViolationResult() {
        this.violated = false;
        this.violationType = 0;
        this.subType = 0;
        this.confidence = 0.0;
    }

    // Getter和Setter方法
    public boolean isViolated() {
        return violated;
    }

    public void setViolated(boolean violated) {
        this.violated = violated;
    }

    public int getViolationType() {
        return violationType;
    }

    public void setViolationType(int violationType) {
        this.violationType = violationType;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public List<String> getViolationDetails() {
        return violationDetails;
    }

    public void setViolationDetails(List<String> violationDetails) {
        this.violationDetails = violationDetails;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ViolationResult{" +
                "violated=" + violated +
                ", violationType=" + violationType +
                ", subType=" + subType +
                ", violationDetails=" + violationDetails +
                ", source='" + source + '\'' +
                ", confidence=" + confidence +
                ", message='" + message + '\'' +
                '}';
    }
}