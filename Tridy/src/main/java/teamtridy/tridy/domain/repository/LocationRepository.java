package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
