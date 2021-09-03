package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;

@Data
@Builder
public class SigninResponseDto {
    private Long id;
    private String nickname;
    private TokenDto token;

    public static SigninResponseDto of(Account account, TokenDto token) {
        SigninResponseDto signinResponseDto = SigninResponseDto
                .builder()
                .id(account.getId())
                .nickname(account.getNickname())
                .build();
        signinResponseDto.setToken(token);
        return signinResponseDto;
    }
}
