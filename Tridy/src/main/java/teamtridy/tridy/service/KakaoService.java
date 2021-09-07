package teamtridy.tridy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import teamtridy.tridy.dto.KakaoInfoResponseDto;

@RequiredArgsConstructor
@Service
public class KakaoService {

    private final RestTemplate restTemplate;

    @Value("${social.kakao.client_id}")
    private String kakaoClientId;
    @Value("${social.kakao.url.access_token_info}")
    private String kakaoUrlAccessTokenInfo;

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

        // Request access token info
        ResponseEntity<KakaoInfoResponseDto> response = restTemplate
            .exchange(kakaoUrlAccessTokenInfo, HttpMethod.GET, request, KakaoInfoResponseDto.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getId().toString();
        }
        return null;
    }
}