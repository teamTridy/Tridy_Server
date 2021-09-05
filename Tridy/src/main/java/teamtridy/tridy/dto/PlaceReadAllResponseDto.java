package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.PlaceDto;

import java.util.List;

@Data
@Builder
public class PlaceReadAllResponseDto {
    private Integer currentPage;
    private Integer currentSize;
    private Boolean hasNextPage;
    private List<PlaceDto> places;
}
