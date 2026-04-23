-- =====================================================
-- Migration: 新增國家代碼欄位至 members 表
-- 執行時機：後端啟動前，於 MySQL 執行此腳本
-- =====================================================

ALTER TABLE members
  ADD COLUMN country VARCHAR(5) NOT NULL DEFAULT 'TW'
  AFTER phone;

-- 現有會員預設設為台灣（TW）
-- 新會員由前端 BranchService 傳入，合法值：TW / JP / KR
