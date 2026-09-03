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

    @Query("SELECT u.createdAt FROM User u WHERE u.username = :username")
    Date getGeneratedAtByUsername(@Param("username") String username);

    /**
     * [수정] username 으로 식별자만 스칼라 조회한다.
     *
     * <p>{@link #findByUsername}은 User 엔티티를 로드하는데, User.account 가
     * mappedBy 인 @OneToOne 이라 <b>fetch=LAZY 를 선언해도 provider 가 함께 로드</b>한다
     * (LAZY_FETCH_PROBE 실측: isLoaded=true). 그러면 Accounts 가 영속성 컨텍스트에
     * managed 로 올라가고, 같은 트랜잭션의 체결 단계에서 잠금 조회를 해도
     * 1차 캐시의 낡은 balance 가 반환되어 lost update 가 발생한다.
     *
     * <p>식별자만 조회하면 User 엔티티 자체가 로드되지 않으므로
     * Accounts 도 딸려오지 않는다.
     */
    @Query("SELECT u.userId FROM User u WHERE u.username = :username")
    Optional<Long> findUserIdByUsername(@Param("username") String username);
}
