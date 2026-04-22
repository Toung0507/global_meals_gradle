package com.example.global_meals_gradle.utils;

import java.util.Set;

import org.springframework.util.StringUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/* 電話號碼驗證工具類別：使用 Google libphonenumber 函式庫驗證各國電話號碼格式。 */
public class PhoneValidatorUtils {
	
	/* getInstance() 是一個靜態工廠方法(Static Factory Method)，它實現了單例模式(Singleton Pattern)。
	 * １.單例模式：確保一個類別（Class）在整個程式執行期間，永遠「只有一個實例」。
	 * ２.靜態工廠方法：不直接使用 new，而是透過一個「靜態方法」來向類別索取物件。
	 * 為什麼不使用 new PhoneNumberUtil() 的方式
	 * 因為在 libphonenumber 庫中，PhoneNumberUtil 的建構子通常被設為 private，所以你無法直接使用 new。 */
	private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();
	
	// 獲取所有支援的區域代碼 (如 TW, US, JP...)
	private static final Set<String> SUPPORTED_REGIONS = PHONE_UTIL.getSupportedRegions();
	
    /* 驗證電話號碼是否合法
     * 流程：
     *  1. 嘗試解析（parse）電話號碼，若格式根本無法解析（例如 "abc"）則回傳 false
     *  2. 用 isValidNumber() 驗證是否為該國真實存在的號碼段
     * @param phoneNumber 使用者輸入的電話號碼（可含或不含國碼，例如 "0912345678" 或 "+886912345678"）
     * @param countryCode 國家代碼（ISO 3166-1 alpha-2），例如 "TW"、"JP"、"KR"
     *                    用於告訴 libphonenumber「當號碼沒有 + 國碼時，預設哪個國家」
     * @return true = 合法號碼 / false = 不合法或無法解析 */
    public static boolean isValid(String phoneNumber, String countryCode) {
    	// 參數檢查
        if (!StringUtils.hasText(phoneNumber) || !StringUtils.hasText(countryCode)) {
            return false;
        }
        // 檢查該國家代碼是否在函式庫支援清單中
        if (!SUPPORTED_REGIONS.contains(countryCode.toUpperCase())) {
        	return false;
        }
        try {
        	/* parse()：嘗試把字串解析成 PhoneNumber 物件
        	 * 第一個參數：使用者輸入的號碼字串
        	 * 第二個參數：預設國家代碼（當號碼沒有明確的 +國碼 時使用） */
            PhoneNumber parsedNumber = PHONE_UTIL.parse(phoneNumber, countryCode.toUpperCase());
            /* isValidNumber()：更嚴格的驗證
             * 不只判斷「位數對不對」，還確認「這個號段在該國是否真實存在」
             * 例如：台灣手機號碼必須 09 開頭，0912345678 ✅，0012345678 ❌ */
            // 1. 基本合法性檢查 (包含長度、國碼、區碼是否正確)
            if (!PHONE_UTIL.isValidNumber(parsedNumber)) {
            	return false;
            }
            // 2. 電話類型檢查 (手機號碼、市內電話...等等)
            PhoneNumberType phoneType = PHONE_UTIL.getNumberType(parsedNumber);
            /* 我們允許的類型：FIXED_LINE(市話)、MOBILE(手機)、FIXED_LINE_OR_MOBILE(部分國家(如美國)市話手機編碼相同) */
            return phoneType == PhoneNumberType.FIXED_LINE || //
            		phoneType == PhoneNumberType.MOBILE || //
            		phoneType == PhoneNumberType.FIXED_LINE_OR_MOBILE;
        } catch (NumberParseException e) {
            // 如果 parse() 無法解析（例如輸入 "hello"、"000" 超短號碼），會拋出 NumberParseException，我們在此攔截並回傳 false
            return false;
        }
    }
    
    /* 將電話號碼標準化為 E.164 格式
     * E.164 是國際電話號碼標準格式："+國際區號（去掉0）+ 號碼"
     *  例如：台灣(+886) "0912345678" → "+886912345678"
     * 		 日本(+81) "09012345678" → "+819012345678"
     * 		 韓國(+82) "01012345678" → "+821012345678"
     * 建議：存進資料庫前先做標準化，讓 DB 的格式統一
     * 注意：使用本方法前建議先呼叫 isValid() 確認號碼合法，若號碼非法強行 format 可能拋出例外
     * @param phoneNumber 使用者輸入的電話號碼
     * @param countryCode 國家代碼（例如 "TW"）
     * @return E.164 格式字串（例如 "+886912345678"），解析失敗則回傳原始輸入 */
    public static String toE164Format(String phoneNumber, String countryCode) {
        if (!StringUtils.hasText(phoneNumber)) {
            return phoneNumber;
        }
        try {
            // 解析號碼
            PhoneNumber parsedNumber = PHONE_UTIL.parse(phoneNumber, countryCode.toUpperCase());
            // PhoneNumberFormat.E164 → 標準化為 +886912345678 格式
            return PHONE_UTIL.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            // 解析失敗，回傳原始輸入（不要讓系統因此崩潰）
            return phoneNumber;
        }
    }
}
