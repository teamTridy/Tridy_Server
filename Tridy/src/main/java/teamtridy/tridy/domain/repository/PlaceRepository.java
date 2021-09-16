package teamtridy.tridy.domain.repository;

import java.util.List;
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


    Slice<Place> findAllByCategoryInAndRegionIn(List<Category> depth3Categories,
            List<Region> regions, Pageable pageable);

    Slice<Place> findAllByCategoryIn(List<Category> depth3Categories, Pageable pageable);
}