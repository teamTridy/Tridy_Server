package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.ReviewDto;

import java.util.List;

@Data
@Builder
public class PlaceReviewReadAllResponseDto {
    private Long lastReviewId;
    private Integer currentSize;
    private Boolean hasNextPage;

    private Float ratingAverage;
    private List<Float> ratingRatios;
    private Long reviewTotalCount;
    private List<ReviewDto> reviews;
}
