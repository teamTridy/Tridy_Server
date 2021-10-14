package teamtridy.tridy.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.SignupDto;

@Data
@Builder
public class SignupEmailRequestDto {

    @Email
    @NotBlank(message = "email is required")
    private String email;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=-_!])(?=\\S+$).{6,12}$")
    private String password;

    @Pattern(regexp = "[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,10}", message = "닉네임은 2글자 이상 10글자 이하의 한글,영어,숫자로 입력해야 합니다.")
    @NotBlank(message = "nickname is required")
    private String nickname;

    public SignupDto toServiceDto() {
        return SignupDto.builder()
                .socialId(email)
                .password(password)
                .nickname(nickname)
                .build();
    }
}