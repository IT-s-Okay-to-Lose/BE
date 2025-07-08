package com.example.iotl.repository;

import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountsRepository extends JpaRepository<Accounts,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Accounts a where a.user = :user")
    Optional<Accounts> findByUserWithPessimisticLock(@Param("user") User user);

}
