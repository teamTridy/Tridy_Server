package teamtridy.tridy.domain.entity;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAccount extends User {
    private Account account;

    public UserAccount(Account account) {
        super(account.getSocialId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
