package teamtridy.tridy.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import teamtridy.tridy.dto.WeatherCurrentResponseDto;
import teamtridy.tridy.error.ExternalException;
import teamtridy.tridy.service.dto.ShortForecastResponseDto;
import teamtridy.tridy.service.dto.UltraShortForecastResponseDto;
import teamtridy.tridy.service.dto.UltraShortForecastResponseDto.Items;

@RequiredArgsConstructor
@Service
@Slf4j
public class VillageForecastService {

    private static final List<LocalTime> SHORT_FORECAST_BASE_TIMES = Arrays
            .asList(LocalTime.of(2, 0), LocalTime.of(5, 0), LocalTime.of(8, 0),
                    LocalTime.of(11, 0),
                    LocalTime.of(14, 0), LocalTime.of(17, 0), LocalTime.of(20, 0),
                    LocalTime.of(23, 0));

    private static final String CATEGORY_SKY_CONDITION = "SKY"; //하늘 상태
    private static final String CATEGORY_TEMPERATURE = "T1H"; // 기온 (섭씨)
    private static final String CATEGORY_PRECIPITATION_FORM = "PTY"; //강수형태
    private final RestTemplate restTemplate;
    @Value("${weather.village_forecast.url.ultra_short_forecast}")
    private String ultraShortForecastUrl;
    @Value("${weather.village_forecast.url.short_forecast}")
    private String shortForecastUrl;
    @Value("${weather.village_forecast.service_key}")
    private String serviceKey;

    @SneakyThrows
    // @Cacheable(value = "villageForecastCurrentWeatherCache", key = "#gridX.toString()+#gridY.toString()+#baseDate+#baseTime") 동일 클래스 내부 메소드 호출시 캐시 작동 x https://brocess.tistory.com/236
    private WeatherCurrentResponseDto getCurrentWeather(Integer gridX, Integer gridY,
            String baseDate,
            String baseTime) {

        log.info("api calls");
        // Set http entity
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers);

        StringBuilder bufferedUri = new StringBuilder(
                ultraShortForecastUrl) /*URL*/
                .append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "="
                        + serviceKey) /*Service Key*/
                .append("&" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(serviceKey, StandardCharsets.UTF_8)) /*공공데이터포털에서 받은 인증키*/

                .append("&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode("1", StandardCharsets.UTF_8)) /*페이지번호*/
                .append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode("60", StandardCharsets.UTF_8)) /*한 페이지 결과 수*/
                .append("&" + URLEncoder.encode("dataType", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode("JSON", StandardCharsets.UTF_8)) /*요청자료형식(XML/JSON) Default: XML*/
                .append("&" + URLEncoder.encode("base_date", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(baseDate, StandardCharsets.UTF_8)) /*‘21년 6월 28일 발표*/
                .append("&" + URLEncoder.encode("base_time", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(baseTime, StandardCharsets.UTF_8)) /*06시 발표(정시단위) */
                .append("&" + URLEncoder.encode("nx", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode(gridX.toString(), StandardCharsets.UTF_8)) /*예보지점의 X 좌표값*/
                .append("&" + URLEncoder.encode("ny", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode(gridY.toString(), StandardCharsets.UTF_8)); /*예보지점의 Y 좌표값*/

        URI uri = new URI(bufferedUri
                .toString()); // RestTemplate에서 string url로 요청시한번 더 encoding 하는 문제점으로 인해 uri을 인수로 전달

        ResponseEntity<UltraShortForecastResponseDto> response;

        try {
            // Request current weather
            response = restTemplate
                    .exchange(uri, HttpMethod.GET, request,
                            UltraShortForecastResponseDto.class);
        } catch (Exception e) {
            throw new ExternalException(e);
        }

        try {
            Items items = response
                    .getBody()
                    .getResponse()
                    .getBody()
                    .getItems();

            String strTemp = items.getItem().stream()
                    .filter(item -> item.getCategory().equals(CATEGORY_TEMPERATURE))
                    .findFirst().get().getFcstValue();
            Integer temp = Integer.parseInt(strTemp);

            String precipitationForm = items.getItem().stream()
                    .filter(item -> item.getCategory().equals(CATEGORY_PRECIPITATION_FORM))
                    .findFirst().get().getFcstValue();

            String description;
            if (getDescriptionByPrecipitationForm(precipitationForm) != null) {
                description = getDescriptionByPrecipitationForm(precipitationForm);
            } else {
                String skyCondition = items.getItem().stream()
                        .filter(item -> item.getCategory().equals(CATEGORY_SKY_CONDITION))
                        .findFirst()
                        .get().getFcstValue();

                description = getDescriptionBySkyCondition(skyCondition);
            }

            return WeatherCurrentResponseDto.builder()
                    .temp(temp).description(description).build();

        } catch (Exception e) {
            throw new ExternalException(e);
        }
    }

    @SneakyThrows
    public WeatherCurrentResponseDto getCurrentWeather(Double latitude, Double longitude,
            String address) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime baseDateTime = now.minusHours(1).withMinute(0).withSecond(0)
                .withNano(0);
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HHmm"));
        Grid grid = convertGpsToGrid(latitude, longitude);

        WeatherCurrentResponseDto weatherCurrentResponseDto = getCurrentWeather(grid.x, grid.y,
                baseDate, baseTime);
        weatherCurrentResponseDto.setAddress(address);
        weatherCurrentResponseDto.setTime(now);
        return weatherCurrentResponseDto;
    }

    private String getDescriptionBySkyCondition(String skyCondition) {
        switch (skyCondition) {
            case "1":
                return "맑음";
            case "3":
                return "구름 많음";
            case "4":
                return "흐림";
        }
        return "기타";
    }

    private String getDescriptionByPrecipitationForm(String precipitationForm) {
        switch (precipitationForm) {
            case "0":
                return null;
            case "1":
            case "5":
                return "비";
            case "3":
            case "7":
                return "눈";
            case "2":
            case "6":
                return "비/눈";
        }
        return "기타";
    }

    public Grid convertGpsToGrid(double latitude, double longitude) {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double tan = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / tan;
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = tan;
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        Grid rs = new Grid();

        double ra = Math.tan(Math.PI * 0.25 + (latitude) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = longitude * DEGRAD - olon;
        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }
        theta *= sn;
        rs.x = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        rs.y = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        return rs;
    }

    @SneakyThrows
    private Boolean shouldBeIndoorsToday(Integer gridX, Integer gridY,
            String baseDate,
            String baseTime, String todayDate) {

        // Set http entity
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers);

        StringBuilder bufferedUri = new StringBuilder(
                shortForecastUrl) /*URL*/
                .append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "="
                        + serviceKey) /*Service Key*/
                .append("&" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(serviceKey, StandardCharsets.UTF_8)) /*공공데이터포털에서 받은 인증키*/

                .append("&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode("1", StandardCharsets.UTF_8)) /*페이지번호*/
                .append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode("300",
                                StandardCharsets.UTF_8)) /*한 페이지 결과 수*/ // 전날 23:00 기준으로 조회 시 오늘 00:00~ 오늘 23:00까지 조회하려면 최소 300개 필요.
                .append("&" + URLEncoder.encode("dataType", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode("JSON", StandardCharsets.UTF_8)) /*요청자료형식(XML/JSON) Default: XML*/
                .append("&" + URLEncoder.encode("base_date", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(baseDate, StandardCharsets.UTF_8)) /*‘21년 6월 28일 발표*/
                .append("&" + URLEncoder.encode("base_time", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(baseTime, StandardCharsets.UTF_8)) /*06시 발표(정시단위) */
                .append("&" + URLEncoder.encode("nx", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode(gridX.toString(), StandardCharsets.UTF_8)) /*예보지점의 X 좌표값*/
                .append("&" + URLEncoder.encode("ny", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode(gridY.toString(), StandardCharsets.UTF_8)); /*예보지점의 Y 좌표값*/

        URI uri = new URI(bufferedUri
                .toString()); // RestTemplate에서 string url로 요청시한번 더 encoding 하는 문제점으로 인해 uri을 인수로 전달

        ResponseEntity<ShortForecastResponseDto> response;

        try {
            // Request current weather
            response = restTemplate
                    .exchange(uri, HttpMethod.GET, request,
                            ShortForecastResponseDto.class);
        } catch (Exception e) {
            throw new ExternalException(e);
        }

        try {
            ShortForecastResponseDto.Items items = response
                    .getBody()
                    .getResponse()
                    .getBody()
                    .getItems();

            int shouldBeIndoorsTodayCount = (int) items.getItem().stream()
                    .filter(item -> item.getFcstDate().equals(todayDate))
                    .filter(item -> item.getFcstTime().compareTo("0600")
                            >= 0) // 오전 6시 이후 야외 활동 가능하므로 이 값 이후로 강수 체크. fcstTime > "0600" == true -> return 1.
                    .filter(item -> item.getCategory().equals(CATEGORY_PRECIPITATION_FORM))
                    .map(item -> shouldBeIndoorsTodayByPrecipitationForm(item.getFcstValue()))
                    .filter(shouldBeIndoorsToday ->
                            shouldBeIndoorsToday == true)
                    .count();

            return shouldBeIndoorsTodayCount > 0; // > 0 : true, < 0 : false

        } catch (Exception e) {
            throw new ExternalException(e);
        }
    }

    private Boolean shouldBeIndoorsTodayByPrecipitationForm(String precipitationForm) {
        if (precipitationForm.equals("0")) {
            return false;
        }
        return true;
    }

    @SneakyThrows
    public boolean shouldBeIndoorsToday(Double latitude, Double longitude) {

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime()
                .minusMinutes(10); // base time 10분 후 부터 API 조회 가능 고려

        Optional<LocalTime> maxTime = SHORT_FORECAST_BASE_TIMES.stream()
                .filter(baseTimes -> baseTimes.isBefore(nowTime)).max(LocalTime::compareTo);

        LocalDateTime baseDateTime;
        if (maxTime.isEmpty()) {
            baseDateTime = now.minusDays(1).withHour(23).withMinute(0);
        } else {
            baseDateTime = now.withHour(maxTime.get().getHour()).withMinute(
                    maxTime.get().getMinute());
        }

        String todayDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HHmm"));

        Grid grid = convertGpsToGrid(latitude, longitude);

        return shouldBeIndoorsToday(grid.x, grid.y, baseDate, baseTime, todayDate);
    }

    class Grid {

        public Integer x;
        public Integer y;
    }
}
