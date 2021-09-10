package teamtridy.tridy.service;

import java.net.URI;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import teamtridy.tridy.service.dto.TourCongestionResponseDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class TourService {

    private final RestTemplate restTemplate;
    @Value("${tour.service_key}")
    private String tourServiceKey;
    @Value("${tour.url.congestion}")
    private String tourCongestionUrl;
    @Value("${tour.mobile_app}")
    private String tourMobileApp;
    @Value("${tour.mobile_os}")
    private String tourMobileOS;

    @SneakyThrows
    public Integer getCongestionLevel(Long OriginContentId) {
        // Set http entity
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers);

        // set uri
        // 서비스키 인코딩 오류로 인하여 UriConponentBuilder 대신 StringBuffer 사용
        String strDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd")); //2021-09-09 -> 20210909

        StringBuffer bufferedUri = new StringBuffer(
                tourCongestionUrl).append("?" + URLEncoder.encode("ServiceKey", "UTF-8")
                + "=" + tourServiceKey)
                .append("&" + URLEncoder.encode("MobileOS", "UTF-8") + "=" + URLEncoder
                        .encode(tourMobileOS, "UTF-8"))
                .append("&" + URLEncoder.encode("MobileApp", "UTF-8") + "=" + URLEncoder
                        .encode(tourMobileOS, "UTF-8"))
                .append("&" + URLEncoder.encode("startYmd", "UTF-8") + "=" + URLEncoder
                        .encode(strDate, "UTF-8"))
                .append("&" + URLEncoder.encode("endYmd", "UTF-8") + "=" + URLEncoder
                        .encode(strDate, "UTF-8"))
                .append("&" + URLEncoder.encode("contentId", "UTF-8") + "=" + URLEncoder
                        .encode(OriginContentId.toString(), "UTF-8"))
                .append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder
                        .encode("json", "UTF-8"));
        URI uri = new URI(bufferedUri
                .toString()); // RestTemplate에서 string url로 요청시한번 더 encoding 하는 문제점으로 인해 uri을 인수로 전달

        // Request current weather
        ResponseEntity<TourCongestionResponseDto> response = restTemplate
                .exchange(uri, HttpMethod.GET, request,
                        TourCongestionResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Integer congestionLevel = response
                    .getBody()
                    .getResponse()
                    .getBody()
                    .getItems()
                    .getItem()
                    .getEstiDecoDivCd(); //(1:쾌적,2:여유,3:보통,4:약간혼잡,5:혼잡)
            return congestionLevel;
        } else {
            throw new RuntimeException("통신 오류");
        }
    }
}
