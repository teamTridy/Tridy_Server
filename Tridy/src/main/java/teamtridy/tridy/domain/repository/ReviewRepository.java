package teamtridy.tridy.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Slice<Review> findByIdLessThanAndPlaceAndIsPrivateOrderByIdDesc(Long lastReviewId, Place place,
            Boolean isPrivate,
            Pageable pageable); //findByIdLessThan : lastArticleId보다 작은 값의 id 중에서 게시물을 가져온다.

    @Query("SELECT ROUND(AVG(r.rating),1) FROM Review r WHERE r.place = :place and r.isPrivate = :isPrivate")
    Float getRatingAverageAndIsPrivate(Place place, Boolean isPrivate);

    Long countByPlaceAndIsPrivate(Place place, Boolean isPrivate);

    Long countByPlaceAndIsPrivateAndRating(Place place, Boolean isPrivate, Integer rating);

    Slice<Review> findByAccountOrderByIdDesc(Account account, Pageable pageable);
}