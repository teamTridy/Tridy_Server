package teamtridy.tridy.service.dto;

import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Account;

import java.util.List;

@Data
@Builder
public class AccountDto {
    private Long id;
    private String nickname;
    private Boolean allowsLocationPermission;

    private TestDto test;

    public static AccountDto of(Account account, List<InterestDto> interests) {
        TestDto test = TestDto.builder().interests(interests).isPreferredFar(account.getIsPreferredFar()).isPreferredPopular(account.getIsPreferredPopular()).build();
        return AccountDto.builder().id(account.getId()).nickname(account.getNickname()).allowsLocationPermission(account.getAllowsLocationPermission()).test(test).build();
    }
}
