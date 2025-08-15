package com.capston.matching_app.repository;

import com.capston.matching_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Integer> {}