package teamtridy.tridy.service.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Job;

@Data
@Builder
public class JobDto {

    private Long id;
    private String name;
    private String imgUrl;
    private String address;
    private String workingDate;
    private String workingHour;
    private String salary;
    private Boolean isClosed;

    public static JobDto of(Job job) {
        Boolean isClosed = job.getDeadline().isBefore(LocalDateTime.now());
        return JobDto.builder()
                .id(job.getId())
                .name(job.getName())
                .imgUrl(job.getImgUrl())
                .address(job.getAddress())
                .workingDate(job.getWorkingDate())
                .workingHour(job.getWorkingHour())
                .salary(job.getSalary())
                .isClosed(isClosed)
                .build();
    }
}
