package teamtridy.tridy.service.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
public class TestDto {
    @NotNull(message = "isPreferredFar is required")
    private Boolean isPreferredFar;

    @NotNull(message = "isPreferredPopular is required")
    private Boolean isPreferredPopular;

    @Size(min=3, max=5, message = "관심사는 3개이상 5개이하 선택해야합니다.")
    @Valid
    @NotNull
    private List<InterestDto> interests;
}
