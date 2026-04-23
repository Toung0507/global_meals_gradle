package com.example.global_meals_gradle.res;

import java.util.List;
import com.example.global_meals_gradle.vo.BranchInventoryVO;

public class BranchInventoryRes extends BasicRes {

	private List<BranchInventoryVO> inventory;

	public BranchInventoryRes(int code, String message, List<BranchInventoryVO> inventory) {
		super(code, message);
		this.inventory = inventory;
	}

	public List<BranchInventoryVO> getInventory() { return inventory; }
	public void setInventory(List<BranchInventoryVO> inventory) { this.inventory = inventory; }
}
