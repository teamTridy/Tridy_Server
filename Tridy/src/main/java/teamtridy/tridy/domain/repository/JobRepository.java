package teamtridy.tridy.domain.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamtridy.tridy.domain.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j "
            + "WHERE j.name LIKE %:query% OR j.workingDescription LIKE %:query% OR j.comment LIKE %:query% "
            + "ORDER BY (CASE WHEN j.deadline > :today THEN 1 ELSE 2 END),"
            + " (CASE WHEN j.name LIKE %:query% THEN 1 WHEN j.workingDescription LIKE %:query% THEN 2 ELSE 3 END),"
            + " j.id DESC")
    Slice<Job> findAllByQuery(@Param("query") String query, @Param("today") LocalDateTime today,
            Pageable pageable);

    @Query("SELECT j FROM Job j "
            + "WHERE j.workingDate LIKE CONCAT('%',:date,'%') "
            + "OR (j.isContinuousWorking = true AND SUBSTRING_INDEX(j.workingDate,'~',1) <= :date AND SUBSTRING_INDEX(j.workingDate,'~',-1) >= :date) "
            + "ORDER BY j.id DESC")
    Slice<Job> findAllByDate(String date, Pageable pageable);
}