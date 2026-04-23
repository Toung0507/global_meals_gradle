-- =====================================================
-- Migration: 新增廚房狀態欄位至 orders 表
-- 執行時機：後端啟動前，於 MySQL 執行此腳本
-- =====================================================

ALTER TABLE orders
  ADD COLUMN kitchen_status ENUM('WAITING', 'COOKING', 'READY') NOT NULL DEFAULT 'WAITING'
  AFTER status;

-- 將現有已付款訂單（COMPLETED）的廚房狀態設為 WAITING（預設值，等待廚房接單）
-- 新訂單付款時 Entity 預設值即為 WAITING，不需額外更新
