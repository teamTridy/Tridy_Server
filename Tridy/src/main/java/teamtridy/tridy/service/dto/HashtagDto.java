package teamtridy.tridy.service.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Hashtag;

@Data
@Builder
public class HashtagDto {
    private Long id;
    private String name;

    public static HashtagDto of(Hashtag hashtag) {
        return HashtagDto.builder().id(hashtag.getId()).name(hashtag.getName()).build();
    }
}
