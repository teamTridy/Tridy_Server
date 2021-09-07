package teamtridy.tridy.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.AccountInterest;
import teamtridy.tridy.service.dto.TendencyDto;

@Getter
@AllArgsConstructor
public class TendencyUpdateRequestDto extends TendencyDto {

    public void apply(Account account, List<AccountInterest> accountInterests) {
        account.updateTendency(getIsPreferredFar(),
                getIsPreferredPopular(),
                accountInterests);
    }
}
