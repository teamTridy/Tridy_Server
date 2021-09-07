package teamtridy.tridy.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommend_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hashtag_id")
//, columnDefinition = "comment 'recommend 가 해시태그 기반 일 경우 값이 채워짐'")
    private Hashtag hashtag;

    @Column(nullable = false)
    private Integer orderNum;

    @ManyToOne
    @JoinColumn(name = "recommend_type_id", nullable = false)
    private RecommendType recommendType; //recommend type 기술

    public void setRecommendType(RecommendType recommendType) {
        this.recommendType = recommendType;
    }

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public void setPlace(Place place) {
        this.place = place;
    }

    @ManyToOne
    @JoinColumn(name = "account_id")
//, columnDefinition = "comment 'recommend 가 이번주 인기 기반 일 경우 값이 없음'")
    private Account account;

    public void setAccount(Account account) {
        this.account = account;
    }
}
