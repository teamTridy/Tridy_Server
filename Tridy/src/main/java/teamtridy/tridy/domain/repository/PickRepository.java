package teamtridy.tridy.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Pick;
import teamtridy.tridy.domain.entity.Place;

public interface PickRepository extends JpaRepository<Pick, Long> {

    Pick findByAccountAndPlace(Account account, Place place);

    Slice<Pick> findByAccountOrderByIdDesc(Account account, Pageable pageable);
}