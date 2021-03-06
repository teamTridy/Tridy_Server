package teamtridy.tridy.controller;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.CurrentUser;
import teamtridy.tridy.dto.AccountReviewReadAllResponseDto;
import teamtridy.tridy.dto.PickReadAllResponseDto;
import teamtridy.tridy.dto.SigninEmailRequestDto;
import teamtridy.tridy.dto.SigninRequestDto;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.dto.SignupEmailRequestDto;
import teamtridy.tridy.dto.SignupRequestDto;
import teamtridy.tridy.dto.TendencyUpdateRequestDto;
import teamtridy.tridy.service.AccountService;
import teamtridy.tridy.service.AppleService;
import teamtridy.tridy.service.GoogleService;
import teamtridy.tridy.service.KakaoService;
import teamtridy.tridy.service.RecommendService;
import teamtridy.tridy.service.dto.AccountDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Validated
@Slf4j
public class AccountController {

    private final RecommendService recommendService;
    private final AccountService accountService;
    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final AppleService appleService;

    private String getSocialId(String socialType, String socialToken) {
        String socialId = null;
        if (socialType.equals("kakao")) {
            socialId = kakaoService.getSocialId(socialToken);
        }
        if (socialType.equals("google")) {
            socialId = googleService.getSocialId(socialToken);
        }
        if (socialType.equals("apple")) {
            socialId = appleService.getSocialId(socialToken);
        }
        return socialId;
    }

    @GetMapping("/duplicate/email")
    public ResponseEntity isDuplicatedEmail(
            @RequestParam @Email String email) {
        accountService.isDuplicatedEmail(email);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/duplicate/nickname")
    public ResponseEntity isDuplicatedNickname(
            @RequestParam String nickname) {
        accountService.isDuplicatedNickname(nickname);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> signin(
            @Valid @RequestBody SigninRequestDto signinRequestDto) {
        String socialType = signinRequestDto.getSocialType();
        String socialToken = signinRequestDto.getSocialToken();
        String socialId = getSocialId(socialType, socialToken); //?????? ????????? ???????????? ??? ?????? null??? ??????????????? ?????????.
        return new ResponseEntity(accountService.signin(socialId, socialId), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<SigninResponseDto> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        String socialType = signupRequestDto.getSocialType();
        String socialToken = signupRequestDto.getSocialToken();
        String socialId = getSocialId(socialType, socialToken);
        accountService.signup(signupRequestDto.toServiceDto(socialId));
        return new ResponseEntity(accountService.signin(socialId, socialId), HttpStatus.CREATED);
    }

    //????????????
    @PostMapping("/signup/email")
    public ResponseEntity<AccountDto> signupViaEmail(
            @Valid @RequestBody SignupEmailRequestDto signupEmailRequestDto) {
        accountService.signup(signupEmailRequestDto.toServiceDto());
        return new ResponseEntity(accountService
                .signin(signupEmailRequestDto.getEmail(), signupEmailRequestDto.getPassword()),
                HttpStatus.CREATED);
    }

    // ?????????
    @PostMapping("/signin/email")
    public ResponseEntity<AccountDto> siginViaEmail(
            @Valid @RequestBody SigninEmailRequestDto signinEmailRequestDto) {
        return new ResponseEntity(accountService
                .signin(signinEmailRequestDto.getEmail(), signinEmailRequestDto.getPassword()),
                HttpStatus.OK);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> read(@CurrentUser Account account,
            @PathVariable Long accountId) {
        return new ResponseEntity(accountService.read(account, accountId), HttpStatus.OK);
    }


    @PatchMapping("/{accountId}/tendency")
    public ResponseEntity<AccountDto> updateTendency(@CurrentUser Account account,
            @PathVariable Long accountId,
            @Valid @RequestBody TendencyUpdateRequestDto tendencyUpdateRequestDto) {
        recommendService.deleteInterest(account);
        return new ResponseEntity(
                accountService.updateTendency(account, accountId, tendencyUpdateRequestDto),
                HttpStatus.OK);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<AccountDto> delete(@CurrentUser Account account,
            @PathVariable Long accountId) {
        accountService.delete(account, accountId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/{accountId}/picks")
    public ResponseEntity<PickReadAllResponseDto> readAllPick(@CurrentUser Account account,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size) {
        return new ResponseEntity(accountService.readAllPick(account, accountId, page, size),
                HttpStatus.OK);
    }

    @GetMapping("/{accountId}/reviews")
    public ResponseEntity<AccountReviewReadAllResponseDto> readAllReview(
            @CurrentUser Account account,
            @PathVariable Long accountId,
            @RequestParam @Min(1900) @Max(2999) Integer year,
            @RequestParam @Min(1) @Max(12) @NotNull Integer month,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size) {
        return new ResponseEntity(
                accountService
                        .readAllReviewByYearAndMonth(account, accountId, year, month, page, size),
                HttpStatus.OK);
    }
}
