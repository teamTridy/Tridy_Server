package teamtridy.tridy.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Slice<Review> findByIdLessThanAndPlaceAndIsPrivateOrderByIdDesc(Long lastReviewId, Place place, Boolean isPrivate,Pageable pageable); //findByIdLessThan : lastArticleId보다 작은 값의 id 중에서 게시물을 가져온다.

    @Query("SELECT ROUND(AVG(r.rating),1) FROM Review r WHERE r.place = :place")
    Float getRatingAverage(Place place);

    Long countByPlace(Place place);
    Long countByPlaceAndRating(Place place, Integer rating);
}
