package teamtridy.tridy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import teamtridy.tridy.ApiDocumentationTest;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.dto.PlaceReadResponseDto;
import teamtridy.tridy.dto.PlaceReviewReadAllResponseDto;
import teamtridy.tridy.dto.ReviewCreateRequestDto;
import teamtridy.tridy.dto.ReviewUpdateRequestDto;
import teamtridy.tridy.service.dto.PlaceDto;
import teamtridy.tridy.service.dto.PlaceReviewDto;

class PlaceControllerTest extends ApiDocumentationTest {

    @Test
    void readAllByQuery() throws Exception {
        //시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        List<String> hashtags1 = Arrays.asList("경관포토", "걷기등산", "맑음", "커플");
        List<String> hashtags2 = Arrays.asList("걷기등산", "휴식힐링", "가족");

        PlaceDto placeDto1 = PlaceDto.builder().id(2L).address("제주특별자치도 제주시 조천읍 선교로 569-36")
                .name("거문오름 [세계자연유산]")
                .hashtags(hashtags1)
                .imgUrl("http://tong.visitkorea.or.kr/cms/resource/62/2661662_image2_1.jpg")
                .isPicked(true).build();

        PlaceDto placeDto2 = PlaceDto.builder().id(5L).address("제주특별자치도 제주시 구좌읍 종달논길")
                .name("용눈이오름")
                .hashtags(hashtags2)
                .imgUrl(
                        "http://tong.visitkorea.or.kr/cms/resource/29/1771329_image3_1.jpg")
                .isPicked(false).build();

        List<PlaceDto> placeDtos = Arrays.asList(placeDto1, placeDto2);
        PlaceReadAllResponseDto placeReadAllResponseDto = PlaceReadAllResponseDto.builder()
                .currentPage(1).currentSize(2).hasNextPage(false).places(placeDtos).build();

        given(placeService.readAllPlaceByQuery(account, 1, 10, "오름",
                Arrays.asList(2L), Arrays.asList(276L, 277L)))
                .willReturn(placeReadAllResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/places/search", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .queryParam("query", "오름")
                        .queryParam("depth2CategoryIds", "276,277")
                        .queryParam("regionIds", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("place-search",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        requestParameters(
                                parameterWithName("page")
                                        .description("요청 페이지\n(1 이상) (기본값: 1)").optional(),
                                parameterWithName("size")
                                        .description("요청 컨텐츠 개수\n(1 이상 30 이하) (기본값: 10)")
                                        .optional(),
                                parameterWithName("query")
                                        .description("검색할 키워드\n (2글자 이상)"),
                                parameterWithName("depth2CategoryIds")
                                        .description(
                                                "필터링할 depth2 카테고리 고유 id값들\n(id값은 Category API로 확인)\n(여러개일 경우 ,로 구분)")
                                        .optional(),
                                parameterWithName("regionIds")
                                        .description(
                                                "필터링할 지역 고유 id값들\n(2:제주시, 3:서귀포시)\n(여러개일 경우 ,로 구분)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지"),
                                fieldWithPath("currentSize").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지의 컨텐츠 개수"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN)
                                        .description("다음 페이지 존재 여부"),
                                subsectionWithPath("places").type("List<Place>")
                                        .description("장소 목록\n(없으면 [])")
                        ),
                        responseFields(
                                beneathPath("places").withSubsectionId("place"),
                                attributes(key("title").value("Place")),
                                placeFields
                        )
                ));
    }


    @Test
    void read() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        List<String> hashtags1 = Arrays.asList("경관포토", "걷기등산", "맑음", "커플");
        PlaceReadResponseDto placeReadResponseDto = PlaceReadResponseDto
                .builder()
                .id(1L)
                .name("거문오름 [세계자연유산]")
                .hashtags(hashtags1)
                .isPicked(false)
                .imgUrl("http://tong.visitkorea.or.kr/cms/resource/62/2661662_image2_1.jpg")
                .address("제주특별자치도 제주시 조천읍 선교로 569-36")
                .rep("064-710-8981")
                .mapUrl("http://place.map.kakao.com/7863269")
                .intro("문의 및 안내: 064-710-8980~1\n"
                        + " 주차시설: 주차가능\n"
                        + " 쉬는날: 매주 화요일(휴식의 날), 설날/추석, 기상악화시 전면 통제\n"
                        + " 이용시간: 탐방출발 시간 09:00~13:00(30분 간격 출발)\n"
                        + " 수용인원: 탐방인원 1일 450명 (평일, 휴일 구분 없음/단, 화요일은 휴식의 날 운영)\n"
                        + " ")
                .story("지역주민들 사이에는 분화구의 별칭으로 거물창(거멀창) 이라고 불리기도 하고, 숲으로 덮여 검게 보인다 하여 검은오름이라 부르고 있으나, 학자들의 어원적 해석으로는 `검은`은 神이란 뜻의 고조선 시대의 ` ·감·검`에 뿌리를 두는 것으로 풀이하고 있어요.\n"
                        + " 해발 456m의 복합형화산체인 거문오름으로부터 흘러나온 용암류가 지형경사를 따라 북동쪽의 방향으로 해안선까지 도달하면서 20여 개의 동굴 무리를 이루고 있는데, 용암동굴계 중에서 벵뒤굴, 만장굴, 김녕굴, 용천동굴, 그리고 당처물동굴은 세계자연유산으로 지정됐어요.")
                .info("숲으로 덮여 검게 보이는 거문오름")
                .build();

        given(placeService.read(account, 1L)).willReturn(placeReadResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/places/{placeId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("place-read",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("장소 고유 id값"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("장소 이름"),
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("장소 주소"),
                                fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                                        .description("장소 이미지").optional(),
                                fieldWithPath("hashtags").type("List<String>")
                                        .description("장소 해시태그 리스트"),
                                fieldWithPath("rep").type(JsonFieldType.STRING)
                                        .description("장소 전화번호\n(없으면 '--')"),
                                fieldWithPath("mapUrl").type(JsonFieldType.STRING)
                                        .description("장소 카카오맵").optional(),
                                fieldWithPath("intro").type(JsonFieldType.STRING)
                                        .description("장소 한줄소개\n(트리디의 첨언)"),
                                fieldWithPath("story").type(JsonFieldType.STRING)
                                        .description("장소 두줄개요\n(여행지소개)").optional(),
                                fieldWithPath("info").type(JsonFieldType.STRING)
                                        .description("장소 상세정보").optional(),
                                fieldWithPath("isPicked").type(JsonFieldType.BOOLEAN)
                                        .description("장소 찜 여부")
                        )
                ));
    }

    @Test
    void createPick() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        willDoNothing().given(placeService).createPick(account, 1L);

        //when
        ResultActions result = this.mockMvc.perform(
                post("/api/v1/places/{placeId}/picks", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("place-pick-create",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값")
                        )
                ));
    }

    @Test
    void deletePick() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        willDoNothing().given(placeService).deletePick(account, 1L);

        //when
        ResultActions result = this.mockMvc.perform(
                delete("/api/v1/places/{placeId}/picks", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("place-pick-delete",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값")
                        )
                ));
    }

    @Test
    void readAllReview() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        PlaceReviewDto placeReviewDto1 = PlaceReviewDto.builder().id(30L).authorNickname("트리디")
                .comment("괜찮은 장소예요")
                .rating(5).isAuthor(true).createdAt(LocalDate.now().minusDays(5))
                .build();
        PlaceReviewDto placeReviewDto2 = PlaceReviewDto.builder().id(18L).authorNickname("들희디")
                .comment("바람쐬기 좋은 장소")
                .rating(4).isAuthor(false).createdAt(LocalDate.now())
                .build();
        List<PlaceReviewDto> placeReviewDtos = Arrays.asList(placeReviewDto1, placeReviewDto2);

        List<Float> ratingRatios = Arrays.asList(0.05f, 0.15f, 0.28f, 0.10f, 0.42f);
        PlaceReviewReadAllResponseDto placeReviewReadAllResponseDtos = PlaceReviewReadAllResponseDto
                .builder()
                .lastReviewId(18L).currentSize(2).hasNextPage(false)
                .reviewTotalCount(12L)
                .ratingRatios(ratingRatios)
                .ratingAverage(3.1f)
                .reviews(placeReviewDtos).build();

        given(placeService.readAllReview(account, 1L, 30L, 10))
                .willReturn(placeReviewReadAllResponseDtos);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/places/{placeId}/reviews", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .queryParam("lastReviewId", "30")
                        .queryParam("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("place-review-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값")
                        ),
                        requestParameters(
                                parameterWithName("lastReviewId")
                                        .description(
                                                "이전 요청의 마지막 리뷰 고유 id값\n(첫 요청시 생략)\n(기본값: 9223372036854775807)")
                                        .optional(),
                                parameterWithName("size")
                                        .description("요청 컨텐츠 개수\n(1 이상 30 이하) (기본값: 10)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("lastReviewId").type(JsonFieldType.NUMBER)
                                        .description(
                                                "현재 요청의 마지막 리뷰 고유 id값\n(다음 요청시 이 값을 파라미터로 전달)\n(리뷰 총 개수가 0개면 null)")
                                        .optional(),
                                fieldWithPath("currentSize").type(JsonFieldType.NUMBER)
                                        .description("현재 요청의 컨텐츠 개수"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN)
                                        .description("다음 페이지 존재 여부"),
                                fieldWithPath("reviewTotalCount").type(JsonFieldType.NUMBER)
                                        .description("리뷰 총 개수"),
                                fieldWithPath("ratingRatios").type("List<Number>")
                                        .attributes(key("format").value("소수점 둘째자리까지"))
                                        .description("리뷰 평점 별 비율\n(1,2,3,4,5점 순서)"),
                                fieldWithPath("ratingAverage").type(JsonFieldType.NUMBER)
                                        .description("리뷰 총 평점 평균")
                                        .attributes(key("format").value("소수점 둘째자리까지")),
                                subsectionWithPath("reviews").type("List<PlaceReview>")
                                        .description("장소 리뷰 목록\n((없으면 [])")
                        ),
                        responseFields(
                                beneathPath("reviews").withSubsectionId("placeReview"),
                                attributes(key("title").value("PlaceReview")),
                                placeReviewResponseFields
                        )
                ));
    }

    @Test
    void saveReview() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        PlaceReviewDto placeReviewDto = PlaceReviewDto.builder().id(30L).authorNickname("tridy")
                .comment("good place")
                .rating(5).isAuthor(true).createdAt(LocalDate.now())
                .build();

        given(placeService.createReview(eq(account), eq(1L), any(ReviewCreateRequestDto.class)))
                .willReturn(placeReviewDto);

        //when
        ReviewCreateRequestDto reviewCreateRequestDto = ReviewCreateRequestDto.builder()
                .comment("I want to go again").isPrivate(false).rating(5).build();

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/places/{placeId}/reviews", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .content(objectMapper.writeValueAsString(reviewCreateRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(document("place-review-create",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값")
                        ),
                        requestFields(
                                reviewRequestFields
                        ),
                        responseFields(
                                placeReviewResponseFields
                        )
                ));
    }

    @Test
    void updateReview() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        PlaceReviewDto placeReviewDto = PlaceReviewDto.builder().id(30L).authorNickname("트리디")
                .comment("괜찮은 장소예요")
                .rating(5).isAuthor(true).createdAt(LocalDate.now())
                .build();

        given(placeService
                .updateReview(eq(account), eq(1L), eq(1L), any(ReviewUpdateRequestDto.class)))
                .willReturn(placeReviewDto);

        //when
        ReviewUpdateRequestDto reviewUpdateRequestDto = ReviewUpdateRequestDto.builder()
                .comment("It's a nice place.").isPrivate(false).rating(5).build();

        ResultActions result = this.mockMvc.perform(
                put("/api/v1/places/{placeId}/reviews/{reviewId}", 1L, 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .content(objectMapper.writeValueAsString(reviewUpdateRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("place-review-update",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값"),
                                parameterWithName("reviewId").description("리뷰 고유 id값")
                        ),
                        requestFields(
                                reviewRequestFields
                        ),
                        responseFields(
                                placeReviewResponseFields
                        )
                ));
    }

    @Test
    void deleteReview() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        willDoNothing().given(placeService).deleteReview(account, 1L, 1L);

        //when

        ResultActions result = this.mockMvc.perform(
                delete("/api/v1/places/{placeId}/reviews/{reviewId}", 1L, 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("place-review-delete",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("placeId").description("장소 고유 id값"),
                                parameterWithName("reviewId").description("리뷰 고유 id값")
                        )
                ));
    }
}