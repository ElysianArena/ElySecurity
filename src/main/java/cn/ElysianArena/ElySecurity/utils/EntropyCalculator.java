package cn.ElysianArena.ElySecurity.utils;

import java.util.HashMap;
import java.util.Map;

public class EntropyCalculator {

    // 计算字符串的信息熵
    public double calculateEntropy(String str) {
        if (str == null || str.isEmpty()) {
            return 0.0;
        }

        Map<Character, Integer> freqMap = new HashMap<>();
        int length = str.length();

        // 统计字符频率
        for (char c : str.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        // 计算信息熵
        double entropy = 0.0;
        for (int count : freqMap.values()) {
            double probability = (double) count / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    // 计算平均信息熵
    public double calculateAverageEntropy(String str) {
        double entropy = calculateEntropy(str);
        if (entropy == 0.0) return 0.0;

        double length = str.length();
        return entropy / Math.log(length);
    }
}