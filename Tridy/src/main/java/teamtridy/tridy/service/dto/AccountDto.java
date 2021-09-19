package teamtridy.tridy.service.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;

@Data
@Builder
public class AccountDto {

    private Long id;
    private String nickname;
    private TendencyDto tendency;

    public static AccountDto of(Account account, List<Long> interestIds) {
        AccountDto accountDto = AccountDto.builder().id(account.getId())
                .nickname(account.getNickname())
                .build();

        if (account.getHasTendency()) {
            TendencyDto tendency = TendencyDto.builder().interestIds(interestIds)
                    .isPreferredFar(account.getIsPreferredFar())
                    .isPreferredPopular(account.getIsPreferredPopular()).build();
            accountDto.setTendency(tendency);
        }
        return accountDto;
    }

    @Override
    public String toString() {
        return "Account";
    }
}
