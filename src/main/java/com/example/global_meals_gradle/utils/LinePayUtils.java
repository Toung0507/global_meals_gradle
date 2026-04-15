package com.example.global_meals_gradle.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class LinePayUtils {

	/**
     * LINE Pay 專用加密演算法 (HmacSignature)
     * 規則：Base64(HmacSHA256(ChannelSecret, 資料字串))
     */
    public static String encrypt(String key, String data) {
        try {
            // 建立 HmacSHA256 加密器
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            // 設定 Secret Key
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            // 執行加密並轉為 Base64 字串 (LINE Pay 規定的最終格式)
            return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException("LINE Pay 加密失敗", e);
        }
    }
}
