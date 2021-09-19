package teamtridy.tridy.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.RecommendPlaceDto;

@Data
@Builder
public class InterestRecommendPlaceDto {

    private Long interestId;
    private List<RecommendPlaceDto> places;
}
