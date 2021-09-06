package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Place;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PlaceReadResponseDto {
    private Long id;
    private String name;
    private String imgUrl;
    private String address;
    private String rep;
    private String mapUrl;
    private String intro;
    private String story;
    private String info;
    private List<String> hashtags;

    public static PlaceReadResponseDto of(Place place) {
        PlaceReadResponseDto placeDto = PlaceReadResponseDto
                .builder()
                .id(place.getId())
                .name(place.getName())
                .imgUrl(place.getImgUrl())
                .address(place.getAddress())
                .rep(place.getRep())
                .mapUrl(place.getMapUrl())
                .intro(place.getIntro())
                .story(place.getStory())
                .info(place.getInfo())
                .build();

        if (place.getPlaceHashtag().size() != 0) {
            List<String> hashtags = place.getPlaceHashtag().stream().map(placeHashtag -> placeHashtag.getHashtag().getName()).collect(Collectors.toList());
            placeDto.setHashtags(hashtags);
        }
        return placeDto;
    }
}