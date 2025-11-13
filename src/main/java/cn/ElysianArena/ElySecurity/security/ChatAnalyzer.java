package cn.ElysianArena.ElySecurity.security;

import cn.ElysianArena.ElySecurity.utils.EntropyCalculator;
import cn.ElysianArena.ElySecurity.utils.SimilarityCalculator;

public class ChatAnalyzer {
    private final EntropyCalculator entropyCalculator;
    private final SimilarityCalculator similarityCalculator;

    public ChatAnalyzer() {
        this.entropyCalculator = new EntropyCalculator();
        this.similarityCalculator = new SimilarityCalculator();
    }

    // 计算消息的平均信息熵
    public double calculateAverageEntropy(String message) {
        return entropyCalculator.calculateAverageEntropy(message);
    }

    // 计算两条消息的相似度
    public double calculateSimilarity(String str1, String str2) {
        return similarityCalculator.calculateSimilarity(str1, str2);
    }
}