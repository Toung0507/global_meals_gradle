package com.example.global_meals_gradle.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.global_meals_gradle.entity.OrderCartDetails;

@Repository
public interface OrderCartDetailsDao extends JpaRepository<OrderCartDetails, Integer> {

	/* 找出一台購物車裡面的「某個特定商品」(不包含贈品)，目的：存在則更改數量，不存在新建orderCartDetail */
	@Query(value = "SELECT * FROM order_cart_details WHERE order_cart_id = ?1 AND product_id = ?2 AND is_gift = false ", nativeQuery = true)
	public OrderCartDetails findByCartIdAndProductId(int orderCartId, int productId);

	// 把 ？號車裡『所有商品與贈品』,目的：都拿出來給我算小計！
	@Query(value = "SELECT * FROM order_cart_details WHERE order_cart_id = ?1", nativeQuery = true)
	public List<OrderCartDetails> findAllByCartId(int orderCartId);

	// 把 7 號車裡標記為『贈品(is_gift = 1)』的東西全刪了
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM order_cart_details WHERE order_cart_id = ?1 AND is_gift = 1", nativeQuery = true)
	public void deleteAllGiftsByCartId(int orderCartId);

	/* 刪除購物車裡的「某個特定商品」 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM order_cart_details WHERE order_cart_id = ?1 AND product_id = ?2", nativeQuery = true)
	public void deleteByCartIdAndProductId(int orderCartId, int productId);

	/* 清空購物車 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM order_cart_details WHERE order_cart_id = ?1", nativeQuery = true)
	void deleteAllByCartId(int orderCartId);

	/* 查找這台購物車裡已經選擇的贈品 */
	@Query(value = "SELECT * FROM order_cart_details WHERE order_cart_id = ?1 AND is_gift = 1", nativeQuery = true)
	List<OrderCartDetails> findSelectGiftsByCartId(int orderCartId);
}
