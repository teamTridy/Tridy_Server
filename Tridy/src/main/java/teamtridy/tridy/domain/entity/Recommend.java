package teamtridy.tridy.domain.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "place_id"}))
public class Recommend extends BaseTimeEntity {

    // 자식 정의
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mainRecommend")
    private List<Recommend> relatedRecommends = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id", nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "interest_id")
    private Interest interest;
    @Column(nullable = false)
    private Integer orderNum;
    private Double distanceFromReference;
    private String referenceAddress;
    private Integer congestion;
    @ManyToOne
    @JoinColumn(name = "recommend_type_id", nullable = false)
    private RecommendType recommendType; //recommend type 기술
    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
    @ManyToOne
    @JoinColumn(name = "account_id")
//, columnDefinition = "comment 'recommend 가 이번주 인기 기반 일 경우 값이 없음'")
    private Account account;
    // 부모 정의 (셀프 참조) https://velog.io/@guswns3371/JPA-%EC%88%9C%ED%99%98-%EC%B0%B8%EC%A1%B0-self-%EC%B0%B8%EC%A1%B0
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_recommend_id")
    private Recommend mainRecommend;

    public Recommend setMainRecommend(Recommend mainRecommend) {
        if (this.mainRecommend != null) {
            this.mainRecommend.getRelatedRecommends().remove(this);
        }

        this.mainRecommend = mainRecommend;

        // 편의 메소드는 한 곳에만 작성하거나 양쪽 다 작성할 수 있다. 양쪽 엔티티 둘다 작성한다면 무한루프에 빠지지 않도록 체크
        if (!mainRecommend.getRelatedRecommends().contains(this)) {
            mainRecommend.getRelatedRecommends().add(this);
        }

        return this;
    }


    // flush 이전에도 정상적으로 teamA에 memberA가 포함되도록 할 수 있다.
    public void setPlace(Place place) {
        this.place = place;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
