package teamtridy.tridy.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import teamtridy.tridy.dto.WeatherCurrentResponseDto;
import teamtridy.tridy.error.ExternalException;
import teamtridy.tridy.service.dto.OpenWeatherCurrentResponseDto;
import teamtridy.tridy.service.dto.OpenWeatherDailyResponseDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class OpenWeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.open_weather.api_key}")
    private String openWeatherApiKey;
    @Value("${weather.open_weather.url.one_call}")
    private String openWeatherOneCallUrl;

    public WeatherCurrentResponseDto getCurrentWeather(Double latitude, Double longitude,
            String address) {
        HttpHeaders headers = new HttpHeaders();

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers); //HttpEntity<SubmitData> entity = new HttpEntity<>(requestDto, headers);

        // set uri
        String url = UriComponentsBuilder.fromHttpUrl(openWeatherOneCallUrl)
                .queryParam("appid", openWeatherApiKey)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("exclude", "hourly,minutely,alerts,daily") // 제외할 항목
                .queryParam("units", "metric").toUriString();

        ResponseEntity<OpenWeatherCurrentResponseDto> response;
        try {
            // Request current weather
            response = restTemplate
                    .exchange(url, HttpMethod.GET, request,
                            OpenWeatherCurrentResponseDto.class);
        } catch (Exception e) {
            throw new ExternalException(e); // http status != 2xx
        }

        OpenWeatherCurrentResponseDto.Current current = response.getBody().getCurrent();
        Integer temp = current.getTemp().intValue();
        Integer id = current.getWeather().get(0).getId();
        String description = getDescriptionByWeatherId(id);
        return WeatherCurrentResponseDto.builder().time(LocalDateTime.now()).address(address)
                .temp(temp).description(description).build();
    }

    public Boolean shouldBeIndoorsToday(Double latitude, Double longitude) {
        HttpHeaders headers = new HttpHeaders();

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers); //HttpEntity<SubmitData> entity = new HttpEntity<>(requestDto, headers);

        // set uri
        String url = UriComponentsBuilder.fromHttpUrl(openWeatherOneCallUrl)
                .queryParam("appid", openWeatherApiKey)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("exclude", "hourly,minutely,alerts,current") // 제외할 항목
                .queryParam("units", "metric").toUriString();

        ResponseEntity<OpenWeatherDailyResponseDto> response;

        try {
            // Request current weather
            response = restTemplate
                    .exchange(url, HttpMethod.GET, request,
                            OpenWeatherDailyResponseDto.class);
        } catch (Exception e) {
            throw new ExternalException(e); // http status != 2xx
        }

        OpenWeatherDailyResponseDto.Daily daily = response.getBody().getDaily()
                .get(0); //배열 형태
        Integer id = daily.getWeather().get(0).getId(); // get(0) == 오늘 정오

        return shouldBeIndoorsTodayByWeatherId(id);
    }

    // https://openweathermap.org/weather-conditions
    private String getDescriptionByWeatherId(Integer id) {
        String strId = id.toString();
        char group = strId.charAt(0);

        switch (group) {
            case '2':
                return "번개";
            case '3':
                return "이슬비";
            case '5':
                return "비";
            case '6':
                return "눈";
            case '7':
                return getDescriptionByAtmosphereId(strId);
            case '8':
                if (strId.equals("800")) {
                    return "맑음";
                } else { //80x
                    return "구름";
                }
        }
        return "기타";
    }

    private String getDescriptionByAtmosphereId(String strId) {
        char atmosphereGroup = strId.charAt(1);

        switch (atmosphereGroup) {
            case '0':
            case '1':
            case '2':
            case '4':
                return "안개";
            case '3':
            case '6':
                return "먼지";
            case '5':
                return "황사";
            case '7':
                return "돌풍";
            case '8':
                return "폭풍";
        }

        return "기타";
    }

    private Boolean shouldBeIndoorsTodayByWeatherId(Integer id) {
        String strId = id.toString();
        char group = strId.charAt(0);

        switch (group) {
            case '2':
            case '3':
            case '5':
            case '6':
                return true;
            case '7':
                return shouldBeIndoorsTodayByAtmosphereId(strId);
            case '8':
                return false;
        }
        return null;
    }

    private Boolean shouldBeIndoorsTodayByAtmosphereId(String strId) {
        char atmosphereGroup = strId.charAt(1);

        switch (atmosphereGroup) {
            case '0':
            case '1':
            case '2':
            case '4':
                return false;
            case '3': //"먼지"
            case '6': //"먼지"
            case '5': //"황사";
            case '7': //"돌풍";
            case '8': //"폭풍";
                return true;
        }

        return null;
    }


}
