package teamtridy.tridy.service.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Place;

@Data
@Builder
public class PlaceDto {

    private Long id;
    private String name;
    private String imgUrl;
    private String address;
    private List<String> hashtags;
    private Boolean isPicked;

    public static PlaceDto of(Place place, Account account) {
        PlaceDto placeDto = PlaceDto.builder().id(place.getId()).name(place.getName())
                .imgUrl(place.getImgUrl()).address(place.getAddress()).build();

        if (place.getPlaceHashtag().size() != 0) {
            List<String> hashtags = place.getPlaceHashtag().stream()
                    .map(placeHashtag -> placeHashtag.getHashtag().getName())
                    .collect(Collectors.toList());
            placeDto.setHashtags(hashtags);
        }

        Boolean isPicked = account.getPicks()
                .stream().map(pick -> pick.getPlace())
                .collect(Collectors.toList())
                .contains(place);

        placeDto.setIsPicked(isPicked);

        return placeDto;
    }
}
