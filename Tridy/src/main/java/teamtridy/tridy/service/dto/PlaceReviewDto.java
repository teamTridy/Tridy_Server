package teamtridy.tridy.service.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Review;

@Data
@Builder
public class PlaceReviewDto {

    private Long id;
    private Integer rating;
    private String comment;
    private LocalDate createdAt;
    private String authorNickname;
    private Boolean isAuthor;

    public static PlaceReviewDto of(Review review, Account account) {
        PlaceReviewDto placeReviewDto = PlaceReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt().toLocalDate())
                .authorNickname(review.getAccount().getNickname())
                .isAuthor(review.getAccount() == account)
                .build();

        return placeReviewDto;
    }
}
