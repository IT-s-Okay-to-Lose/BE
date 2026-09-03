package com.example.iotl.trading;

import com.example.iotl.service.OrderMatchingService;
import com.example.iotl.service.OrderService;
import com.example.iotl.service.TradeService;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거래 정합성 재현 테스트용 최소 컨텍스트.
 *
 * <p>기존 {@code @SpringBootTest}는 OAuth2 / KIS / JWT / Sentry 의 placeholder(${DB_URL},
 * ${JWT_SECRET} ...)가 .env 부재로 해석되지 않아 ApplicationContext 로딩 단계에서 실패한다.
 * (baseline-concurrency-test.log 의 PlaceholderResolutionException)
 *
 * <p>따라서 JPA 슬라이스만 띄우고, 거래 도메인 서비스 3개만 @Import 한다.
 * 이 서비스들의 의존성은 전부 JpaRepository 이므로 슬라이스 안에서 해결된다.
 *
 * <p>중요한 두 가지 설정:
 * <ul>
 *   <li>{@code replace = NONE} : 내장 H2로 갈아끼우지 않고 실제 MySQL(3307)을 쓴다.
 *       비관적 락/갭락/데드락 같은 InnoDB 고유 동작을 재현해야 하므로 필수다.</li>
 *   <li>{@code propagation = NOT_SUPPORTED} : @DataJpaTest 는 기본으로 테스트 메서드를
 *       트랜잭션으로 감싸고 롤백한다. 그러면 별도 스레드에서 연 트랜잭션이 테스트가 넣은
 *       데이터를 볼 수 없어 동시성 재현이 불가능하다. 그래서 테스트 트랜잭션을 끄고
 *       각 테스트가 @AfterEach 에서 직접 정리한다.</li>
 * </ul>
 *
 * <p>production 코드는 일절 수정하지 않는다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("tradetest")
@Import({TradeService.class, OrderMatchingService.class, OrderService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public abstract class TradingSliceTest {
}
