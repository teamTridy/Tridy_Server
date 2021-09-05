package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {
}
