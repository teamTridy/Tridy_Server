package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InterestRecommendReadResponseDto {

    InterestRecommendPlaceDto interest1;
    InterestRecommendPlaceDto interest2;
}
