package teamtridy.tridy.service.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Recommend;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
//Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.
/*
Lombok은 Builder 주석에 직면 한 상속 문제에 대해 버전 1.18.2의 실험 기능을 도입했으며 다음과 같이 @SuperBuilder 주석으로 해결할 수 있습니다.
https://www.python2.net/questions-128206.htm
 */
public class MainRecommendPlaceDto extends RecommendPlaceDto {

    private List<RecommendPlaceDto> relateds;

    public static MainRecommendPlaceDto of(Recommend recommend, Account account) {
        Place place = recommend.getPlace();

        MainRecommendPlaceDto mainRecommendPlaceDto = MainRecommendPlaceDto.builder()
                .id(place.getId()).name(place.getName())
                .thumbImgUrl(place.getThumbImgUrl()).build();

        if (recommend.getDistanceFromReference() != null) {
            Float distance =
                    Math.round(recommend.getDistanceFromReference() * 10)
                            / (float) 10; // 소수점 첫째자리까지
            mainRecommendPlaceDto.setDistance(distance);
        }

        if (recommend.getCongestion() != null) {
            mainRecommendPlaceDto.setCongestion(recommend.getCongestion());
        }

        if (place.getPlaceHashtag().size() != 0) {
            List<String> hashtags = place.getPlaceHashtag().stream()
                    .map(placeHashtag -> placeHashtag.getHashtag().getName())
                    .collect(Collectors.toList());
            mainRecommendPlaceDto.setHashtags(hashtags);
        }

        Boolean isPicked = account.getPicks()
                .stream().map(pick -> pick.getPlace())
                .collect(Collectors.toList())
                .contains(place);

        mainRecommendPlaceDto.setIsPicked(isPicked);

        if (recommend.getRelatedRecommends().size() != 0) {
            List<RecommendPlaceDto> relatedRecommendDtos = recommend
                    .getRelatedRecommends().stream()
                    .map(relatedRecommends -> RecommendPlaceDto
                            .of(relatedRecommends, account))
                    .collect(Collectors.toList());
            mainRecommendPlaceDto.setRelateds(relatedRecommendDtos);
        }

        return mainRecommendPlaceDto;
    }
}
