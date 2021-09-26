package teamtridy.tridy.controller;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.dto.JobReadAllResponseDto;
import teamtridy.tridy.dto.PlaceReadResponseDto;
import teamtridy.tridy.service.JobService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
@Validated
@Slf4j
public class JobController {

    private final JobService jobService;

    @GetMapping("")
    public ResponseEntity<JobReadAllResponseDto> readAll(
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$", message = "날짜는 yyyy-MM-dd 형식이어야 합니다.") String date,
            @RequestParam(required = false) @Length(min = 2) String query,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size) {
        return new ResponseEntity(
                jobService
                        .readAllByDateOrQuery(date, query, page, size),
                HttpStatus.OK);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<PlaceReadResponseDto> read(
            @PathVariable Long jobId) {
        return new ResponseEntity(jobService.read(jobId), HttpStatus.OK);
    }

}
