package teamtridy.tridy.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.PlaceDto;

@Data
@Builder
public class PickReadAllResponseDto {

    private Integer currentPage;
    private Integer currentSize;
    private Boolean hasNextPage;
    private List<PlaceDto> places;
}
