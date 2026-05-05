package com.example.global_meals_gradle.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.global_meals_gradle.res.BasicRes;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 宣告 Logger，注意要選 org.slf4j 的封裝版本
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕捉參數驗證失敗 (@NotBlank, @Min, @Max 等)
     * HTTP 狀態碼：400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BasicRes handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        
        // 紀錄警告日誌 (WARN)，這通常是前端傳入資料有誤
        log.warn("【參數驗證失敗】原因: {}", errorMsg);
        
        return new BasicRes(400, "驗證失敗: " + errorMsg);
    }

    /**
     * 捕捉 JSON 格式錯誤 (例如：前端將數字欄位傳成字串，或 JSON 語法錯誤)
     * HTTP 狀態碼：400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BasicRes handleJsonException(HttpMessageNotReadableException e) {
        
        // 紀錄警告日誌
        log.warn("【JSON 解析失敗】請檢查前端請求格式: {}", e.getMessage());
        
        return new BasicRes(400, "資料格式錯誤，請檢查 JSON 結構");
    }

    /**
     * 捕捉所有未預期的錯誤 (例如：NullPointerException, SQL 錯誤等)
     * HTTP 狀態碼：500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BasicRes handleAllException(Exception e) {
        
        // 【核心修正】使用 log.error 紀錄完整錯誤現場
        // 最後一個參數傳入 e，Logback 會自動將完整的 Stack Trace 寫入 logs 檔案中
        log.error("【系統發生未預期錯誤】詳細原因: ", e);

        // 為了安全性，不建議將太詳細的錯誤 (如 DAO 名稱) 直接丟給前端
        // 但開發階段你可以先保留 e.getMessage()
        return new BasicRes(500, "系統發生未預期錯誤，請連繫管理員。(" + e.getMessage() + ")");
    }
}