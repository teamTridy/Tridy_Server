package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Place;

public interface PlaceRepository extends JpaRepository<Place, Long> {

}