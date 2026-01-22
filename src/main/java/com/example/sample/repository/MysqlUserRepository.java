package com.example.sample.repository;

import com.example.sample.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MysqlUserRepository extends JpaRepository<User, Long> {
}
