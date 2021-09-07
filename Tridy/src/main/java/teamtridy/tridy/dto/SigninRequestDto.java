package teamtridy.tridy.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SigninRequestDto {

    @NotBlank(message = "socialType is required")
    private String socialType;

    @NotBlank(message = "socialToken is required")
    private String socialToken;
}