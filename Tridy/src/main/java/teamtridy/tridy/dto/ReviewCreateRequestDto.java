package teamtridy.tridy.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import teamtridy.tridy.domain.entity.Review;

@Data
@Builder
public class ReviewCreateRequestDto {

    @NotNull(message = "rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank(message = "comment is required")
    @Length(max = 50, message = "댓글은 최대 50자까지 작성할 수 있습니다.")
    private String comment;

    @NotNull(message = "isPrivate is required")
    private Boolean isPrivate;

    public Review toReview() {
        return Review.builder()
            .rating(rating)
            .comment(comment)
            .isPrivate(isPrivate)
            .build();
    }
}