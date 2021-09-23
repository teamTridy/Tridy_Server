package teamtridy.tridy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
import teamtridy.tridy.domain.entity.Pick;
import teamtridy.tridy.domain.entity.RefreshToken;
import teamtridy.tridy.domain.entity.Review;
import teamtridy.tridy.domain.entity.UserAccount;
import teamtridy.tridy.domain.repository.AccountInterestRepository;
import teamtridy.tridy.domain.repository.AccountRepository;
import teamtridy.tridy.domain.repository.InterestRepository;
import teamtridy.tridy.domain.repository.PickRepository;
import teamtridy.tridy.domain.repository.RefreshTokenRepository;
import teamtridy.tridy.domain.repository.ReviewRepository;
import teamtridy.tridy.dto.AccountReviewReadAllResponseDto;
import teamtridy.tridy.dto.PickReadAllResponseDto;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.dto.TendencyUpdateRequestDto;
import teamtridy.tridy.dto.TokenDto;
import teamtridy.tridy.error.CustomException;
import teamtridy.tridy.error.ErrorCode;
import teamtridy.tridy.service.dto.AccountDto;
import teamtridy.tridy.service.dto.PlaceDto;
import teamtridy.tridy.service.dto.ReviewDto;
import teamtridy.tridy.service.dto.SignupDto;
import teamtridy.tridy.util.SecurityUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final TokenProvider tokenProvider;
    private final AccountRepository accountRepository;
    private final AccountInterestRepository accountInterestRepository;
    private final InterestRepository interestRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PickRepository pickRepository;
    private final ReviewRepository reviewRepository;

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

    @Transactional
    public Boolean isDuplicatedNickname(String nickname) {
        if (accountRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATION);
        }
        return true;
    }

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
                .accountId(authentication.getName())
                .tokenValue(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 5. 토큰 포함 현재 유저 정보 반환
        Account account = getCurrentAccount();
        List<Long> interestIds = account.getAccountInterests().stream()
                .map(accountInterest -> accountInterest.getInterest().getId())
                .collect(Collectors.toList());
        AccountDto accountDto = AccountDto.of(account, interestIds);

        SigninResponseDto signinResponseDto = SigninResponseDto.of(accountDto, tokenDto);
        return signinResponseDto;
    }

    @Transactional
    public void signup(SignupDto signupDto) {
        if (accountRepository.existsBySocialId(signupDto.getSocialId())) {
            throw new CustomException(ErrorCode.ACCOUNT_DUPLICATION);
        }

        isDuplicatedNickname(signupDto.getNickname());

        Account account = signupDto.toAccount();
        accountRepository.save(account);
    }

    @Transactional
    public AccountDto read(Account account, Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getId() != accountId) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<Long> interestIds = account.getAccountInterests().stream()
                .map(accountInterest -> accountInterest.getInterest().getId())
                .collect(Collectors.toList());
        return AccountDto.of(account, interestIds);
    }

    @Transactional
    public AccountDto updateTendency(Account account, Long accountId,
            TendencyUpdateRequestDto tendencyUpdateRequestDto) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getId() != accountId) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<Interest> interests = tendencyUpdateRequestDto.getInterestIds().stream()
                .map(interestId -> interestRepository.findById(interestId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INTEREST_NOT_FOUND)))
                .collect(Collectors.toList());

        List<AccountInterest> accountInterests = interests.stream().map(
                interest -> AccountInterest.builder().account(account).interest(interest)
                        .build())
                .map(accountInterest -> accountInterestRepository.save(accountInterest))
                .collect(Collectors.toList());

        tendencyUpdateRequestDto.apply(account, accountInterests);

        return AccountDto.of(account, tendencyUpdateRequestDto.getInterestIds());
    }

    @Transactional
    public void delete(Account account, Long accountId) {
        if (account.getId() != accountId) {
            new CustomException(ErrorCode.ACCESS_DENIED);
        }
        accountRepository.delete(account);
    }

    @Transactional
    public PickReadAllResponseDto readAllPick(Account account, Long accountId,
            Integer page,
            Integer size) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getId() != accountId) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);

        Slice<Pick> picks = pickRepository
                .findByAccountOrderByIdDesc(account, pageRequest);

        List<PlaceDto> placeDtos = picks.stream()
                .map(pick -> PlaceDto.of(pick.getPlace(), account))
                .collect(Collectors.toList());

        return PickReadAllResponseDto.builder()
                .currentPage(picks.getNumber() + 1)
                .currentSize(picks.getNumberOfElements())
                .hasNextPage(picks.hasNext())
                .places(placeDtos).build();
    }

    @Transactional
    public AccountReviewReadAllResponseDto readAllReviewByYearAndMonth(Account account,
            Long accountId, Integer year, Integer month,
            Integer page,
            Integer size) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (account.getId() != accountId) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);

        // Period: A month
        LocalDate StartDate = LocalDate.of(year, month, 1);
        LocalDateTime startTime = LocalDateTime
                .of(StartDate, LocalTime.of(0, 0, 0));
        LocalDateTime endTime = LocalDateTime
                .of(StartDate.withDayOfMonth(StartDate.lengthOfMonth()), LocalTime.of(23, 59, 59));

        Slice<Review> reviews = reviewRepository
                .findByAccountAndCreatedAtBetweenOrderByIdDesc(account, startTime, endTime,
                        pageRequest);

        List<ReviewDto> reviewDtos = reviews.stream()
                .map(review -> ReviewDto.of(review, account))
                .collect(Collectors.toList());

        return AccountReviewReadAllResponseDto.builder()
                .year(year)
                .month(month)
                .currentPage(reviews.getNumber() + 1)
                .currentSize(reviews.getNumberOfElements())
                .hasNextPage(reviews.hasNext())
                .reviews(reviewDtos).build();
    }

}
