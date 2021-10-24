package teamtridy.tridy.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Recommend;
import teamtridy.tridy.domain.entity.RecommendType;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {

    @Query(value = "select place_id as id \n"
            + "from place\n"
            + "where origin_content_id is not null \n"
            + "and place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
            + "and place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1)\n"
            + "and place_id in (select place_id \n"
            + "\t\tfrom place_hashtag ph left join interest_hashtag ih on ih.hashtag_id = ph.hashtag_id \n"
            + "\t\twhere ih.interest_id = :interestId)\n"
            + "ORDER BY rand()\n"
            + "limit 0,3", nativeQuery = true)
    List<PlaceId> findPlaceTop3ByInterestIdOrderByRandom(Long accountId, Long interestId);


    @Query(value = "SELECT place_id as id, name, latitude, longitude,\n"
            + "\t(6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude)))))\n"
            + "\tAS distance\n"
            + "FROM place\n"
            + "where category_id in (select category_id from category where parent_id in (select category_id from category where parent_id in (274, 275))) and place_id not in (select place_id from review where account_id = :accountId)\n"
            + "ORDER BY distance \n"
            + "LIMIT 0,3", nativeQuery = true)
    List<DistanceIncludePlace> findTop3RelatedPlaceByMainRecommendAndInFoodOrCafeCategory(
            Double latitude, Double longitude, Long accountId);

   /* @Query(value = "select prv.place_id as id\n"
            + "from \n"
            + "\t(select p.place_id from place p left join (select distinct(place_id) from review where account_id  = :accountId) as rv \n"
            + "    ON p.place_id = rv.place_id where rv.place_id is null) as prv \n"
            + "    left join \n"
            + "\t\t(select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1) as rc \n"
            + "        ON prv.place_id = rc.place_id \n"
            + "\twhere rc.place_id is null", nativeQuery = true)
    List<PlaceId> findPlaceIdByNotReviewedAndNotMainRecommended(Long accountId);*/

    @Query(value =
            "SELECT *, (case when distance <= 15 then 1 when distance <= 30 then :distanceLt30Order else 3 end ) as distance_order , ( case when view_per_rank >=0.5 then :viewPerRankGt50Order else :viewPerRankLt50Order end) as view_order\n"
                    + "FROM \n"
                    + "\t(SELECT place_id as id, name, PERCENT_RANK() OVER (ORDER BY place_id DESC) as view_per_rank, latitude, longitude, (6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))))) as distance\n"
                    + "\t\tFROM place\n"
                    + "\t\tWHERE place_id in (select place_id\n"
                    + "\t\t\tfrom place\n"
                    + "\t\t\twhere origin_content_id is not null \n"
                    + "\t\t\tand place_id in (select distinct(place_id) from place_hashtag where hashtag_id in (1645,1674,1925,535))\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1)\n"
                    + "\t\t\tand place_id in (select place_id \n"
                    + "\t\t\t\t\t\t\t from place_hashtag ph left join interest_hashtag ih on ih.hashtag_id = ph.hashtag_id \n"
                    + "\t\t\t\t\t\t\t where ih.interest_id not in (select interest_id from account_interest where account_id = :accountId)))\n"
                    + "\t) rp\n"
                    + "ORDER BY distance_order, view_order, rand()\n"
                    + "limit 0,:count", nativeQuery = true)
    List<DistanceIncludePlace> findTopCountPlaceByNotInInterestAndIndoorWeather(Long accountId,
            Double latitude, Double longitude, Integer distanceLt30Order,
            Integer viewPerRankGt50Order, Integer viewPerRankLt50Order,
            Integer count);

    @Query(value =
            "SELECT *, (case when distance <= 15 then 1 when distance <= 30 then :distanceLt30Order else 3 end ) as distance_order , ( case when view_per_rank >=0.5 then :viewPerRankGt50Order else :viewPerRankLt50Order end) as view_order\n"
                    + "FROM \n"
                    + "\t(SELECT place_id as id, name, PERCENT_RANK() OVER (ORDER BY place_id DESC) as view_per_rank, latitude, longitude, (6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))))) as distance\n"
                    + "\t\tFROM place\n"
                    + "\t\tWHERE place_id in (select place_id\n"
                    + "\t\t\tfrom place\n"
                    + "\t\t\twhere origin_content_id is not null \n"
                    + "\t\t\tand place_id in (select distinct(place_id) from place_hashtag where hashtag_id in (1645,1674,1925,535))\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1)\n"
                    + "\t\t\tand place_id in (select place_id \n"
                    + "\t\t\t\t\t\t\t from place_hashtag ph left join interest_hashtag ih on ih.hashtag_id = ph.hashtag_id \n"
                    + "\t\t\t\t\t\t\t where ih.interest_id in (select interest_id from account_interest where account_id = :accountId)))\n"
                    + "\t) rp\n"
                    + "ORDER BY distance_order, view_order, rand()\n"
                    + "limit 0,:count", nativeQuery = true)
    List<DistanceIncludePlace> findTopCountPlaceByInInterestAndIndoorWeather(Long accountId,
            Double latitude, Double longitude, Integer distanceLt30Order,
            Integer viewPerRankGt50Order, Integer viewPerRankLt50Order,
            Integer count);

    @Query(value =
            "SELECT *, (case when distance <= 15 then 1 when distance <= 30 then :distanceLt30Order else 3 end ) as distance_order , ( case when view_per_rank >=0.5 then :viewPerRankGt50Order else :viewPerRankLt50Order end) as view_order\n"
                    + "FROM \n"
                    + "\t(SELECT place_id as id, name, PERCENT_RANK() OVER (ORDER BY place_id DESC) as view_per_rank, latitude, longitude, (6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))))) as distance\n"
                    + "\t\tFROM place\n"
                    + "\t\tWHERE place_id in (select place_id\n"
                    + "\t\t\tfrom place\n"
                    + "\t\t\twhere origin_content_id is not null \n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1)\n"
                    + "\t\t\tand place_id in (select place_id \n"
                    + "\t\t\t\t\t\t\t from place_hashtag ph left join interest_hashtag ih on ih.hashtag_id = ph.hashtag_id \n"
                    + "\t\t\t\t\t\t\t where ih.interest_id in (select interest_id from account_interest where account_id = :accountId)))\n"
                    + "\t) rp\n"
                    + "ORDER BY distance_order, view_order, rand()\n"
                    + "limit 0,:count", nativeQuery = true)
    List<DistanceIncludePlace> findTopCountPlaceByInInterest(Long accountId,
            Double latitude, Double longitude, Integer distanceLt30Order,
            Integer viewPerRankGt50Order, Integer viewPerRankLt50Order,
            Integer count);

    @Query(value =
            "SELECT *, (case when distance <= 15 then 1 when distance <= 30 then :distanceLt30Order else 3 end ) as distance_order , ( case when view_per_rank >=0.5 then :viewPerRankGt50Order else :viewPerRankLt50Order end) as view_order\n"
                    + "FROM \n"
                    + "\t(SELECT place_id as id, name, PERCENT_RANK() OVER (ORDER BY place_id DESC) as view_per_rank, latitude, longitude, (6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))))) as distance\n"
                    + "\t\tFROM place\n"
                    + "\t\tWHERE place_id in (select place_id\n"
                    + "\t\t\tfrom place\n"
                    + "\t\t\twhere origin_content_id is not null \n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1)\n"
                    + "\t\t\tand place_id in (select place_id \n"
                    + "\t\t\t\t\t\t\t from place_hashtag ph left join interest_hashtag ih on ih.hashtag_id = ph.hashtag_id \n"
                    + "\t\t\t\t\t\t\t where ih.interest_id not in (select interest_id from account_interest where account_id = :accountId)))\n"
                    + "\t) rp\n"
                    + "ORDER BY distance_order, view_order, rand()\n"
                    + "limit 0,:count", nativeQuery = true)
    List<DistanceIncludePlace> findTopCountPlaceByNotInInterest(Long accountId,
            Double latitude, Double longitude, Integer distanceLt30Order,
            Integer viewPerRankGt50Order, Integer viewPerRankLt50Order,
            Integer count);


    @Query(value =
            "SELECT place_id as id, name, latitude, longitude, (6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))))) as distance\n"
                    + "\t\tFROM place\n"
                    + "\t\tWHERE place_id in (select place_id\n"
                    + "\t\t\tfrom place\n"
                    + "\t\t\twhere origin_content_id is not null \n"
                    + "\t\t\tand place_id in (select distinct(place_id) from place_hashtag where hashtag_id in (1645,1674,1925,535))\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1))\n"
                    + "ORDER BY (case when distance <= 20 then 1 else 2 end ), rand()\n"
                    + "limit 0,:count", nativeQuery = true)
    List<DistanceIncludePlace> findTopCountPlaceByIndoorWeather(Long accountId, Double latitude,
            Double longitude, Integer count);

    @Query(value =
            "SELECT place_id as id, name, latitude, longitude, (6371 * 2 * ATAN2(SQRT(POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))), SQRT(1 - POW(SIN(RADIANS(latitude - :latitude)/2), 2) + POW(SIN(RADIANS(longitude - :longitude)/2), 2) * COS(RADIANS(:latitude)) * COS(RADIANS(latitude))))) as distance\n"
                    + "\t\tFROM place\n"
                    + "\t\tWHERE place_id in (select place_id\n"
                    + "\t\t\tfrom place\n"
                    + "\t\t\twhere origin_content_id is not null \n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from review where account_id  = :accountId )\n"
                    + "\t\t\tand place_id not in (select distinct(place_id) from recommend where account_id  = :accountId and recommend_type_id = 1))\n"
                    + "ORDER BY (case when distance <= 20 then 1 else 2 end ), rand()\n"
                    + "limit 0,:count", nativeQuery = true)
    List<DistanceIncludePlace> findTopCountPlace(Long accountId, Double latitude,
            Double longitude, Integer count);

    List<Recommend> findTop5ByAccountAndReferenceAddressAndCreatedAtBetweenAndRecommendType(
            Account account,
            String address,
            LocalDateTime todayStartTime, LocalDateTime todayEndTime,
            RecommendType mainRecommendType);

    @Cacheable(value = "mainRecommendCache", key = "#account.getId()")
    List<Recommend> findTop5ByAccountAndCreatedAtBetweenAndRecommendTypeOrderByCreatedAtDescOrderNum(
            Account account,
            LocalDateTime todayStartTime, LocalDateTime todayEndTime,
            RecommendType mainRecommendType);

    List<Recommend> findByAccountAndCreatedAtBetweenAndRecommendTypeOrderById(Account account,
            LocalDateTime todayStartTime, LocalDateTime todayEndTime,
            RecommendType recommendType);

    void deleteAllByCreatedAtBetweenAndRecommendType(LocalDateTime startTime, LocalDateTime endTime,
            RecommendType recommendType);


    void deleteAllByAccountAndRecommendType(Account account, RecommendType interest2RecommendType);

    interface DistanceIncludePlace {

        Long getId();

        String getName();

        Double getDistance();
    }

    interface PlaceId {

        Long getId();

    }
}

