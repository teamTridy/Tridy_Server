package teamtridy.tridy.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.dto.WeatherCurrentResponseDto;
import teamtridy.tridy.service.KakaoService;
import teamtridy.tridy.service.VillageForecastService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weathers")
@Validated
@Slf4j
public class WeatherController {

    private static final String JEJU_ISLAND = "제주특별자치도";
    private static final String BLANK_SPACE = " ";
    private final VillageForecastService villageForecastService;
    private final KakaoService kakaoService;

    private boolean isInJeju(String address) {
        String region1depthName = address.split(BLANK_SPACE)[0];
        return region1depthName.equals(JEJU_ISLAND);
    }

    @GetMapping("/current")
    public ResponseEntity<WeatherCurrentResponseDto> current(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        String address = null; // latitude == null || longitude == null
        if (latitude != null && longitude != null) {
            address = kakaoService.getAddress(latitude, longitude);
        }

        if (address == null || !isInJeju(address)) {
            address = JejuAirport.ADDRESS;
            latitude = JejuAirport.LATITUDE;
            longitude = JejuAirport.LONGITUDE;
        }

        return new ResponseEntity(
                villageForecastService.getCurrentWeather(latitude, longitude, address),
                HttpStatus.OK);
    }
}
