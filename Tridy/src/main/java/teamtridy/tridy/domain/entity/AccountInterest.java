package teamtridy.tridy.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInterest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_interest_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;


    public void setAccount(Account account) {
        if (this.account != null) {
            this.account.getAccountInterests().remove(this);
        }

        this.account = account;

        // 편의 메소드는 한 곳에만 작성하거나 양쪽 다 작성할 수 있다. 양쪽 엔티티 둘다 작성한다면 무한루프에 빠지지 않도록 체크
        if (!account.getAccountInterests().contains(this)) {
            account.getAccountInterests().add(this);
        }
    }

    /*
    위에 다대다 매핑의 한계 첨부 그림에서는 MemberProduct의 MEMBER_ID, PRODUCT_ID를 묶어서 PK로 썻지만, 실제로는 아래 처럼 독립적으로 generated되는 id를 사용하는 것을 권장한다.
    ID가 두개의 테이블에 종속되지 않고 더 유연하게 개발 할 수 있다.
    시스템을 운영하면서 점점 커지는데 만약 비즈니스적인 제약 조건이 커지면 PK를 운영중에 업데이트 하는 상황이 발생할 수도 있다.
    출처: https://ict-nroo.tistory.com/127 [개발자의 기록습관]
     */
}
