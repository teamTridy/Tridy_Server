package teamtridy.tridy.service.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Review;

@Data
@Builder
public class ReviewDto {

    private Long id;
    private Integer rating;
    private String comment;
    private Boolean isPrivate;
    private LocalDate createdAt;
    private Boolean isAuthor;

    public static ReviewDto of(Review review, Account account) {
        ReviewDto reviewDto = ReviewDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .isPrivate(review.getIsPrivate())
                .createdAt(review.getCreatedAt().toLocalDate())
                .build();

        Boolean isAuthor = review.getAccount() == account;
        reviewDto.setIsAuthor(isAuthor);

        return reviewDto;
    }
}
