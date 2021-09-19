package teamtridy.tridy.controller;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.CurrentUser;
import teamtridy.tridy.dto.InterestRecommendReadResponseDto;
import teamtridy.tridy.dto.MainRecommendCreateRequestDto;
import teamtridy.tridy.dto.MainRecommendReadResponseDto;
import teamtridy.tridy.error.CustomException;
import teamtridy.tridy.error.ErrorCode;
import teamtridy.tridy.service.KakaoService;
import teamtridy.tridy.service.OpenWeatherService;
import teamtridy.tridy.service.RecommendService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts/{accountId}/recommends")
@Validated
@Slf4j
public class RecommendController {

    private static final Double JEJU_AIRPORT_LATITUDE = 33.5072404;
    private static final Double JEJU_AIRPORT_LONGITUDE = 126.4924838;

    private static final String STR_JEJU_AIRPORT_LATITUDE = "33.5072404";
    private static final String STR_JEJU_AIRPORT_LONGITUDE = "126.4924838";

    private final OpenWeatherService openWeatherService;
    private final KakaoService kakaoService;
    private final RecommendService recommendService;

    @GetMapping("/mains")
    public ResponseEntity<MainRecommendReadResponseDto> readMain(
            @CurrentUser Account account,
            @PathVariable Long accountId,
            @RequestParam(defaultValue = STR_JEJU_AIRPORT_LATITUDE) Double latitude,
            @RequestParam(defaultValue = STR_JEJU_AIRPORT_LONGITUDE) Double longitude) {

        if (accountId != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        String address = kakaoService.getAddress(latitude, longitude);
        Boolean shouldBeIndoorsToday = openWeatherService.shouldBeIndoorsToday(latitude, longitude);

        return new ResponseEntity(recommendService
                .readMain(account, latitude, longitude, address, shouldBeIndoorsToday),
                HttpStatus.OK);
    }

    @PostMapping("/mains")
    public ResponseEntity<MainRecommendReadResponseDto> createMain(
            @CurrentUser Account account,
            @PathVariable Long accountId,
            @Valid MainRecommendCreateRequestDto mainRecommendCreateRequestDto) {

        if (accountId != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Double latitude = mainRecommendCreateRequestDto.getLatitude();
        Double longitude = mainRecommendCreateRequestDto.getLongitude();

        latitude = (latitude == null) ? JEJU_AIRPORT_LATITUDE : latitude;
        longitude = (longitude == null) ? JEJU_AIRPORT_LONGITUDE : longitude;

        String address = kakaoService.getAddress(latitude, longitude);
        Boolean shouldBeIndoorsToday = openWeatherService.shouldBeIndoorsToday(latitude, longitude);

        return new ResponseEntity(recommendService
                .createMain(account, latitude, longitude, address, shouldBeIndoorsToday),
                HttpStatus.CREATED);
    }

    @GetMapping("/interests")
    public ResponseEntity<InterestRecommendReadResponseDto> readInterest(
            @CurrentUser Account account,
            @PathVariable Long accountId) {
        if (accountId != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return new ResponseEntity(recommendService
                .readInterest(account), HttpStatus.OK);
    }

}
