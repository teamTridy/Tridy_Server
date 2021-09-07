package teamtridy.tridy.domain.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer depth;

    @Column //nullable. 관광공사 데이터가 아닐수도 있음.
    private String originCode;

    // 부모 정의 (셀프 참조) https://velog.io/@guswns3371/JPA-%EC%88%9C%ED%99%98-%EC%B0%B8%EC%A1%B0-self-%EC%B0%B8%EC%A1%B0
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    // 자식 정의
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCategory")
    private List<Category> children;
}
