package com.example.global_meals_gradle.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "branch_inventory")
public class BranchInventory {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	
	@Column(name = "product_id")
    private int productId;
	
	@Column(name = "branch_id")
    private int branchId;

    @Column(name = "stock_quantity")
    private int stockQuantity;

    @Column(name = "version")
    private int version;

    @UpdateTimestamp // 每次更新資料時，Hibernate 會自動幫你填入當前時間
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
