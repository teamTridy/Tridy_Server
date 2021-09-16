package teamtridy.tridy.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.MainRecommendPlaceDto;

@Builder
@Data
public class MainRecommendReadResponseDto {

    String address;
    List<MainRecommendPlaceDto> places;
}
