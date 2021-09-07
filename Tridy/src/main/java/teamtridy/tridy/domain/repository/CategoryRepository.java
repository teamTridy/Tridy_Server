package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Category findByDepth(Integer depth);
}

