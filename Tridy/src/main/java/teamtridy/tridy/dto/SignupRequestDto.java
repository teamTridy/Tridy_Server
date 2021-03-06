package teamtridy.tridy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.SignupDto;

@Data
@Builder
public class SignupRequestDto {

    @NotBlank(message = "socialType is required")
    private String socialType;

    @NotBlank(message = "socialToken is required")
    private String socialToken;

    @Pattern(regexp = "[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,10}", message = "닉네임은 2글자 이상 10글자 이하의 한글,영어,숫자로 입력해야 합니다.")
    @NotBlank(message = "nickname is required")
    private String nickname;

    public SignupDto toServiceDto(String socialId) {
        return SignupDto.builder()
                .socialId(socialId)
                .password(socialId)
                .nickname(nickname)
                .build();
    }
}