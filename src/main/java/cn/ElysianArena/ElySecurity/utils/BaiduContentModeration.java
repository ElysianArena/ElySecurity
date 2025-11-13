package cn.ElysianArena.ElySecurity.utils;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.ElysianArena.ElySecurity.Main;
import cn.ElysianArena.ElySecurity.security.BaiduViolation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BaiduContentModeration {
    private final Main plugin;
    private String accessToken;
    private long tokenExpireTime;

    private static final String TEXT_CENSOR_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined";
    private static final String AUTH_URL = "https://aip.baidubce.com/oauth/2.0/token";

    public BaiduContentModeration(Main plugin) {
        this.plugin = plugin;
    }

    // 检查文本内容
    public BaiduViolation checkContent(String text) {
        BaiduViolation result = new BaiduViolation();

        try {
            // 获取access token
            String token = getAccessToken();
            if (token == null) {
                plugin.getLogger().error("获取百度API access token失败");
                return result;
            }

            // 构建请求参数
            String params = "access_token=" + token + "&text=" + URLEncoder.encode(text, "UTF-8");

            // 添加策略ID（如果配置了）
            Config config = plugin.getConfigManager().getConfig();
            int strategyId = config.getInt("baidu-api.strategy-id", 0);
            if (strategyId > 0) {
                params += "&strategyId=" + strategyId;
            }

            // 发送请求
            String response = sendPostRequest(TEXT_CENSOR_URL, params);

            // 使用FastJson解析响应
            return parseBaiduResponse(response);

        } catch (Exception e) {
            plugin.getLogger().error("百度内容审核API调用失败: " + e.getMessage());
            return result;
        }
    }

    // 获取Access Token
    private String getAccessToken() {
        // 检查token是否过期
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }

        try {
            Config config = plugin.getConfigManager().getConfig();
            String apiKey = config.getString("baidu-api.api-key");
            String secretKey = config.getString("baidu-api.secret-key");

            if (apiKey == null || secretKey == null || apiKey.equals("your_api_key_here")) {
                plugin.getLogger().error("百度API密钥未配置");
                return null;
            }

            String params = "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
            String response = sendPostRequest(AUTH_URL, params);

            // 使用FastJson解析响应
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.containsKey("access_token")) {
                accessToken = jsonResponse.getString("access_token");

                // 设置token过期时间（提前5分钟刷新）
                long expiresIn = jsonResponse.getLongValue("expires_in") * 1000L;
                tokenExpireTime = System.currentTimeMillis() + expiresIn - (5 * 60 * 1000L);

                plugin.getLogger().info("百度API Access Token获取成功");
                return accessToken;
            } else {
                plugin.getLogger().error("获取Access Token失败: " + jsonResponse.getString("error_description"));
            }

        } catch (Exception e) {
            plugin.getLogger().error("获取百度Access Token失败: " + e.getMessage());
        }

        return null;
    }

    // 发送POST请求
    private String sendPostRequest(String urlStr, String params) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP响应码: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine);
            }
        }

        return response.toString();
    }

    // 使用FastJson解析百度API响应
    private BaiduViolation parseBaiduResponse(String response) {
        BaiduViolation result = new BaiduViolation();

        try {
            JSONObject jsonResponse = JSON.parseObject(response);

            // 检查错误码
            if (jsonResponse.containsKey("error_code")) {
                int errorCode = jsonResponse.getIntValue("error_code");
                String errorMsg = jsonResponse.getString("error_msg");
                plugin.getLogger().error("百度API返回错误: " + errorCode + " - " + errorMsg);
                return result;
            }

            // 获取审核结论
            String conclusion = jsonResponse.getString("conclusion");
            int conclusionType = jsonResponse.getIntValue("conclusionType");

            // 判断是否违规
            if ("不合规".equals(conclusion) || conclusionType == 2) {
                result.violated = true;

                // 解析违规详情
                JSONArray dataArray = jsonResponse.getJSONArray("data");
                if (dataArray != null && !dataArray.isEmpty()) {
                    List<String> violatedWords = new ArrayList<>();

                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject dataItem = dataArray.getJSONObject(i);
                        int type = dataItem.getIntValue("type");
                        int subType = dataItem.getIntValue("subType");
                        String msg = dataItem.getString("msg");

                        // 设置主要违规类型
                        if (i == 0) {
                            result.type = type;
                            result.subType = subType;
                            result.message = msg;
                        }

                        // 解析命中关键词
                        JSONArray hitsArray = dataItem.getJSONArray("hits");
                        if (hitsArray != null) {
                            for (int j = 0; j < hitsArray.size(); j++) {
                                JSONObject hit = hitsArray.getJSONObject(j);

                                // 获取传统关键词
                                JSONArray wordsArray = hit.getJSONArray("words");
                                if (wordsArray != null) {
                                    for (int k = 0; k < wordsArray.size(); k++) {
                                        violatedWords.add(wordsArray.getString(k));
                                    }
                                }

                                // 获取带位置信息的关键词
                                JSONArray wordHitPositions = hit.getJSONArray("wordHitPositions");
                                if (wordHitPositions != null) {
                                    for (int k = 0; k < wordHitPositions.size(); k++) {
                                        JSONObject wordHit = wordHitPositions.getJSONObject(k);
                                        String keyword = wordHit.getString("keyword");
                                        if (keyword != null && !keyword.isEmpty()) {
                                            violatedWords.add(keyword);
                                        }
                                    }
                                }

                                // 获取置信度
                                if (hit.containsKey("probability")) {
                                    try {
                                        result.confidence = hit.getDoubleValue("probability");
                                    } catch (Exception e) {
                                        // 忽略转换异常
                                    }
                                }
                            }
                        }
                    }

                    result.violatedWords = violatedWords;
                }
            }

        } catch (Exception e) {
            plugin.getLogger().error("解析百度API响应失败: " + e.getMessage());
            plugin.getLogger().error("原始响应: " + response);
        }

        return result;
    }

    // 测试API连接
    public boolean testConnection() {
        try {
            String token = getAccessToken();
            return token != null;
        } catch (Exception e) {
            plugin.getLogger().error("百度API连接测试失败: " + e.getMessage());
            return false;
        }
    }
}