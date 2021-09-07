package teamtridy.tridy.service.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Place;

@Data
@Builder
public class PlaceDto {

    private Long id;
    private String name;
    private String thumbImgUrl;
    private String address;
    private List<String> hashtags;

    public static PlaceDto of(Place place) {
        PlaceDto placeDto = PlaceDto.builder().id(place.getId()).name(place.getName())
            .thumbImgUrl(place.getThumbImgUrl()).address(place.getAddress()).build();

        if (place.getPlaceHashtag().size() != 0) {
            List<String> hashtags = place.getPlaceHashtag().stream()
                .map(placeHashtag -> placeHashtag.getHashtag().getName())
                .collect(Collectors.toList());
            placeDto.setHashtags(hashtags);
        }

        return placeDto;
    }
}
