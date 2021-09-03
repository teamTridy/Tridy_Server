package teamtridy.tridy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.dto.SigninRequestDto;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.service.AccountService;
import teamtridy.tridy.service.AppleService;
import teamtridy.tridy.service.GoogleService;
import teamtridy.tridy.service.KakaoService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AccountController {
    private final AccountService accountService;
    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final AppleService appleService;

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> login(@Valid @RequestBody SigninRequestDto signinRequestDto) {
        String socialType = signinRequestDto.getSocialType();
        String socialToken = signinRequestDto.getSocialToken();

        String socialId = null;
        if (socialType.equals("kakao")) {
            socialId = kakaoService.getSocialId(socialToken);
        }if (socialType.equals("google")) {
            socialId = googleService.getSocialId(socialToken);
        }if (socialType.equals("apple")) {
            socialId = appleService.getSocialId(socialToken);
        }

        return ResponseEntity.ok(accountService.signin(socialId));
    }

}
