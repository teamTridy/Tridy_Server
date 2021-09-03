package teamtridy.tridy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.config.TokenProvider;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.RefreshToken;
import teamtridy.tridy.domain.entity.UserAccount;
import teamtridy.tridy.domain.repository.AccountRepository;
import teamtridy.tridy.domain.repository.RefreshTokenRepository;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.dto.TokenDto;
import teamtridy.tridy.util.SecurityUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인한 유저 정보 반환 to @CurrentUser
    @Transactional(readOnly = true)
    public Account getCurrentAccount() {
        return accountRepository.findBySocialId(SecurityUtil.getCurrentUserName()); //userName == socialId
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //userName == socialId
        Account account = accountRepository.findBySocialId(username);

        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserAccount(account);
    }

    // 로그인
    @Transactional
    public SigninResponseDto signin(String socialId) {
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(socialId, null);

        // 2. 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .email(authentication.getName())
                .tokenValue(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 5. 토큰 포함 현재 유저 정보 반환
        SigninResponseDto signinResponseDto = SigninResponseDto.of(getCurrentAccount(), tokenDto);
        return signinResponseDto;
    }
}
