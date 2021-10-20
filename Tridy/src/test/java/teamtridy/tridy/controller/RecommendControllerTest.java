package teamtridy.tridy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static teamtridy.tridy.util.ApiDocumentUtils.getDocumentRequest;
import static teamtridy.tridy.util.ApiDocumentUtils.getDocumentResponse;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import teamtridy.tridy.ApiDocumentationTest;
import teamtridy.tridy.dto.InterestRecommendPlaceDto;
import teamtridy.tridy.dto.InterestRecommendReadResponseDto;
import teamtridy.tridy.dto.MainRecommendCreateRequestDto;
import teamtridy.tridy.dto.MainRecommendReadResponseDto;
import teamtridy.tridy.service.dto.MainRecommendPlaceDto;
import teamtridy.tridy.service.dto.RecommendPlaceDto;

class RecommendControllerTest extends ApiDocumentationTest {

    @Test
    void readMain() throws Exception {
        //시큐리티 세팅
        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        String address = "제주특별자치도 서귀포시 남원읍 신례동로 256";
        Boolean shouldBeIndoorsToday = false;

        given(openWeatherService
                .shouldBeIndoorsToday(JejuAirport.LATITUDE, JejuAirport.LONGITUDE))
                .willReturn(shouldBeIndoorsToday);
        given(kakaoService.getAddress(JejuAirport.LATITUDE, JejuAirport.LONGITUDE))
                .willReturn(address);

        List<String> hashtags1 = Arrays.asList("경관포토", "걷기등산", "맑음", "커플");
        List<String> hashtags2 = Arrays.asList("맛집", "돈까스", "음식점");

        RecommendPlaceDto recommendPlaceDto3 = RecommendPlaceDto.builder().id(1L)
                .congestion(null)
                .distance(0.3F)
                .name("얌얌돈까스")
                .hashtags(hashtags2)
                .imgUrl(
                        "https://api.cdn.visitjeju.net/photomng/thumbnailpath/201804/30/7f6c7979-90ee-41fe-b353-613b8e18c425.jpg")
                .isPicked(false).build();

        List<RecommendPlaceDto> relates = Arrays
                .asList(recommendPlaceDto3, recommendPlaceDto3, recommendPlaceDto3);

        MainRecommendPlaceDto recommendPlaceDto1 = MainRecommendPlaceDto.builder().id(1L)
                .congestion(null)
                .distance(2.7F).name("거문오름 [세계자연유산]")
                .hashtags(hashtags1)
                .imgUrl(
                        "http://tong.visitkorea.or.kr/cms/resource/62/2661662_image2_1.jpg")
                .isPicked(true).relateds(relates).build();

        MainRecommendPlaceDto recommendPlaceDto2 = MainRecommendPlaceDto.builder().id(1L)
                .congestion(3)
                .distance(1.7F)
                .name("성산일출봉 [유네스코 세계자연유산]")
                .hashtags(hashtags1)
                .imgUrl(
                        "http://tong.visitkorea.or.kr/cms/resource/85/1876185_image3_1.jpg")
                .isPicked(false).relateds(relates).build();

        List<MainRecommendPlaceDto> mainRecommendPlaceDtos = Arrays
                .asList(recommendPlaceDto1, recommendPlaceDto1);
        MainRecommendReadResponseDto mainRecommendReadResponseDto = MainRecommendReadResponseDto
                .builder().address(address).places(mainRecommendPlaceDtos)
                .build();

        given(recommendService
                .readMain(account, JejuAirport.LATITUDE, JejuAirport.LONGITUDE, address,
                        shouldBeIndoorsToday))
                .willReturn(mainRecommendReadResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/{accountId}/recommends/mains", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .param("latitude", JejuAirport.STR_LATITUDE)
                        .param("longitude", JejuAirport.STR_LONGITUDE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("recommend-main-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("계정 고유 id값")
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
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("추천 당시 위치의 주소값"),
                                subsectionWithPath("places").type("List<RecommendPlace>")
                                        .description("메인 추천 장소 목록")
                        ),
                        responseFields(
                                beneathPath("places").withSubsectionId("recommendPlace"),
                                attributes(key("title").value("RecommendPlace")),
                                mainRecommendFields
                        )
                ));
    }

    @Test
    void createMain() throws Exception {

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        String address = JejuAirport.ADDRESS;
        Boolean shouldBeIndoorsToday = false;

        given(openWeatherService
                .shouldBeIndoorsToday(JejuAirport.LATITUDE, JejuAirport.LONGITUDE))
                .willReturn(shouldBeIndoorsToday);
        given(kakaoService.getAddress(JejuAirport.LATITUDE, JejuAirport.LONGITUDE))
                .willReturn(address);

        List<String> hashtags1 = Arrays.asList("경관포토", "걷기등산", "맑음", "커플");
        List<String> hashtags2 = Arrays.asList("맛집", "돈까스", "음식점");

        RecommendPlaceDto recommendPlaceDto3 = RecommendPlaceDto.builder().id(1L)
                .congestion(null)
                .distance(0.3F)
                .name("얌얌돈까스")
                .hashtags(hashtags2)
                .imgUrl(
                        "https://api.cdn.visitjeju.net/photomng/thumbnailpath/201804/30/7f6c7979-90ee-41fe-b353-613b8e18c425.jpg")
                .isPicked(false).build();

        List<RecommendPlaceDto> relates = Arrays
                .asList(recommendPlaceDto3, recommendPlaceDto3, recommendPlaceDto3);

        MainRecommendPlaceDto recommendPlaceDto1 = MainRecommendPlaceDto.builder().id(1L)
                .congestion(null)
                .distance(2.7F).name("거문오름 [세계자연유산]")
                .hashtags(hashtags1)
                .imgUrl(
                        "http://tong.visitkorea.or.kr/cms/resource/62/2661662_image2_1.jpg")
                .isPicked(true).relateds(relates).build();

        List<MainRecommendPlaceDto> mainRecommendPlaceDtos = Arrays
                .asList(recommendPlaceDto1, recommendPlaceDto1);
        MainRecommendReadResponseDto mainRecommendReadResponseDto = MainRecommendReadResponseDto
                .builder().address(address).places(mainRecommendPlaceDtos)
                .build();

        given(recommendService
                .createMain(account, JejuAirport.LATITUDE, JejuAirport.LONGITUDE, address,
                        shouldBeIndoorsToday))
                .willReturn(mainRecommendReadResponseDto);

        //when
        MainRecommendCreateRequestDto mainRecommendCreateRequestDto = MainRecommendCreateRequestDto
                .builder().latitude(JejuAirport.LATITUDE).longitude(JejuAirport.LONGITUDE)
                .build();

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/accounts/{accountId}/recommends/mains", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .content(objectMapper.writeValueAsString(mainRecommendCreateRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(document("recommend-main-create",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("계정 고유 id값")
                        ),
                        requestFields(
                                fieldWithPath("latitude")
                                        .description("위도\n(위치정보제공 미동의시 null)\n(기본값:"
                                                + JejuAirport.STR_LATITUDE + "(제주공항))").optional(),
                                fieldWithPath("longitude")
                                        .description("경도\n(위치정보제공 미동의시 null)\n(기본값:"
                                                + JejuAirport.STR_LONGITUDE + "(제주공항))").optional()
                        ),
                        responseFields(
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("추천 당시 위치의 주소값"),
                                subsectionWithPath("places").type("List<RecommendPlace>")
                                        .description("메인 추천 장소 목록")
                        ),
                        responseFields(
                                beneathPath("places").withSubsectionId("recommendPlace"),
                                attributes(key("title").value("RecommendPlace")),
                                mainRecommendFields
                        )
                ));
    }

    @Test
    void readInterest() throws Exception {

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        String address = "제주특별자치도 서귀포시 남원읍 신례동로 256";
        Boolean shouldBeIndoorsToday = false;

        given(openWeatherService
                .shouldBeIndoorsToday(JejuAirport.LATITUDE, JejuAirport.LONGITUDE))
                .willReturn(shouldBeIndoorsToday);
        given(kakaoService.getAddress(JejuAirport.LATITUDE, JejuAirport.LONGITUDE))
                .willReturn(address);

        List<String> hashtags1 = Arrays.asList("경관포토", "걷기등산", "맑음", "커플");

        RecommendPlaceDto recommendPlaceDto1 = RecommendPlaceDto.builder().id(1L)
                .congestion(null)
                .distance(2.7F).name("거문오름 [세계자연유산]")
                .hashtags(hashtags1)
                .imgUrl(
                        "http://tong.visitkorea.or.kr/cms/resource/62/2661662_image2_1.jpg")
                .isPicked(false).build();

        List<RecommendPlaceDto> interestRecommendPlaceDtos = Arrays
                .asList(recommendPlaceDto1, recommendPlaceDto1, recommendPlaceDto1);

        InterestRecommendPlaceDto interestRecommendPlace1Dto1 = InterestRecommendPlaceDto
                .builder().interestId(1L).places(interestRecommendPlaceDtos).build();
        InterestRecommendPlaceDto interestRecommendPlace2Dto2 = InterestRecommendPlaceDto
                .builder().interestId(2L).places(interestRecommendPlaceDtos).build();

        InterestRecommendReadResponseDto interestRecommendReadResponseDto = InterestRecommendReadResponseDto
                .builder().interest1(interestRecommendPlace1Dto1)
                .interest2(interestRecommendPlace2Dto2).build();
        given(recommendService
                .readInterest(account))
                .willReturn(interestRecommendReadResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/{accountId}/recommends/interests", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("recommend-interest-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("계정 고유 id값")
                        ),
                        responseFields(
                                subsectionWithPath("interest1").type("InterestRecommend")
                                        .description("관심활동1 추천"),
                                subsectionWithPath("interest2").type("InterestRecommend")
                                        .description("관심활동2 추천")
                        ),
                        responseFields(
                                beneathPath("interest1").withSubsectionId("interestRecommend"),
                                attributes(key("title").value("InterestRecommend")),
                                fieldWithPath("interestId").type(JsonFieldType.NUMBER)
                                        .description("관심활동 고유 id값"),
                                subsectionWithPath("places").type("List<RecommendPlace>")
                                        .description("관심활동 추천 장소 목록")
                        ),
                        responseFields(
                                beneathPath("interest1.places").withSubsectionId("recommendPlace"),
                                attributes(key("title").value("RecommendPlace")),
                                recommendFields
                        )

                ));

    }


}