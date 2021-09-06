package teamtridy.tridy.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamtridy.tridy.domain.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    @Query("SELECT p FROM Place p " +
            "WHERE p.name LIKE %:query% OR p.intro LIKE %:query% OR p.story LIKE %:query% " +
            "ORDER BY (CASE WHEN p.name LIKE %:query% THEN 1 WHEN p.intro LIKE %:query% THEN 2 ELSE 3 END), p.id")
    Slice<Place> findAllByQuery(@Param("query") String query, Pageable pageable);
}