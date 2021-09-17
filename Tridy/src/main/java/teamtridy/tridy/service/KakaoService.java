package teamtridy.tridy.service;

import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import teamtridy.tridy.error.ExternalException;
import teamtridy.tridy.service.dto.KakaoAddressResponseDto;
import teamtridy.tridy.service.dto.KakaoInfoResponseDto;

@RequiredArgsConstructor
@Service
public class KakaoService {

    private static final String BLANK_SPACE = " ";
    private final RestTemplate restTemplate;
    @Value("${social.kakao.client_id}")
    private String kakaoClientId;
    @Value("${social.kakao.url.access_token_info}")
    private String kakaoUrlAccessTokenInfo;

    @Value("${address.kakao.rest_api_key}")
    private String kakaoRestApiKey;
    @Value("${address.kakao.url.coord2address}")
    private String kakaCoord2addressUrl;

    public String getSocialId(String accessToken) {

        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers); //HttpEntity<SubmitData> entity = new HttpEntity<>(requestDto, headers);


        /*  getForEntity() 메서드의 경우에는 응답을 ResponseEntity 객체로 받게 됩니다.
            getForObject()와 달리 HTTP 응답에 대한 추가 정보를 담고 있어서 GET 요청에 대한 응답 코드, 실제 데이터를 확인할 수 있습니다.
            또한 ResponseEntity<T> 제네릭 타입에 따라서 응답을 String이나 Object 객체로 받을 수 있습니다.
            getForObject는 응답을 Response DTO에 직접 매핑 할 수도 있습니다.
         */

        ResponseEntity<KakaoInfoResponseDto> response;
        // Request access token info
        try {
            response = restTemplate
                    .exchange(kakaoUrlAccessTokenInfo, HttpMethod.GET, request,
                            KakaoInfoResponseDto.class);
        } catch (Exception e) {
            throw new ExternalException(e); // http status != 2xx
        }

        return response.getBody().getId().toString();
    }

    public String getAddress(Double latitude, Double longitude) {

        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

        // set uri
        String url = UriComponentsBuilder.fromHttpUrl(kakaCoord2addressUrl)
                .queryParam("x", longitude) // longitude(경도)
                .queryParam("y", latitude).toUriString(); // latitude(위도)

        // Set http entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null,
                headers);

        ResponseEntity<KakaoAddressResponseDto> response;
        try {
            // Request coord2address
            response = restTemplate.exchange(url, HttpMethod.GET, request,
                    KakaoAddressResponseDto.class);
        } catch (Exception e) {
            throw new ExternalException(e); // http status != 2xx
        }

        if (response.getBody().getMeta().getTotalCount() == 1) {
            KakaoAddressResponseDto.Document.Address address = response.getBody().getDocuments()
                    .get(0).getAddress();

            /*
                https://ifuwanna.tistory.com/221 [IfUwanna IT]
                StringBuffer/StringBuilder 는 가변성 가지기 때문에 .append() .delete() 등의 API를 이용하여 동일 객체내에서 문자열을 변경하는 것이 가능
                StringBuffer는 thread-safe => 웹에 사용하기 적당
             */
            return new StringJoiner(BLANK_SPACE)
                    .add(address.getRegion1depthName())
                    .add(address.getRegion2depthName())
                    .add(address.getRegion3depthName().split(BLANK_SPACE)[0])
                    .toString();
        }
        return null;
    }

}