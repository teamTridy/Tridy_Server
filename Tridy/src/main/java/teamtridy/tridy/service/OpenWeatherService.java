package teamtridy.tridy.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import teamtridy.tridy.dto.WeatherCurrentResponseDto;
import teamtridy.tridy.service.dto.OpenWeatherCurrentResponseDto;
import teamtridy.tridy.service.dto.OpenWeatherDailyResponseDto;

@RequiredArgsConstructor
@Service
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

        // Request current weather
        ResponseEntity<OpenWeatherCurrentResponseDto> response = restTemplate
                .exchange(url, HttpMethod.GET, request,
                        OpenWeatherCurrentResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            OpenWeatherCurrentResponseDto.Current current = response.getBody().getCurrent();
            Integer temp = current.getTemp().intValue();
            Integer id = current.getWeather().get(0).getId();
            String description = getDescription(id);
            return WeatherCurrentResponseDto.builder().time(LocalDateTime.now()).address(address)
                    .latitude(latitude).longitude(longitude)
                    .temp(temp).description(description).build();
        } else {
            throw new RuntimeException("통신 오류");
        }
    }

    public String getDailyWeather(Double latitude, Double longitude,
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
                .queryParam("exclude", "hourly,minutely,alerts,current") // 제외할 항목
                .queryParam("units", "metric").toUriString();

        // Request current weather
        ResponseEntity<OpenWeatherDailyResponseDto> response = restTemplate
                .exchange(url, HttpMethod.GET, request,
                        OpenWeatherDailyResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            OpenWeatherDailyResponseDto.Daily daily = response.getBody().getDaily().get(0); //배열 형태
            Integer id = daily.getWeather().get(0).getId(); // get(0) == 오늘 정오
            String description = getDescription(id);
            return description;
        } else {
            throw new RuntimeException("통신 오류");
        }
    }

    // https://openweathermap.org/weather-conditions
    private String getDescription(Integer id) {
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
                return getAtmosphereDescription(strId);
            case '8':
                if (strId.equals("800")) {
                    return "맑음";
                } else { //80x
                    return "구름";
                }
        }
        return "기타";
    }

    private String getAtmosphereDescription(String strId) {
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


}
