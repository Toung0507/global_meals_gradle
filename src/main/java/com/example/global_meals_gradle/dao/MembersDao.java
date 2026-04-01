package com.example.global_meals_gradle.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.Members;

@Repository
public interface MembersDao extends JpaRepository<Members, Integer> {

	/* 根據 id 取的該筆會員資料，並鎖定*/
	@Query(value = "select * from members where id = ?1", nativeQuery = true)
	public Members findById(int id);
	
	/* 根據 id 取的該筆會員資料，並鎖定*/
	@Query(value = "select * from members where id = ?1 for update", nativeQuery = true)
	public Members findByIdForUpdate(int id);

	/* 增加該會員點數 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count + 1 WHERE id = ?1 "
			+ " AND order_count < 10", nativeQuery = true)
	public void addPoint(int id);

	/* 增加該會員點數並把9折劵打開 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count + 1, is_discount = true WHERE id = ?1", nativeQuery = true)
	public void reachFullPointsAndGiveCoupon(int id);
	
	/* 減少該會員點數 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE members SET order_count = order_count - 1 WHERE id = ?1", nativeQuery = true)
	public void reducePoint(int id);

	
}
