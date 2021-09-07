package teamtridy.tridy.domain.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
/*
    @DynamicUpdate 변경된 컬럼만 찾아서 업데이트를 진행한다.
        변경된 컬럼만 찾는다.
        변경되는 컬럼에따라 쿼리가 변경된다.
    이 두가지 사실만 놓고 보더라고 성능상 손해가 있을것 같다.
    그럼 언제 사용해야 좋을까?
    하나의 테이블에 정말 많은 수의 컬럼이 있는데, 몇몇개의 컬럼만 자주 업데이트 하는 경우에 사용하라고 되어있다.
    출처: https://velog.io/@freddiey/JPA%EC%9D%98-DynamicUpdate
*/
public class Account extends BaseTimeEntity {

    @Builder.Default
    @OneToMany(mappedBy = "account")
    private List<Review> reviews = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "account")
    private List<Pick> picks = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", nullable = false)
    private Long id;
    @Column(nullable = false, unique = true)
    private String socialId;
    @Column(nullable = false, unique = true)
    private String nickname;
    @Column
    private Boolean isPreferredFar;
    @Column
    private Boolean isPreferredPopular;
    @Column(nullable = false)
    private Boolean hasTendency;
    // !! @Builder 는 초기화 표현을 완전히 무시한다. 초기화 하고 싶으면 @Builder.Default 를 사용해. 아니면 final 쓰면돼
    @Builder.Default
    @OneToMany(mappedBy = "account", orphanRemoval = true)
    private List<AccountInterest> accountInterests = new ArrayList<>();

    public void updateTendency(Boolean isPreferredFar, Boolean isPreferredPopular,
            List<AccountInterest> newAccountInterest) {
        this.hasTendency = true;
        this.isPreferredFar = isPreferredFar;
        this.isPreferredPopular = isPreferredPopular;
        this.accountInterests = newAccountInterest;
    }

    /*
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
            특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶으면 영속성 전이(transitive persistence) 기능을 사용
            JPA는 CASCADE 옵션으로 영속성 전이를 제공
            영속성 전이는 연관관계를 매핑하는 것과는 아무 관련이 없다. 단지 엔티티를 영속화할 때 연관된 엔티티도 같이 영속화하는 편리함을 제공할 뿐

            CascadeType.PERSIST:
                (저장) 부모와 자식 엔티티를 한 번에 영속화 할 수 있다.
                CascadeType.PERSIST를 함부로 사용하면 안됨. 엔티티의 자식에 CascadeType.PERSIST를 지정할 경우 JPA에서 추가적으로 수행하는 동작이 있고, 이 때문에 예상치 못한 사이드 이펙트가 발생할 수 있다.
                참고: https://joont92.github.io/jpa/CascadeType-PERSIST%EB%A5%BC-%ED%95%A8%EB%B6%80%EB%A1%9C-%EC%82%AC%EC%9A%A9%ED%95%98%EB%A9%B4-%EC%95%88%EB%90%98%EB%8A%94-%EC%9D%B4%EC%9C%A0/

            CascadeType.REMOVE:
                부모 엔티티만 삭제해도 연관된 자식 엔티티도 함께 삭제 가능하다.
                삭제 순서는 외래 키 제약조건을 고려하여 자식을 먼저 삭제한 후 부모를 삭제한다.

        orphanRemoval = true
            부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제하는 기능을 고아 객체(ORPHAN) 제거
            개념적으로 부모를 제거할때도 자식은 고아가 되기 때문에 부모를 제거하면 자식도 제거된다. 이는 CascadeType.REMOVE를 설정한 것과 같다.
            고아 객체 제거는 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능이다.
            따라서 이 기능은 참조되는 곳이 하나일 때만 사용해야 한다. 만약 삭제한 엔티티가 다른 곳에서도 참조한다면 문제가 발생할 수 있다.
            post의 경우 회원이 탈퇴해도 남으므로 사용하면 안될 듯하다.
     */


    /*
        mappedBy 속성은 양방향 매핑에서 반대쪽 매핑의 필드 이름을 값으로 적용한다.
            객체에는 양방향 연관관계가 없다. 서로 다른 단방향 연관관계 2개를 로직으로 묶어준 것일 뿐이다.
            반면, 데이터베이스 테이블을 외래 키 하나로 테이블 간 양방향 연관관계 관리가 가능하다.
            따라서 객체에서 양방향 연관관계를 설정하려면 2개의 참조가 필요하지만 테이블에서는 1개일 뿐이기 떄문에 차이가 발생한다.
            이러한 차이를 JPA에서는 두 객체 연관관계 중 하나를 정해 테이블의 외래 키를 관리하도록 한다. 이 한 곳이 연관관계의 주인이 된다.

        연관관계의 주인이란 즉, 외래키 관리자를 선택하는 것
            ** 연관관계의 주인은 테이블에 외래 키가 있는 곳으로 정해야 한다. **
            실제 외래 키는 post 테이블에 있는데 account 테이블에서 설정하게 되면 물리적으로 다른 테이블에 있는 외래 키를 관리하는 형태가 된다.
            데이터베이스 테이블의 다대일/일대다 관계에서는 항상 다 쪽이 외래 키를 갖게 된다.
            따라서 @ManyToOne은 항상 연관관계의 주인이 되므로, mappedBy 속성이 없다.

        주의점
            연관관계의 주인만이 외래 키의 값을 변경할 수 있다. 주인이 아닌 곳에만 값을 입력하면 데이터베이스에 외래키 값이 정상적으로 저장되지 않는다.
            하지만, 객체 관점에서는 양쪽 방향 모두 값을 입력해주는 것이 가장 안전하다. 그렇지 않으면 JPA를 사용하지 않는 순수한 객체 상태에서는 문제가 발생할 수 있다.
            양뱡향 연관관계는 결국 양쪽 다 신경써야한다. 각각 호출하다보면 실수로 둘 중 하나만 호출해서 양방향이 깨질 수도 있다. => 연관관계 편의 메소드 사용!

       연관관계 편의 메소드
            한번에 양방향 관계를 설정하는 메소드
                양뱡향 연관관계는 결국 양쪽 다 신경써야한다. 각각 호출하다보면 실수로 둘 중 하나만 호출해서 양방향이 깨질 수도 있다.
                Post의 setAccount를 수정해 두 코드를 하나인 것 처럼 사용하는 것이 안전하다.

        일대다 단방향 매핑은 UPDATE SQL이 한번 더 실행된다는 성능적인 문제 뿐만 아니라 관리에 대한 부담도 있기 때문에 다대일 양방향 매핑을 사용하는 것이 바람직하다.
        출처: https://leejaedoo.github.io/relationship_mapping/
     */



    /*
    역방향도 @ManyToMany를 사용하고 mappedBy로 연관관계 주인을 지정한다. 연관관계 편의 메소드를 추가해서 관리하는 것이 편리하다.
    @ManyToMany를 사용하면 연결 테이블을 자동으로 처리해주므로 도메인 모델이 단순해지고 편리하지만, 다른 컬럼(ex. 상품 주문 날짜..)이 더 필요할 수 있다.
    따라서 실무에서 사용하기에는 한계가 있다. 이렇게 다른 컬럼이 추가되게 되면 추가된 컬럼에 대한 매핑이 불가하기 때문에 더 이상 @ManyToMany를 사용할 수 없게 된다.
    따라서 연결 테이블과 매핑 되는 연결 엔티티를 만들어 다대다에서 엔티티 간에 다대일과 일대다 관계로 풀어야 한다.

    @ManyToMany
    @JoinTable(name = "post_like", joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id"))
    private List<Post> likePosts = new ArrayList<>();
     */
}
