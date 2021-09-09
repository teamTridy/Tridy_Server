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
import teamtridy.tridy.service.OpenWeatherService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weather")
@Validated
@Slf4j
public class WeatherController {

    private final OpenWeatherService openWeatherService;
    private final KakaoService kakaoService;

    @GetMapping("/current")
    public ResponseEntity<WeatherCurrentResponseDto> current(@RequestParam Double latitude,
            @RequestParam Double longitude) {
        String address = kakaoService.getAddress(latitude, longitude);
        return new ResponseEntity(
                openWeatherService.getCurrentWeather(latitude, longitude, address),
                HttpStatus.OK);
    }
}
