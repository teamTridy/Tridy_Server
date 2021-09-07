package teamtridy.tridy.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;

@Data
@Builder
public class SignupDto {

    @NotEmpty
    private String socialId;

    @Pattern(regexp = "[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]{2,10}", message = "닉네임은 2글자 이상 10글자 이하의 한글,영어,숫자로 입력해야 합니다.")
    @NotBlank(message = "nickname is required")
    private String nickname;

    @Valid
    private TendencyDto tendency;

    public Account toAccount() {
        return Account.builder()
                .socialId(socialId)
                .nickname(nickname)
                .hasTendency(false)
                .build();
    }
}
