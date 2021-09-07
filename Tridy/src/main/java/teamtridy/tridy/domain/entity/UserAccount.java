package teamtridy.tridy.domain.entity;

import java.util.List;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@Getter
public class UserAccount extends User {

    private final Account account;

    public UserAccount(Account account) {
        super(account.getSocialId(), PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .encode(account.getSocialId()), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
