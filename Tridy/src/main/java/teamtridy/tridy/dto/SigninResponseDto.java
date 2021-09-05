package teamtridy.tridy.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.service.dto.AccountDto;

@Data
@Builder
public class SigninResponseDto {
    private AccountDto account;
    private TokenDto token;

    public static SigninResponseDto of(AccountDto accountDto, TokenDto tokenDto) {
        return SigninResponseDto.builder().account(accountDto).token(tokenDto).build();
    }
}
