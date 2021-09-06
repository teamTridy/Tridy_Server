package teamtridy.tridy.service.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Interest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class InterestDto { //멤버가 하나일때는 json에서 "id": 1 이렇게 넘기면 안되고 그냥 1 이렇게 넘겨야함.
    @Min(1)
    @Max(8)
    @NotNull
    private Long id;
    private String name;

    public static InterestDto of(Interest interest) {
        return InterestDto.builder().id(interest.getId()).name(interest.getName()).build();
    }
}
