package teamtridy.tridy.service.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Recommend;

@Data
@SuperBuilder
public class RecommendPlaceDto {

    private Long id;
    private String name;
    private String imgUrl;
    private List<String> hashtags;
    private Boolean isPicked;

    private Float distance; //nullable
    private Integer congestion; //nullable


    public static RecommendPlaceDto of(Recommend recommend, Account account) {
        Place place = recommend.getPlace();

        RecommendPlaceDto relatedRecommendPlaceDto = RecommendPlaceDto.builder()
                .id(place.getId()).name(place.getName())
                .imgUrl(place.getImgUrl()).build();

        if (recommend.getDistanceFromReference() != null) {
            Float distance =
                    Math.round(recommend.getDistanceFromReference() * 10)
                            / (float) 10; // 소수점 첫째자리까지
            relatedRecommendPlaceDto.setDistance(distance);
        }

        if (recommend.getCongestion() != null) {
            relatedRecommendPlaceDto.setCongestion(recommend.getCongestion());
        }

        if (place.getPlaceHashtag().size() != 0) {
            List<String> hashtags = place.getPlaceHashtag().stream()
                    .map(placeHashtag -> placeHashtag.getHashtag().getName())
                    .collect(Collectors.toList());
            relatedRecommendPlaceDto.setHashtags(hashtags);
        }

        Boolean isPicked = account.getPicks()
                .stream().map(pick -> pick.getPlace().getId())
                .collect(Collectors.toList())
                .contains(place.getId());

        relatedRecommendPlaceDto.setIsPicked(isPicked);

        return relatedRecommendPlaceDto;
    }
}
