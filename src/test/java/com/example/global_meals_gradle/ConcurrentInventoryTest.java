package com.example.global_meals_gradle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.global_meals_gradle.dao.BranchInventoryDao;
import com.example.global_meals_gradle.entity.BranchInventory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ConcurrentInventoryTest {

    @Autowired
    private BranchInventoryDao branchInventoryDao;

    // 測試目標：招牌滷肉飯 / 台北總店
    private static final int PRODUCT_ID    = 1;
    private static final int GLOBAL_AREA_ID = 4;
    private static final int INITIAL_STOCK  = 5;   // 模擬「限量 5 份」
    private static final int THREAD_COUNT   = 100;  // 模擬 100 人同時搶購

    @Test
    @DisplayName("100 筆併發請求搶購僅剩 5 份的商品，驗證庫存不會變成負數（樂觀鎖）")
    void testConcurrentOrder_stockShouldNotGoNegative() throws InterruptedException {

        // ── 1. 測試前準備：把庫存設為 5 ───────────────────────────────────────
        BranchInventory inv = branchInventoryDao
                .findByProductIdAndGlobalAreaId(PRODUCT_ID, GLOBAL_AREA_ID)
                .orElseThrow(() -> new RuntimeException("找不到測試用庫存資料"));
        int originalStock = inv.getStockQuantity(); // 儲存原始庫存，測試後復原
        inv.setStockQuantity(INITIAL_STOCK);
        branchInventoryDao.save(inv);
        System.out.println("【測試前】庫存設定為 " + INITIAL_STOCK + " 份");

        // ── 2. 建立 100 個 Thread，全部就位後同時起跑 ─────────────────────────
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT); // 等所有 Thread 就位
        CountDownLatch startLatch = new CountDownLatch(1);            // 統一起跑信號
        CountDownLatch doneLatch  = new CountDownLatch(THREAD_COUNT); // 等所有 Thread 跑完

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount    = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // 等待統一起跑

                    // 讀取目前庫存快照（含 version）
                    BranchInventory current = branchInventoryDao
                            .findByProductIdAndGlobalAreaId(PRODUCT_ID, GLOBAL_AREA_ID)
                            .orElseThrow();

                    // 帶著 version 執行扣減（樂觀鎖）
                    // SQL: UPDATE ... AND version = :oldVersion → 衝突時回傳 0
                    int rows = branchInventoryDao.updateBranchStock(
                            PRODUCT_ID, GLOBAL_AREA_ID, 1, current.getVersion());

                    if (rows > 0) successCount.incrementAndGet();
                    else failCount.incrementAndGet();

                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // ── 3. 所有 Thread 就位後，同時放行 ──────────────────────────────────
        readyLatch.await();
        System.out.println("【起跑】" + THREAD_COUNT + " 個請求同時發出！");
        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // ── 4. 查詢最終庫存 ───────────────────────────────────────────────────
        BranchInventory finalInv = branchInventoryDao
                .findByProductIdAndGlobalAreaId(PRODUCT_ID, GLOBAL_AREA_ID)
                .orElseThrow();

        int finalStock = finalInv.getStockQuantity();
        System.out.println("════════════════════════════════════════════");
        System.out.println("✅ 下單成功：" + successCount.get() + " 筆");
        System.out.println("❌ 下單失敗（版本衝突）：" + failCount.get() + " 筆");
        System.out.println("📦 最終庫存：" + finalStock);
        System.out.println("🔒 守恆驗算：成功 " + successCount.get()
                + " + 剩餘 " + finalStock + " = " + (successCount.get() + finalStock)
                + "（初始 " + INITIAL_STOCK + "）");
        System.out.println("════════════════════════════════════════════");

        // ── 5. 測試後復原庫存 ─────────────────────────────────────────────────
        finalInv.setStockQuantity(originalStock);
        branchInventoryDao.save(finalInv);

        // ── 6. 驗證斷言 ────────────────────────────────────────────────────────
        // 核心：庫存不能為負數（不超賣）
        assertTrue(finalStock >= 0,
                "❌ 超賣！庫存變成負數：" + finalStock);

        // 核心：成功筆數不能超過初始庫存
        assertTrue(successCount.get() <= INITIAL_STOCK,
                "❌ 成功筆數 " + successCount.get() + " 超過初始庫存 " + INITIAL_STOCK);

        // 守恆：成功扣的 + 剩餘的 == 初始庫存（沒有任何憑空消失或多出）
        assertEquals(INITIAL_STOCK, successCount.get() + finalStock,
                "❌ 庫存守恆失敗！成功 " + successCount.get() + " + 剩餘 " + finalStock
                        + " ≠ 初始 " + INITIAL_STOCK);

        System.out.println("✅ 測試通過！樂觀鎖正常，無超賣！");
    }
}
