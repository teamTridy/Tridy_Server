package teamtridy.tridy.service.dto;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TendencyDto {

    @NotNull(message = "isPreferredFar is required")
    private Boolean isPreferredFar;

    @NotNull(message = "isPreferredPopular is required")
    private Boolean isPreferredPopular;

    @Size(min = 3, max = 5, message = "관심활동은 3개이상 5개이하 선택해야합니다.")
    @NotNull
    private List<Long> interestIds;

    @Override
    public String toString() {
        return "Tendency";
    }
}
