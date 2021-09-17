package teamtridy.tridy.domain.repository;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    @Cacheable(value = "InterestFindAllCache")
    List<Interest> findAll();
}
