package teamtridy.tridy.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Place extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double Longitude;

    @Column
    private Long originContentId;

    @Column
    private Long originContentTypeId;

    @Column
    private String thumbImgUrl;

    @Column//(columnDefinition = "comment '한줄 소개'")
    private String intro;

    @Column//(columnDefinition = "comment '개요 2줄 요약'")
    private String story;

    @Column//(columnDefinition = "comment '맵 주소'")
    private String mapUrl;

    @Column//(columnDefinition = "comment '대표 전화번호'")
    private String rep;

    @Column//(columnDefinition = "comment '상세 이용정보'")
    private String info; //크롤링 할 것 고려

    @Column//(columnDefinition = "comment '큰 이미지 주소'")
    private String imgUrl; //크롤링 할 것 고려

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    public void setLocation(Location location) {
        this.location = location;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public void setCategory(Category category) {
        this.category = category;
    }

    @OneToMany(mappedBy = "place")
    @Builder.Default
    private List<PlaceHashtag> placeHashtag = new ArrayList<>();
}
