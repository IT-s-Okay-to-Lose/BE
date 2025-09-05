package com.example.iotl.repository;

import com.example.iotl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByName(String name);
    Optional<User> findOptionalByUsername(String username);

    @Query("SELECT u.createdAt FROM User u WHERE u.username = :username")
    Date getGeneratedAtByUsername(@Param("username") String username);
}
