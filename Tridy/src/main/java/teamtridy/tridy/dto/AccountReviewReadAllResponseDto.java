package teamtridy.tridy.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.ReviewDto;

@Data
@Builder
public class AccountReviewReadAllResponseDto {

    private Integer year;
    private Integer month;
    private Integer currentPage;
    private Integer currentSize;
    private Boolean hasNextPage;

    private List<ReviewDto> reviews;
}
