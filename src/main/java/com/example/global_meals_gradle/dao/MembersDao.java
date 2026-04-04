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

	/* 減少該會員次數 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count - 1 "
			+ "WHERE id = ?1 AND order_count > 0", nativeQuery = true)
	public int reducePoint(int id);

	/* 次數 = 10 時，減少該會員點數並關閉優惠劵 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count - 1,"
			+ "is_discount = false "
			+ "WHERE id = ?1 AND order_count = 10 AND is_discount = true", nativeQuery = true)
	public int reducePointClose(int id);

	/* 撤銷訂單時回扣點數(會自己判斷，類似switch 或 if-else if) */
	/*
	 * CASE WHEN 條件1 THEN 結果1 WHEN 條件2 THEN 結果2 ELSE 預設結果 END
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET is_discount = CASE WHEN order_count = 10 THEN false "
			+ "ELSE is_discount END, order_count = order_count - 1 "
			+ "WHERE id = ?1 AND order_count > 0", nativeQuery = true)
	public int smartReducePoint(int id);

	/* 使用8折劵: 次數歸1，優惠劵關閉 */
	// is_discount = true: 多一層判斷，需要優惠劵是開啟的狀態，才能關閉
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = 1, is_discount = false "
			+ "WHERE id = ?1 AND is_discount = true", nativeQuery = true)
	public int useDiscount(int id);
}
