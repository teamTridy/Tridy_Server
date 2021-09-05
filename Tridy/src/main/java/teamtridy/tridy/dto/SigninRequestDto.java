package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class SigninRequestDto {
    @NotBlank(message = "socialType is required")
    private String socialType;

    @NotBlank(message = "socialToken is required")
    private String socialToken;
}