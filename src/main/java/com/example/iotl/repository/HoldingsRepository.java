package com.example.iotl.repository;

import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface HoldingsRepository extends JpaRepository<Holdings,Long> {
    List<Holdings> findByUser_username(String username);
    Optional<Holdings> findByUser_UsernameAndStock_StockCode(String username, String stockCode);
    Optional<Holdings> findByUserAndStock(User user, Stocks stock);
    Optional<StockDetail> findTopByStock_StockCodeOrderByCreatedAtDesc(String stockCode);
    List<Holdings> findByUser_Name(String name);
    List<Holdings> findByUser(User user);
    /**
     * [수정안 D] 보유 수량만 스칼라로 조회한다.
     *
     * <p>주문 접수 시 사전검증에 사용한다. Holdings <b>엔티티를 반환하지 않으므로
     * 영속성 컨텍스트에 managed 상태로 올라가지 않는다.</b>
     *
     * <p>왜 필요한가: 사전검증이 엔티티를 managed 로 만들면, 같은 트랜잭션의
     * 체결 단계에서 {@link #findByUserAndStockWithPessimisticLock}(잠금 조회)를
     * 호출해도 1차 캐시의 낡은 값이 반환된다(PersistenceContextProbeTest 실측).
     * 그 값으로 갱신하면 다른 트랜잭션의 커밋이 덮어써진다(lost update).
     *
     * <p>이 조회는 <b>advisory validation</b>이다. 실제 자산 변경 판단은
     * 체결 단계의 잠금 조회를 authoritative validation 으로 사용한다.
     */
    @Query("select h.quantity from Holdings h where h.user = :user and h.stock = :stock")
    Optional<Integer> findQuantityByUserAndStock(@Param("user") User user,
        @Param("stock") Stocks stock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Holdings h where h.user = :user and h.stock = :stock")
    Optional<Holdings> findByUserAndStockWithPessimisticLock(@Param("user") User user, @Param("stock") Stocks stock);
}
