package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Members;

@Repository
public interface MembersDao extends JpaRepository<Members, Integer> {

	/* 根據 id 取的該筆會員資料 */
	@Query(value = "select * from members where id = ?1", nativeQuery = true)
	public Members findById(int id);

	/* 加點：點數 1~8，或點數>=10且券已開 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count + 1 WHERE id = ?1 "
			+ "AND ((order_count < 9) OR (order_count >= 10 AND is_discount = true))", nativeQuery = true)
	public int addPoint(int id);

	/* 加次數並開券：次數剛好是9，加完變10並打開優惠券 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count + 1, "
			+ "is_discount = true "
			+ "WHERE id = ?1 AND order_count = 9 AND is_discount = false", nativeQuery = true)
	public int reachFullPointsAndGiveCoupon(int id);
	
	/* 還原優惠券與點數邏輯(該訂單有使用優惠劵)給一張優惠劵跟點數變10 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members " +
	               "SET is_discount = true, order_count = 10 " +
	               "WHERE id = ?1", nativeQuery = true)
	int restoreCouponAndPoints(int id);

	/* 撤銷訂單時回扣次數(會自己判斷，類似switch 或 if-else if)(該訂單未使用優惠劵) */
	/*
	 * CASE WHEN 條件1 THEN 結果1 WHEN 條件2 THEN 結果2 ELSE 預設結果 END
	 * 只有再次數 = 10 的情況下，再減少1次次數，才會動到 is_discount，其他情況就不會動到
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET is_discount = CASE WHEN order_count = 10 THEN false "
			+ "ELSE is_discount END, order_count = order_count - 1 "
			+ "WHERE id = ?1 AND order_count > 0", nativeQuery = true)
	public int smartReducePoint(int id);

	/* 使用優惠劵: 次數歸0，優惠劵關閉 */
	// is_discount = true: 多一層判斷，需要優惠劵是開啟的狀態，才能關閉
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = 0, is_discount = false "
			+ "WHERE id = ?1 AND is_discount = true", nativeQuery = true)
	public int useDiscount(int id);
	
	//=============================================================================================================
	// 昱文
	
	// 訪客註冊：強制 id = 1，若衝突則更新(覆蓋)
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO members (id, name, phone, password, order_count, is_discount, created_at) "
			+ " values (1, ?1, ?2, NULL, 0, false, CURDATE()) ON DUPLICATE KEY "
			+ " UPDATE name = ?1, phone = ?2, password = NULL, "
			+ " order_count = 0, is_discount = false, created_at = CURDATE()", nativeQuery = true)
	public void registerGuest(String name, String phone);
	
	// 會員註冊
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO members (name, phone, password, order_count, is_discount, created_at) "
			+ " values (?1, ?2, ?3, 0, false, CURDATE())", nativeQuery = true)
	public void registerMember(String name, String phone, String password);
	
	// 查詢(根據手機號碼)
	@Query(value = "SELECT * FROM members WHERE phone = ?1", nativeQuery = true)
	public Members getByPhone(String phone);
	
	// 檢查除了 id=1 以外，是否有其他會員使用此手機 (防止訪客覆蓋到正式會員的手機號碼)
    @Query(value = "SELECT * FROM members WHERE phone = ?1 AND id != 1", nativeQuery = true)
    public Members getByPhoneExcludeGuest(String phone);
	
	// 會員修改密碼
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET password = ?2 WHERE id = ?1", nativeQuery = true)	
	public int updatePassword(int id, String newPassword);
	
}
