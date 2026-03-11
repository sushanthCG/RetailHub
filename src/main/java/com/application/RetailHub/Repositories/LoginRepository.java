package com.application.RetailHub.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.RetailHub.Entities.User;

public interface LoginRepository extends JpaRepository<User,Integer> {
	
   Optional<User> findByEmail(String email);
	
}
