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
import org.springframework.web.bind.annotation.RequestBody;
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

    private static final String JEJU_ISLAND = "제주특별자치도";
    private static final String BLANK_SPACE = " ";
    private final OpenWeatherService openWeatherService;
    private final KakaoService kakaoService;
    private final RecommendService recommendService;

    private boolean isInJeju(String address) {
        String region1depthName = address.split(BLANK_SPACE)[0];
        return region1depthName.equals(JEJU_ISLAND);
    }

    @GetMapping("/mains")
    public ResponseEntity<MainRecommendReadResponseDto> readMain(
            @CurrentUser Account account,
            @PathVariable Long accountId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        if (accountId != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        String address = null; // latitude == null || longitude == null
        if (latitude != null && longitude != null) {
            address = kakaoService.getAddress(latitude, longitude);
        }

        if (address == null || !isInJeju(address)) {
            address = JejuAirport.ADDRESS;
            latitude = JejuAirport.LATITUDE;
            longitude = JejuAirport.LONGITUDE;
        }

        Boolean shouldBeIndoorsToday = openWeatherService.shouldBeIndoorsToday(latitude, longitude);

        return new ResponseEntity(recommendService
                .readMain(account, latitude, longitude, address, shouldBeIndoorsToday),
                HttpStatus.OK);
    }

    @PostMapping("/mains")
    public ResponseEntity<MainRecommendReadResponseDto> createMain(
            @CurrentUser Account account,
            @PathVariable Long accountId,
            @Valid @RequestBody MainRecommendCreateRequestDto mainRecommendCreateRequestDto) {

        if (accountId != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Double latitude = mainRecommendCreateRequestDto.getLatitude();
        Double longitude = mainRecommendCreateRequestDto.getLongitude();

        String address = null; // latitude == null || longitude == null
        if (latitude != null && longitude != null) {
            address = kakaoService.getAddress(latitude, longitude);
        }

        if (address == null || !isInJeju(address)) {
            address = JejuAirport.ADDRESS;
            latitude = JejuAirport.LATITUDE;
            longitude = JejuAirport.LONGITUDE;
        }

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
