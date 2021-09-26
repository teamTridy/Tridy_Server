package teamtridy.tridy.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.JobDto;

@Data
@Builder
public class JobReadAllResponseDto {

    private Integer currentPage;
    private Integer currentSize;
    private Boolean hasNextPage;
    private List<JobDto> jobs;
}