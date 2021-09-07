package teamtridy.tridy.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamtridy.tridy.domain.entity.RefreshToken;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}
