package com.example.iotl.repository;

import com.example.iotl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT u.createdAt FROM User u WHERE u.username = :username")
    Date getGeneratedAtByUsername(@Param("username") String username);
}
