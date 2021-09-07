package teamtridy.tridy.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.config.TokenProvider;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.AccountInterest;
import teamtridy.tridy.domain.entity.Interest;
import teamtridy.tridy.domain.entity.RefreshToken;
import teamtridy.tridy.domain.entity.UserAccount;
import teamtridy.tridy.domain.repository.AccountInterestRepository;
import teamtridy.tridy.domain.repository.AccountRepository;
import teamtridy.tridy.domain.repository.InterestRepository;
import teamtridy.tridy.domain.repository.RefreshTokenRepository;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.dto.TokenDto;
import teamtridy.tridy.exception.AlreadyExistsException;
import teamtridy.tridy.service.dto.AccountDto;
import teamtridy.tridy.service.dto.InterestDto;
import teamtridy.tridy.service.dto.SignupDto;
import teamtridy.tridy.service.dto.TestDto;
import teamtridy.tridy.util.SecurityUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final AccountInterestRepository accountInterestRepository;
    private final InterestRepository interestRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 로그인한 유저 정보 반환 to @CurrentUser
    @Transactional(readOnly = true)
    public Account getCurrentAccount() {
        return accountRepository
            .findBySocialId(SecurityUtil.getCurrentUserName()); //userName == socialId
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException { //userName == socialId
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
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            socialId, socialId);

        // 2. 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject()
            .authenticate(authenticationToken);

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
        Account account = getCurrentAccount();
        List<InterestDto> interestDtos = account.getAccountInterests().stream()
            .map(accountInterest -> InterestDto.of(accountInterest.getInterest()))
            .collect(Collectors.toList());
        AccountDto accountDto = AccountDto.of(account, interestDtos);
        SigninResponseDto signinResponseDto = SigninResponseDto.of(accountDto, tokenDto);

        return signinResponseDto;
    }

    // 회원가입
    @Transactional
    public void signup(SignupDto signupDto) {
        if (accountRepository.existsBySocialId(signupDto.getSocialId())) {
            throw new AlreadyExistsException("이미 가입되어 있는 유저입니다");
        }
        Account account = signupDto.toAccount();
        accountRepository.save(account);

        TestDto testDto = signupDto.getTest();

        if (testDto != null) {
            List<InterestDto> interestDtos = testDto.getInterests();
            List<Interest> interests = interestDtos.stream()
                .map(interestDto -> interestRepository.findById(interestDto.getId()).get())
                .collect(Collectors.toList());
            List<AccountInterest> accountInterests = interests.stream().map(
                interest -> AccountInterest.builder().account(account).interest(interest).build())
                .collect(Collectors.toList());
            accountInterests = accountInterests.stream()
                .map(accountInterest -> accountInterestRepository.save(accountInterest))
                .collect(Collectors.toList());

            account.updateTestResult(testDto.getIsPreferredFar(), testDto.getIsPreferredPopular(),
                accountInterests);
        }
    }

    @Transactional
    public Boolean isDuplicatedNickname(String nickname) {
        if (accountRepository.existsByNickname(nickname)) {
            throw new AlreadyExistsException("이미 존재하는 닉네임 입니다.");
        }
        return true;
    }
}
