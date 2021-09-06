package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
