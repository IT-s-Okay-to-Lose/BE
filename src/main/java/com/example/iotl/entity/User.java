package com.example.iotl.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    private String name;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holdings> holdings = new ArrayList<>();

    /**
     * [수정] fetch = LAZY 를 명시한다.
     *
     * <p>@OneToOne 의 JPA 기본 fetch 는 EAGER 다. 그래서 User 를 조회하기만 해도
     * Accounts 가 함께 영속성 컨텍스트에 managed 상태로 올라갔다
     * (ACCOUNTS_PATH_PROBE 실측: User 로드 직후 em.contains(account)=true).
     *
     * <p>그 결과 OrderService.placeOrder 의 findByUsername 이
     * Accounts 를 미리 managed 로 만들고, 같은 트랜잭션의 체결 단계에서
     * accountsRepository.findByUserWithPessimisticLock 을 호출해도
     * 1차 캐시의 낡은 balance 가 반환되어 lost update 가 발생했다
     * (managed=1,000,000 / DB=995,000 / 잠금조회 후=1,000,000).
     *
     * <p>Holdings 에서 확인된 것과 동일한 메커니즘이며, 진입점만 다르다.
     * Holdings 는 명시적 엔티티 조회였고, Accounts 는 이 EAGER 연관관계다.
     */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY,
        cascade = CascadeType.ALL, orphanRemoval = true)
    private Accounts account;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    public void setAccount(Accounts account) {
        this.account = account;
        if (account.getUser() != this) {
            account.setUser(this);
        }
    }




}
