package teamtridy.tridy.controller;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import teamtridy.tridy.dto.SigninRequestDto;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.dto.SignupRequestDto;
import teamtridy.tridy.service.AccountService;
import teamtridy.tridy.service.AppleService;
import teamtridy.tridy.service.GoogleService;
import teamtridy.tridy.service.KakaoService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Validated
@Slf4j
public class AccountController {

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

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> login(
            @Valid @RequestBody SigninRequestDto signinRequestDto) {
        String socialType = signinRequestDto.getSocialType();
        String socialToken = signinRequestDto.getSocialToken();
        String socialId = getSocialId(socialType, socialToken); //여기 안에서 에러처리 다 해서 null값 안넘어오게 해야함.
        return ResponseEntity.ok(accountService.signin(socialId));
    }

    @PostMapping("/signup")
    public ResponseEntity<SigninResponseDto> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        String socialType = signupRequestDto.getSocialType();
        String socialToken = signupRequestDto.getSocialToken();
        String socialId = getSocialId(socialType, socialToken);
        accountService.signup(signupRequestDto.toServiceDto(socialId));
        return ResponseEntity.ok(accountService.signin(socialId));
    }

    @GetMapping("/duplicate/nickname")
    public ResponseEntity isDuplicatedNickname(
            @RequestParam String nickname) { //(required = true) true
        accountService.isDuplicatedNickname(nickname);
        return new ResponseEntity(HttpStatus.OK);
    }


    @GetMapping("/{accountId}/picks")
    public ResponseEntity<PickReadAllResponseDto> readAllPick(@CurrentUser Account account,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size) {
        return ResponseEntity
                .ok(accountService.readAllPick(account, accountId, page, size));
    }

    @GetMapping("/{accountId}/reviews")
    public ResponseEntity<AccountReviewReadAllResponseDto> readAllReview(
            @CurrentUser Account account,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size) {
        return ResponseEntity
                .ok(accountService.readAllReview(account, accountId, page, size));
    }
}
