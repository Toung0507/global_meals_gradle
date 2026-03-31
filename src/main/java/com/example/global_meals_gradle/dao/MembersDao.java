package com.example.global_meals_gradle.dao;

import com.example.global_meals_gradle.entity.Members;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembersDao extends JpaRepository<Members, Integer> {
    // 繼承 JpaRepository 就能直接使用 findById(Integer id) 和 save(Members entity)
	//findMemberById
}
