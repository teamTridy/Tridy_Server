package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findBySocialId(String socialId);
    boolean existsByNickname(String nickname);
    boolean existsBySocialId(String socialId);
}
