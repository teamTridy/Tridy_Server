package teamtridy.tridy.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.service.dto.TourCongestionResponseDto;
import teamtridy.tridy.service.dto.TourCongestionResponseDto.Items;

@RequiredArgsConstructor
@Service
@Slf4j
public class TourService {

    private final PlaceRepository placeRepository;

    private final RestTemplate restTemplate;
    @Value("${tour.service_key}")
    private String tourServiceKey;
    @Value("${tour.url.congestion}")
    private String tourCongestionUrl;
    @Value("${tour.mobile_app}")
    private String tourMobileApp;
    @Value("${tour.mobile_os}")
    private String tourMobileOS;

    @Cacheable(value = "congestionCache", key = "#placeId")
    @SneakyThrows
    public Integer getCongestionLevel(Long placeId) {
        // Set http entity
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers);

        // set uri
        // 서비스키 인코딩 오류로 인하여 UriConponentBuilder 대신 StringBuffer 사용
        Long originContentId = placeRepository.findById(placeId).get().getOriginContentId();

        String strDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd")); //2021-09-09 -> 20210909

        StringBuffer bufferedUri = new StringBuffer(
                tourCongestionUrl).append("?" + URLEncoder.encode("ServiceKey",
                StandardCharsets.UTF_8)
                + "=" + tourServiceKey)
                .append("&" + URLEncoder.encode("MobileOS", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(tourMobileOS, StandardCharsets.UTF_8))
                .append("&" + URLEncoder.encode("MobileApp", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(tourMobileOS, StandardCharsets.UTF_8))
                .append("&" + URLEncoder.encode("startYmd", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(strDate, StandardCharsets.UTF_8))
                .append("&" + URLEncoder.encode("endYmd", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode(strDate, StandardCharsets.UTF_8))
                .append("&" + URLEncoder.encode("contentId", StandardCharsets.UTF_8) + "="
                        + URLEncoder
                        .encode(originContentId.toString(), StandardCharsets.UTF_8))
                .append("&" + URLEncoder.encode("_type", StandardCharsets.UTF_8) + "=" + URLEncoder
                        .encode("json", StandardCharsets.UTF_8));
        URI uri = new URI(bufferedUri
                .toString()); // RestTemplate에서 string url로 요청시한번 더 encoding 하는 문제점으로 인해 uri을 인수로 전달

        ResponseEntity<TourCongestionResponseDto> response;

        try {
            response = restTemplate
                    .exchange(uri, HttpMethod.GET, request,
                            TourCongestionResponseDto.class);
        } catch (Exception e) {
            //throw new ExternalException(e);
            log.error("congestion error", e);
            return null; // read time out 발생시 혼잡도 조회 실패로 간주
        }

        Items items = response
                .getBody()
                .getResponse()
                .getBody()
                .getItems();

        if (items != null) {
            return items.getItem().getEstiDecoDivCd(); //(1:쾌적,2:여유,3:보통,4:약간혼잡,5:혼잡)
        } else {
            return null;
        }
    }
}
