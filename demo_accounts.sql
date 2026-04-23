-- =====================================================
-- Demo 帳號初始化腳本（含 TW 分店資料）
-- 用途：Demo 展示前於 MySQL 執行
-- 密碼統一：12345（BCrypt 加密）
-- 執行順序：regions → global_area → staff → members
-- =====================================================

SET @pwd_12345 = '$2b$10$l9sT0YMosrTD7lD5lLjRy.PaZyvALNQc7NEDGrWg9qjKwZTV1g0I6';

-- =====================================================
-- 1. 新增台灣 region（目前只有韓國/日本，缺台灣）
-- =====================================================
INSERT IGNORE INTO regions (country, currency_code, tax_rate, tax_type, created_at, updated_at)
VALUES ('台灣', 'TWD', 0.0500, 'INCLUSIVE', CURDATE(), CURDATE());

-- =====================================================
-- 2. 新增台灣分店到 global_area
-- =====================================================
INSERT IGNORE INTO global_area (country, country_code, branch, address, phone)
VALUES ('台灣', 'TW', '懶飽飽台北總店', '台北市中正區重慶南路一段', '02-2345-6789');

-- 取得 TW 分店 id
SET @tw_area_id = (SELECT id FROM global_area WHERE country_code = 'TW' LIMIT 1);

-- =====================================================
-- 3. Staff 帳號（管理後台 / POS 登入）
-- =====================================================

-- 老闆（ADMIN）— 只能 SQL 手動建立，global_area_id 使用 TW 分店
INSERT IGNORE INTO staff (name, account, password, role, global_area_id, is_status, hire_at)
VALUES ('林老闆', 'ADMIN', @pwd_12345, 'ADMIN', @tw_area_id, true, CURDATE());

-- 分店長（REGION_MANAGER）
INSERT IGNORE INTO staff (name, account, password, role, global_area_id, is_status, hire_at)
VALUES ('陳分店長', 'RM0001', @pwd_12345, 'REGION_MANAGER', @tw_area_id, true, CURDATE());

-- 副店長（MANAGER_AGENT）
INSERT IGNORE INTO staff (name, account, password, role, global_area_id, is_status, hire_at)
VALUES ('李副店長', 'MA0001', @pwd_12345, 'MANAGER_AGENT', @tw_area_id, true, CURDATE());

-- 員工（STAFF）
INSERT IGNORE INTO staff (name, account, password, role, global_area_id, is_status, hire_at)
VALUES ('王小明', 'ST0001', @pwd_12345, 'STAFF', @tw_area_id, true, CURDATE());

-- =====================================================
-- 4. Members 帳號（客戶端會員登入，phone 須為 10 碼）
-- =====================================================
INSERT IGNORE INTO members (name, phone, country, password, order_count, is_discount, created_at)
VALUES ('測試會員甲', '0912345678', 'TW', @pwd_12345, 0, false, CURDATE());

INSERT IGNORE INTO members (name, phone, country, password, order_count, is_discount, created_at)
VALUES ('測試會員乙', '0923456789', 'TW', @pwd_12345, 0, false, CURDATE());

-- =====================================================
-- 登入帳號一覽
-- =====================================================
-- 角色        帳號          密碼    登入後
-- 老闆        ADMIN         12345   /manager-dashboard
-- 分店長      RM0001        12345   /pos-terminal
-- 副店長      MA0001        12345   /pos-terminal
-- 員工        ST0001        12345   /pos-terminal
-- 會員        0912345678    12345   客戶端會員登入
-- 會員        0923456789    12345   客戶端會員登入

-- 確認結果
SELECT '=== regions ===' AS '';
SELECT id, country, currency_code FROM regions;
SELECT '=== global_area ===' AS '';
SELECT id, country, branch FROM global_area;
SELECT '=== staff ===' AS '';
SELECT id, name, account, role, global_area_id FROM staff;
SELECT '=== members ===' AS '';
SELECT id, name, phone, country FROM members;
