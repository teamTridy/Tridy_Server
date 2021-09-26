package teamtridy.tridy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import teamtridy.tridy.domain.entity.Job;
import teamtridy.tridy.domain.repository.JobRepository;
import teamtridy.tridy.dto.JobReadAllResponseDto;
import teamtridy.tridy.dto.JobReadResponseDto;
import teamtridy.tridy.error.CustomException;
import teamtridy.tridy.error.ErrorCode;
import teamtridy.tridy.service.dto.JobDto;

@RequiredArgsConstructor
@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobReadAllResponseDto readAllByDateOrQuery(String date, String query, Integer page,
            Integer size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Slice<Job> jobs;

        if (date != null && query == null) {
            jobs = jobRepository.findAllByDate(date, pageRequest);
        } else if (date == null && query != null) { //query
            LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
            jobs = jobRepository.findAllByQuery(query, todayStartTime, pageRequest);
        } else {
            jobs = jobRepository.findAll(pageRequest);
        }

        List<JobDto> jobDtos = jobs.stream().map(JobDto::of).collect(Collectors.toList());

        return JobReadAllResponseDto.builder()
                .currentPage(jobs.getNumber() + 1)
                .currentSize(jobs.getNumberOfElements())
                .hasNextPage(jobs.hasNext())
                .jobs(jobDtos).build();
    }

    public JobReadResponseDto read(Long jobId) {
        Job job = jobRepository
                .findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_NOT_FOUND));
        return JobReadResponseDto.of(job);
    }

}
