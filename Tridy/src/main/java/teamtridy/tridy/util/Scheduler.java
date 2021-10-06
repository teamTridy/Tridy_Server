package teamtridy.tridy.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import teamtridy.tridy.service.RecommendService;

@Component
@Slf4j
@RequiredArgsConstructor
// 매일 자정: 0 0 0 * * *.  1분 주기:0 * * * * *
public class Scheduler {

    private final RecommendService recommendService;

    @Scheduled(cron = "0 59 23 * * *")
    @CacheEvict(value = {"congestionCache",
            "readAllPlaceByDepth1OrderByReviewCountCache"}, allEntries = true)
    public void deleteCache() {
        log.info("Cache deleted");
    }


    @Scheduled(cron = "0 5 0 * * *")
    @CacheEvict(value = {"mainRecommendCache"}, allEntries = true)
    public void deleteExpiredRecommend() {
        log.info("expiredRecommend deleted");
        recommendService.deleteExpiredMain(); // that has been recommended for three days
        recommendService.deleteExpiredRelated(); // that has been recommended for one days
        recommendService.deleteExpiredInterest(); // that has been recommended for one days
    }

}
