package teamtridy.tridy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static teamtridy.tridy.util.ApiDocumentUtils.getDocumentRequest;
import static teamtridy.tridy.util.ApiDocumentUtils.getDocumentResponse;
import static teamtridy.tridy.util.DocumentFormatGenerator.getDateTimeFormat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import teamtridy.tridy.ApiDocumentationTest;
import teamtridy.tridy.dto.WeatherCurrentResponseDto;

class WeatherControllerTest extends ApiDocumentationTest {

    @Test
    void current() throws Exception {
        // 시큐리티 세팅
        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        WeatherCurrentResponseDto weatherCurrentResponseDto = WeatherCurrentResponseDto.builder()
                .address("제주특별자치도 서귀포시 남원읍 신례동로 256").temp(26).time(
                        LocalDateTime.now()).description("맑음").build();
        given(kakaoService.getAddress(33.3085171454, 126.6344317363))
                .willReturn("제주특별자치도 서귀포시 남원읍 신례동로 256");
        given(villageForecastService
                .getCurrentWeather(33.3085171454, 126.6344317363,
                        "제주특별자치도 서귀포시 남원읍 신례동로 256"))
                .willReturn(weatherCurrentResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/weathers/current")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .param("latitude", "33.3085171454")
                        .param("longitude", "126.6344317363")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("weather-current",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        requestParameters(
                                parameterWithName("latitude")
                                        .description("위도\n(위치정보제공 미동의시 생략)\n(기본값:"
                                                + JejuAirport.STR_LATITUDE + "(제주공항))").optional(),
                                parameterWithName("longitude")
                                        .description("경도\n(위치정보제공 미동의시 생략)\n(기본값:"
                                                + JejuAirport.STR_LONGITUDE + "(제주공항))").optional()
                        ),
                        responseFields(
                                fieldWithPath("time").type(JsonFieldType.STRING)
                                        .attributes(getDateTimeFormat())
                                        .description("현재 시간"),
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("현재 위치의 주소"),
                                fieldWithPath("temp").type(JsonFieldType.NUMBER)
                                        .description("현재 위치의 기온"),
                                subsectionWithPath("description").type(JsonFieldType.STRING)
                                        .description("현재 위치의 날씨 설명")
                        )
                ));
    }
}