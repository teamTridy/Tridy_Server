package teamtridy.tridy.domain.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String rep;

    @Column(nullable = false)
    private String workingDate;

    @Column(nullable = false)
    private String businessDescription;

    @Column(nullable = false)
    private Boolean isContinuousWorking;

    @Column(nullable = false)
    private String workingDay;

    @Column(nullable = false)
    private String workingHour;

    @Column(nullable = false)
    private String workingDescription;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String salary;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column
    private String qualifications;

    @Column
    private String comment;

    @Column
    private String imgUrl;
}
