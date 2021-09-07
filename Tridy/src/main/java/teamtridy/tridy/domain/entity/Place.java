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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Place extends BaseTimeEntity {

    @OneToMany(mappedBy = "place")
    @Builder.Default
    private final List<PlaceHashtag> placeHashtag = new ArrayList<>();
    @Builder.Default
    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY)
    private final List<Review> reviews = new ArrayList<>();
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
