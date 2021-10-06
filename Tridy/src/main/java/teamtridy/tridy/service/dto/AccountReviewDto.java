package teamtridy.tridy.service.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Review;

@Data
@Builder
public class AccountReviewDto {

    private Long id;
    private Long placeId;
    private String placeName;
    private Integer rating;
    private String comment;
    private LocalDate createdAt;
    private Boolean isPrivate;

    public static AccountReviewDto of(Review review) {
        AccountReviewDto reviewDto = AccountReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .placeId(review.getPlace().getId())
                .placeName(review.getPlace().getName())
                .comment(review.getComment())
                .isPrivate(review.getIsPrivate())
                .createdAt(review.getCreatedAt().toLocalDate())
                .build();

        return reviewDto;
    }
}
