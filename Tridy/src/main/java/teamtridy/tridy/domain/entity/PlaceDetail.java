package teamtridy.tridy.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

//https://ict-nroo.tistory.com/126?category=826875 해보고 잘 안되면 참고해볼 것.
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDetail extends BaseEntity implements Serializable {

    @Id
    @OneToOne
    @JoinColumn(name="place_id",nullable = false)
    private Place place;

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

}
