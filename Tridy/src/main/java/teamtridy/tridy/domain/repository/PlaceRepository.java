package teamtridy.tridy.domain.repository;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamtridy.tridy.domain.entity.Category;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Region;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p " +
            "WHERE p.name LIKE %:query% OR p.intro LIKE %:query% OR p.story LIKE %:query% " +
            "ORDER BY (CASE WHEN p.name LIKE %:query% THEN 1 WHEN p.intro LIKE %:query% THEN 2 ELSE 3 END), p.id")
    Slice<Place> findAllByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Place p " +
            "WHERE p.region in :regions AND (p.name LIKE %:query% OR p.intro LIKE %:query% OR p.story LIKE %:query%) "
            +
            "ORDER BY (CASE WHEN p.name LIKE %:query% THEN 1 WHEN p.intro LIKE %:query% THEN 2 ELSE 3 END), p.id")
    Slice<Place> findAllByQueryAndRegionIn(String query, List<Region> regions,
            Pageable pageable);

    @Query("SELECT p FROM Place p " +
            "WHERE p.category in :categories AND (p.name LIKE %:query% OR p.intro LIKE %:query% OR p.story LIKE %:query%) "
            +
            "ORDER BY (CASE WHEN p.name LIKE %:query% THEN 1 WHEN p.intro LIKE %:query% THEN 2 ELSE 3 END), p.id")
    Slice<Place> findAllByQueryAndCategoryIn(String query, List<Category> categories,
            Pageable pageable);

    @Query("SELECT p FROM Place p " +
            "WHERE  p.region in :regions AND p.category in :categories AND (p.name LIKE %:query% OR p.intro LIKE %:query% OR p.story LIKE %:query%) "
            +
            "ORDER BY (CASE WHEN p.name LIKE %:query% THEN 1 WHEN p.intro LIKE %:query% THEN 2 ELSE 3 END), p.id")
    Slice<Place> findAllByQueryAndCategoryInAndRegionIn(String query, List<Category> categories,
            List<Region> regions, Pageable pageable);

    @Cacheable(value = "readAllPlaceByDepth1OrderByReviewCountCache", key = "#pageable.getPageNumber()+#pageable.getPageSize()+#depth1CategoryId")
    @Query(value =
            "SELECT * FROM place p LEFT JOIN review r on p.place_id = r.place_id \n"
                    + "WHERE p.category_id in (select category_id from category where parent_id in (select category_id from category where parent_id = :depth1CategoryId))\n"
                    + "GROUP BY p.place_id \n"
                    + "ORDER BY \n"
                    + "\t(CASE WHEN COUNT(r.place_id) != 0 THEN 1 ELSE 2 END), \n"
                    + "\tCOUNT(r.place_id) DESC, \n"
                    + "\t(CASE WHEN PERCENT_RANK() OVER (ORDER BY p.place_id DESC) > 0.5 THEN 1 ELSE 2 END),\n"
                    + "\tRAND()", nativeQuery = true)
    Slice<Place> findAllByCategoryInOrderByReviewCount(Long depth1CategoryId,
            Pageable pageable);

    Slice<Place> findAllByCategoryInAndRegionInOrderById(List<Category> depth3Categories,
            List<Region> regions, Pageable pageable);

    Slice<Place> findAllByCategoryInOrderById(List<Category> depth3Categories, Pageable pageable);
}