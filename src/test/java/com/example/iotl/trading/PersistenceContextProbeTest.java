package com.example.iotl.trading;

import com.example.iotl.entity.Accounts;
import com.example.iotl.entity.Holdings;
import com.example.iotl.entity.Stocks;
import com.example.iotl.entity.User;
import com.example.iotl.repository.AccountsRepository;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.repository.OrderRepository;
import com.example.iotl.repository.StocksRepository;
import com.example.iotl.repository.TradeRepository;
import com.example.iotl.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * [probe] 영속성 컨텍스트 + 비관적 락 상호작용 검증.
 *
 * <p>실제 코드 경로(확인됨):
 * <ul>
 *   <li>{@code OrderService.placeOrder:74} — SELL 주문 시 Holdings 를 <b>일반 조회</b>로
 *       읽어 영속성 컨텍스트에 managed 상태로 올린다</li>
 *   <li>{@code TradeService.trade:60} — <b>같은 트랜잭션</b>에서 같은 row 를
 *       {@code PESSIMISTIC_WRITE} 로 다시 조회한다</li>
 * </ul>
 *
 * <p>질문: 이미 managed 인 엔티티를 잠금 조회하면 <b>DB 최신값</b>을 받는가,
 * 아니면 <b>1차 캐시의 낡은 값</b>을 받는가?
 *
 * <p>가정하지 않고 측정한다. managed 값과 DB 값이 같으면 이 가설을 기각한다.
 * production 코드는 수정하지 않는다.
 */
@DisplayName("[probe] Persistence Context + PESSIMISTIC_WRITE")
class PersistenceContextProbeTest extends TradingSliceTest {

    @Autowired HoldingsRepository holdingsRepository;
    @Autowired AccountsRepository accountsRepository;
    @Autowired UserRepository userRepository;
    @Autowired StocksRepository stocksRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired TradeRepository tradeRepository;
    @Autowired TransactionTemplate txTemplate;
    @Autowired EntityManager em;
    @Autowired DataSource dataSource;

    private static final BigDecimal PRICE = new BigDecimal("1000");
    private User owner;
    private Stocks stock;

    @BeforeEach
    void setUp() {
        cleanUp();
        owner = userRepository.save(User.builder()
            .username("PC_" + System.nanoTime()).name("owner").role("USER")
            .email("pc_" + System.nanoTime() + "@t.com").build());
        accountsRepository.save(Accounts.builder().user(owner)
            .balance(new BigDecimal("1000000.00")).realizedProfit(BigDecimal.ZERO).build());
        stock = stocksRepository.save(Stocks.builder()
            .stockCode("P" + (System.nanoTime() % 100000000)).stockName("probe").build());
        holdingsRepository.save(Holdings.builder().user(owner).stock(stock)
            .quantity(100).averageBuyPrice(PRICE).build());
    }

    @AfterEach
    void tearDown() { cleanUp(); }

    private void cleanUp() {
        tradeRepository.deleteAll(); orderRepository.deleteAll();
        holdingsRepository.deleteAll(); accountsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("일반 조회로 managed 된 뒤 다른 트랜잭션이 커밋하면, 잠금 재조회는 어느 값을 보는가")
    void managedEntityVsLockingRead() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // (1) OrderService.placeOrder 와 동일하게 "일반 조회"로 managed 상태를 만든다
            Holdings first = holdingsRepository.findByUserAndStock(owner, stock).orElseThrow();
            observed.put("1_plain_read", String.valueOf(first.getQuantity()));

            // (2) 다른 트랜잭션이 같은 row 를 변경하고 커밋한다
            commitInOtherTx();
            observed.put("2_db_after_other_commit", String.valueOf(readViaJdbc()));

            // (3) TradeService.trade 와 동일하게 PESSIMISTIC_WRITE 로 재조회
            Holdings locked = holdingsRepository
                .findByUserAndStockWithPessimisticLock(owner, stock).orElseThrow();
            observed.put("3_after_pessimistic_read", String.valueOf(locked.getQuantity()));
            observed.put("3_same_instance", String.valueOf(locked == first));

            // (4) 별도 JDBC 로 읽은 실제 DB 값
            observed.put("4_raw_jdbc", String.valueOf(readViaJdbc()));

            // (5) refresh 후 값
            em.refresh(locked, LockModeType.PESSIMISTIC_WRITE);
            observed.put("5_after_refresh", String.valueOf(locked.getQuantity()));

            return null;
        });

        System.out.println("\n===== [probe] 영속성 컨텍스트 vs 비관적 락 =====");
        System.out.println("경로: OrderService:74(일반조회) -> TradeService:60(잠금조회), 같은 트랜잭션");
        observed.forEach((k, v) -> System.out.printf("  %-28s %s%n", k, v));
        System.out.println();
        String plain = observed.get("1_plain_read");
        String afterLock = observed.get("3_after_pessimistic_read");
        String db = observed.get("4_raw_jdbc");
        if (afterLock.equals(db)) {
            System.out.println(">>> 잠금 재조회가 DB 최신값을 반영했다 -> 이 가설 기각");
        } else if (afterLock.equals(plain)) {
            System.out.println(">>> 잠금 재조회가 1차 캐시의 낡은 값을 반환했다");
            System.out.println(">>> managed=" + afterLock + " 인데 실제 DB=" + db);
            System.out.println(">>> 이후 이 값으로 계산·저장하면 다른 트랜잭션의 변경을 덮어쓴다 (lost update)");
        }
        System.out.println("=================================================\n");

        Map<String, String> rec = RunContext.base("PC_PROBE", 2, 1);
        rec.putAll(observed);
        rec.put("verdict", afterLock.equals(db) ? "REJECTED(DB값 반영)" : "STALE(1차 캐시 값)");
        RunContext.append("seed1000_investigation.csv", rec);
    }

    @Test
    @DisplayName("[인과] 일반 조회(SELL 사전검증)를 거치지 않으면 잠금 조회가 최신값을 보는가")
    void withoutPriorPlainRead_lockingReadSeesLatest() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // BUY 주문 경로: placeOrder 가 Holdings 를 일반 조회하지 않는다.
            // 따라서 영속성 컨텍스트에 Holdings 가 올라오지 않은 상태다.
            commitInOtherTx();   // 다른 트랜잭션이 100 -> 93 으로 변경 후 커밋
            observed.put("1_db_after_other_commit", String.valueOf(readViaJdbc()));

            // 첫 접촉이 잠금 조회인 경우
            Holdings locked = holdingsRepository
                .findByUserAndStockWithPessimisticLock(owner, stock).orElseThrow();
            observed.put("2_first_touch_is_locking_read", String.valueOf(locked.getQuantity()));
            observed.put("3_raw_jdbc", String.valueOf(readViaJdbc()));
            return null;
        });

        System.out.println("\n===== [인과] 사전 일반 조회가 없을 때 =====");
        observed.forEach((k, v) -> System.out.printf("  %-32s %s%n", k, v));
        boolean sees = observed.get("2_first_touch_is_locking_read")
            .equals(observed.get("3_raw_jdbc"));
        System.out.println(sees
            ? ">>> 최신값을 본다. 즉 문제는 '잠금 조회' 자체가 아니라\n"
              + ">>> '이미 managed 인 엔티티를 잠금 조회'하는 경우에만 발생한다."
            : ">>> 최신값을 보지 못한다. 다른 원인이 있다.");
        System.out.println("==========================================\n");

        Map<String, String> rec = RunContext.base("PC_PROBE_CAUSAL", 2, 1);
        rec.putAll(observed);
        rec.put("verdict", sees ? "사전 일반조회가 조건" : "잠금조회 자체 문제");
        RunContext.append("seed1000_investigation.csv", rec);
    }

    @Test
    @DisplayName("[후보D 전제 검증] scalar 조회는 영속성 컨텍스트에 엔티티를 올리지 않는가")
    void scalarProjectionDoesNotManageEntity() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // 후보 D 가 쓰려는 방식: 엔티티가 아니라 quantity 스칼라만 조회
            Integer qty = em.createQuery(
                    "SELECT h.quantity FROM Holdings h "
                        + "WHERE h.user = :u AND h.stock = :s", Integer.class)
                .setParameter("u", owner).setParameter("s", stock)
                .getSingleResult();
            observed.put("1_scalar_read", String.valueOf(qty));

            // 이 시점에 Holdings 엔티티가 managed 인가?
            observed.put("2_note", "scalar 조회이므로 엔티티가 로드되지 않음");

            // 다른 트랜잭션이 변경 후 커밋
            commitInOtherTx();
            observed.put("3_db_after_other_commit", String.valueOf(readViaJdbc()));

            // 잠금 조회가 최신값을 보는가?
            Holdings locked = holdingsRepository
                .findByUserAndStockWithPessimisticLock(owner, stock).orElseThrow();
            observed.put("4_after_pessimistic_read", String.valueOf(locked.getQuantity()));
            observed.put("5_raw_jdbc", String.valueOf(readViaJdbc()));
            return null;
        });

        System.out.println("\n===== [후보D 전제] scalar 조회 후 잠금 조회 =====");
        observed.forEach((k, v) -> System.out.printf("  %-32s %s%n", k, v));
        boolean ok = observed.get("4_after_pessimistic_read").equals(observed.get("5_raw_jdbc"));
        System.out.println(ok
            ? ">>> scalar 조회는 엔티티를 managed 로 만들지 않아 잠금 조회가 최신값을 본다."
            : ">>> scalar 조회로도 stale 이 발생한다. 후보 D 전제가 틀렸다.");
        System.out.println("================================================\n");

        Map<String, String> rec = RunContext.base("CANDIDATE_D_PREMISE", 2, 1);
        rec.putAll(observed);
        rec.put("verdict", ok ? "전제 성립" : "전제 불성립");
        RunContext.append("seed1000_investigation.csv", rec);
    }

    @Test
    @DisplayName("[D 적용 검증] 실제 placeOrder 경로에서 stale managed 가 사라졌는가")
    void afterFixD_realPathHasNoStaleManagedEntity() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // OrderService.placeOrder 의 SELL 사전검증과 동일한 호출
            int qty = holdingsRepository.findQuantityByUserAndStock(owner, stock).orElseThrow();
            observed.put("1_scalar_quantity", String.valueOf(qty));

            // 사전검증 후 Holdings 엔티티가 영속성 컨텍스트에 있는가?
            Holdings probe = em.getReference(Holdings.class, holdingsIdOf());
            boolean containsBeforeLock = em.contains(probe) && isInitialized(probe);
            observed.put("2_pc_contains_entity_after_scalar",
                String.valueOf(containsBeforeLock));

            // 다른 트랜잭션이 변경 후 커밋
            commitInOtherTx();
            observed.put("3_db_after_other_commit", String.valueOf(readViaJdbc()));

            // 체결 단계의 잠금 조회
            Holdings locked = holdingsRepository
                .findByUserAndStockWithPessimisticLock(owner, stock).orElseThrow();
            observed.put("4_locking_entity_read", String.valueOf(locked.getQuantity()));
            observed.put("5_raw_jdbc", String.valueOf(readViaJdbc()));
            observed.put("6_pc_contains_after_lock", String.valueOf(em.contains(locked)));
            return null;
        });

        System.out.println("\n===== [D 적용 검증] 실제 placeOrder 경로 =====");
        observed.forEach((k, v) -> System.out.printf("  %-36s %s%n", k, v));
        boolean fixed = observed.get("4_locking_entity_read").equals(observed.get("5_raw_jdbc"));
        System.out.println(fixed
            ? ">>> 잠금 조회가 DB 최신값을 본다. 확정 원인 경로가 제거됐다."
            : ">>> 여전히 stale 값을 본다. 수정이 효과 없다.");
        System.out.println("=============================================\n");

        Map<String, String> rec = RunContext.base("FIX_D_PROBE", 2, 1);
        rec.putAll(observed);
        rec.put("verdict", fixed ? "원인 경로 제거됨" : "미해결");
        RunContext.append("fix_d_verification.csv", rec);
    }

    @Test
    @DisplayName("[Accounts 경로] User 조회가 Accounts 를 managed 로 만드는가")
    void userLoadEagerlyManagesAccounts() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // OrderService.placeOrder 첫 줄과 동일: findByUsername 으로 User 조회
            User u = userRepository.findByUsername(owner.getUsername());
            observed.put("1_user_loaded", u == null ? "null" : "ok");

            // User.account 는 @OneToOne(mappedBy) 이고 fetch 미지정 -> 기본 EAGER
            // 즉 User 조회만으로 Accounts 가 함께 로드될 수 있다.
            Accounts acc = u.getAccount();
            boolean managed = acc != null && em.contains(acc);
            observed.put("2_accounts_managed_after_user_load", String.valueOf(managed));
            observed.put("3_balance_in_pc",
                acc == null ? "null" : acc.getBalance().toPlainString());

            // 다른 트랜잭션이 잔액을 바꾸고 커밋
            commitBalanceInOtherTx();
            observed.put("4_db_after_other_commit", String.valueOf(readBalanceViaJdbc()));

            // 체결 단계와 동일하게 잠금 조회
            Accounts locked = accountsRepository.findByUserWithPessimisticLock(owner)
                .orElseThrow();
            observed.put("5_locking_read", locked.getBalance().toPlainString());
            observed.put("6_raw_jdbc", String.valueOf(readBalanceViaJdbc()));
            return null;
        });

        System.out.println("\n===== [Accounts 경로] User EAGER 로딩 =====");
        observed.forEach((k, v) -> System.out.printf("  %-40s %s%n", k, v));
        String lock = observed.get("5_locking_read");
        String db = observed.get("6_raw_jdbc");
        boolean stale = !lock.startsWith(db.split("\\.")[0]);
        System.out.println(stale
            ? ">>> 잠금 조회가 낡은 값을 본다. Holdings 와 동일한 메커니즘이 Accounts 에도 있다."
            : ">>> 잠금 조회가 최신값을 본다. Accounts 는 이 경로가 아니다.");
        System.out.println("==========================================\n");

        Map<String, String> rec = RunContext.base("ACCOUNTS_PATH_PROBE", 2, 1);
        rec.putAll(observed);
        RunContext.append("fix_d_verification.csv", rec);
    }

    @Test
    @DisplayName("[LAZY 검증] fetch=LAZY 선언이 실제로 Accounts preload 를 막는가")
    void lazyFetchActuallyPreventsAccountsPreload() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // OrderService.placeOrder:44 와 동일
            User u = userRepository.findByUsername(owner.getUsername());

            // (1) provider 가 실제로 account 를 로드했는가?
            //     선언만 믿지 않고 PersistenceUnitUtil 로 측정한다.
            var puu = em.getEntityManagerFactory().getPersistenceUnitUtil();
            observed.put("1_isLoaded_account", String.valueOf(puu.isLoaded(u, "account")));

            // (2) Accounts 가 영속성 컨텍스트에 managed 인가?
            Accounts accRef = u.getAccount();
            observed.put("2_account_ref_class",
                accRef == null ? "null" : accRef.getClass().getSimpleName());
            observed.put("3_em_contains_account",
                String.valueOf(accRef != null && em.contains(accRef)));
            observed.put("4_hibernate_initialized",
                String.valueOf(accRef != null
                    && org.hibernate.Hibernate.isInitialized(accRef)));

            // (3) 다른 트랜잭션이 balance 를 바꾸고 커밋
            commitBalanceInOtherTx();
            observed.put("5_db_after_other_commit", readBalanceViaJdbc());

            // (4) 체결 단계와 동일한 잠금 조회
            Accounts locked = accountsRepository.findByUserWithPessimisticLock(owner)
                .orElseThrow();
            observed.put("6_locking_read", locked.getBalance().toPlainString());
            observed.put("7_raw_jdbc", readBalanceViaJdbc());
            return null;
        });

        System.out.println("\n===== [LAZY 검증] User.account fetch=LAZY =====");
        observed.forEach((k, v) -> System.out.printf("  %-32s %s%n", k, v));
        BigDecimal lock = new BigDecimal(observed.get("6_locking_read"));
        BigDecimal db = new BigDecimal(observed.get("7_raw_jdbc"));
        boolean fixed = lock.compareTo(db) == 0;
        System.out.println(fixed
            ? ">>> 잠금 조회가 DB 최신값을 본다. LAZY 가 실제로 preload 를 막았다."
            : ">>> 여전히 낡은 값을 본다. LAZY 선언만으로는 막히지 않는다.");
        System.out.println("   (mappedBy 인 @OneToOne 은 provider 가 LAZY 를 무시할 수 있다)");
        System.out.println("==============================================\n");

        Map<String, String> rec = RunContext.base("LAZY_FETCH_PROBE", 2, 1);
        rec.putAll(observed);
        rec.put("verdict", fixed ? "LAZY 유효" : "LAZY 무효");
        RunContext.append("fix_e_verification.csv", rec);
    }

    @Test
    @DisplayName("[후보 B 전제] userId scalar 조회 + getReference 는 Accounts 를 로드하지 않는가")
    void userReferenceDoesNotLoadAccounts() {
        Map<String, String> observed = new LinkedHashMap<>();

        txTemplate.execute(status -> {
            // User 엔티티 전체를 조회하지 않고 식별자만 스칼라로 얻는다.
            Long userId = em.createQuery(
                    "SELECT u.userId FROM User u WHERE u.username = :n", Long.class)
                .setParameter("n", owner.getUsername()).getSingleResult();
            observed.put("1_scalar_user_id", String.valueOf(userId));

            // 연관관계 설정용 프록시 참조 (SELECT 발생하지 않음)
            User ref = em.getReference(User.class, userId);
            observed.put("2_user_ref_initialized",
                String.valueOf(org.hibernate.Hibernate.isInitialized(ref)));

            // 다른 트랜잭션이 balance 변경 후 커밋
            commitBalanceInOtherTx();
            observed.put("3_db_after_other_commit", readBalanceViaJdbc());

            // 체결 단계와 동일한 잠금 조회
            Accounts locked = accountsRepository.findByUserWithPessimisticLock(owner)
                .orElseThrow();
            observed.put("4_locking_read", locked.getBalance().toPlainString());
            observed.put("5_raw_jdbc", readBalanceViaJdbc());
            return null;
        });

        System.out.println("\n===== [후보 B 전제] userId scalar + getReference =====");
        observed.forEach((k, v) -> System.out.printf("  %-32s %s%n", k, v));
        BigDecimal lock = new BigDecimal(observed.get("4_locking_read"));
        BigDecimal db = new BigDecimal(observed.get("5_raw_jdbc"));
        boolean ok = lock.compareTo(db) == 0;
        System.out.println(ok
            ? ">>> 잠금 조회가 DB 최신값을 본다. 이 방식은 원인 경로를 제거한다."
            : ">>> 여전히 낡은 값. 이 방식도 무효.");
        System.out.println("====================================================\n");

        Map<String, String> rec = RunContext.base("USER_REF_PROBE", 2, 1);
        rec.putAll(observed);
        rec.put("verdict", ok ? "전제 성립" : "전제 불성립");
        RunContext.append("fix_e_verification.csv", rec);
    }

    private void commitBalanceInOtherTx() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            pool.submit(() -> txTemplate.execute(st -> {
                Accounts a = accountsRepository.findByUserWithPessimisticLock(owner).orElseThrow();
                a.setBalance(a.getBalance().subtract(new BigDecimal("5000.00")));
                accountsRepository.saveAndFlush(a);
                return null;
            })).get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("별도 트랜잭션 실패", e);
        } finally { pool.shutdown(); }
    }

    private String readBalanceViaJdbc() {
        try (var conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery(
                "SELECT balance FROM accounts WHERE user_id = " + owner.getUserId())) {
                return rs.next() ? rs.getString(1) : "-1";
            }
        } catch (Exception e) {
            throw new IllegalStateException("JDBC 조회 실패", e);
        }
    }

    private Long holdingsIdOf() {
        return em.createQuery(
                "SELECT h.holdingsId FROM Holdings h WHERE h.user = :u AND h.stock = :s",
                Long.class)
            .setParameter("u", owner).setParameter("s", stock).getSingleResult();
    }

    private boolean isInitialized(Object entity) {
        return org.hibernate.Hibernate.isInitialized(entity);
    }

    /** 별도 스레드의 독립 트랜잭션에서 Holdings 를 변경하고 커밋한다. */
    private void commitInOtherTx() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            pool.submit(() -> txTemplate.execute(st -> {
                Holdings h = holdingsRepository.findByUserAndStock(owner, stock).orElseThrow();
                h.setQuantity(h.getQuantity() - 7);   // 100 -> 93
                holdingsRepository.saveAndFlush(h);
                return null;
            })).get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("별도 트랜잭션 실패", e);
        } finally {
            pool.shutdown();
        }
    }

    /** 영속성 컨텍스트를 우회해 실제 DB 값을 읽는다. */
    private int readViaJdbc() {
        try (var conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery(
                "SELECT quantity FROM holdings WHERE user_id = " + owner.getUserId()
                    + " AND stock_code = '" + stock.getStockCode() + "'")) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (Exception e) {
            throw new IllegalStateException("JDBC 조회 실패", e);
        }
    }
}
