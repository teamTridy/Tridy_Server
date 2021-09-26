package teamtridy.tridy.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Job;

@Data
@Builder
public class JobReadResponseDto {

    private Long id;
    private String name;
    private String imgUrl;
    private String address;
    private String workingDate;
    private String workingHour;
    private String salary;
    private Boolean isClosed;
    private String rep;
    private String businessDescription;
    private String workingDay;
    private String workingDescription;
    private Integer capacity;
    private LocalDate deadline;
    private String qualifications;
    private String comment;

    public static JobReadResponseDto of(Job job) {
        Boolean isClosed = job.getDeadline().isBefore(LocalDateTime.now());
        return JobReadResponseDto.builder()
                .id(job.getId())
                .name(job.getName())
                .imgUrl(job.getImgUrl())
                .address(job.getAddress())
                .workingDate(job.getWorkingDate())
                .workingHour(job.getWorkingHour())
                .salary(job.getSalary())
                .isClosed(isClosed)
                .rep(job.getRep())
                .businessDescription(job.getBusinessDescription())
                .workingDay(job.getWorkingDay())
                .workingDescription(job.getWorkingDescription())
                .capacity(job.getCapacity())
                .deadline(job.getDeadline().toLocalDate())
                .qualifications(job.getQualifications())
                .comment(job.getComment())
                .build();
    }
}