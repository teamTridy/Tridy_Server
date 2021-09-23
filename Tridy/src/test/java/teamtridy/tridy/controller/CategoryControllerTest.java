package teamtridy.tridy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.service.dto.CategoryDto;
import teamtridy.tridy.service.dto.PlaceDto;

class CategoryControllerTest extends ApiDocumentationTest {

    @Test
    void readAll() throws Exception {
        // 시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        CategoryDto categoryDto3 = CategoryDto.builder().id(307L).name("자연생태관광지").depth(3)
                .children(null)
                .build();
        CategoryDto categoryDto2 = CategoryDto.builder().id(277L).name("산").depth(2)
                .children(Arrays.asList(categoryDto3, categoryDto3)).build();
        CategoryDto categoryDto1 = CategoryDto.builder().id(271L).name("자연").depth(1)
                .children(Arrays.asList(categoryDto2, categoryDto2)).build();
        CategoryDto categoryDto0 = CategoryDto.builder().id(0L).name("ROOT").depth(0)
                .children(Arrays.asList(categoryDto1, categoryDto1)).build();

        given(categoryService.readAll()).willReturn(categoryDto0);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("category-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        responseFields(
                                attributes(key("title").value("Category")),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("카테고리 고유 id값"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("depth").type(JsonFieldType.NUMBER)
                                        .description("카테고리 레벨\n(0:최상위 카테고리, 3:최하위 카테고리"),
                                subsectionWithPath("children").type("List<Category>")
                                        .description("하위 카테고리 목록\n(카테고리 정보는 동일) (없으면 null)")
                                        .optional()
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
        CategoryDto categoryDto1 = CategoryDto.builder().id(271L).name("자연").depth(1)
                .children(null).build();
        CategoryDto categoryDto0 = CategoryDto.builder().id(0L).name("ROOT").depth(0)
                .children(Arrays.asList(categoryDto1, categoryDto1, categoryDto1)).build();

        given(categoryService.read(0L)).willReturn(categoryDto0);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/categories/{categoryId}", 0L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("category-read",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("categoryId").description("카테고리 고유 id값\n"
                                        + "(depth1 카테고리 목록 조회는 id값을 0으로 설정 후 호출)")
                        ),

                        responseFields(
                                attributes(key("title").value("Category")),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description(
                                                "카테고리 고유 id값 \n (depth1 카테고리 목록 조회는 id값을 0으로 설정 후 호출)"),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("카테고리 이름"),
                                fieldWithPath("depth").type(JsonFieldType.NUMBER)
                                        .description("카테고리 레벨\n(0:최상위 카테고리, 3:최하위 카테고리"),
                                subsectionWithPath("children").type("List<Category>")
                                        .description("조회된 카테고리의 1 Depth 하위 카테고리 목록\n"
                                                + "(카테고리 정보 구조는 동일)\n"
                                                + "(2 Depth 이상의 children은 무조건 null로 표시)")
                                        .optional()
                        )
                ));
    }

    @Test
    void readAllPlaceByDepth1() throws Exception {
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
                .thumbImgUrl("http://tong.visitkorea.or.kr/cms/resource/62/2661662_image2_1.jpg")
                .isPicked(true).build();

        PlaceDto placeDto2 = PlaceDto.builder().id(5L).address("제주특별자치도 제주시 구좌읍 종달논길")
                .name("용눈이오름")
                .hashtags(hashtags2)
                .thumbImgUrl(
                        "http://tong.visitkorea.or.kr/cms/resource/29/1771329_image3_1.jpg")
                .isPicked(false).build();

        List<PlaceDto> placeDtos = Arrays.asList(placeDto1, placeDto2);
        PlaceReadAllResponseDto placeReadAllResponseDto = PlaceReadAllResponseDto.builder()
                .currentPage(1).currentSize(2).hasNextPage(false).places(placeDtos).build();

        given(categoryService
                .readAllPlaceByDepth1OrderByPopularity(account, 1, 10, 271L, "오름",
                        Arrays.asList(2L, 3L), Arrays.asList(306L, 307L, 308L)))
                .willReturn(placeReadAllResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/categories/{depth1categoryId}/places", 271L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .queryParam("sort", "popularity")
                        .queryParam("query", "오름")
                        .queryParam("depth3CategoryIds", "306,307,308")
                        .queryParam("regionIds", "2,3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("category-place-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("depth1categoryId")
                                        .description("depth1 카테고리 고유 id값")
                        ),
                        requestParameters(
                                parameterWithName("page")
                                        .description("요청 페이지\n(1 이상) (기본값: 1)").optional(),
                                parameterWithName("size")
                                        .description("요청 컨텐츠 개수\n(1 이상 30 이하) (기본값: 10)")
                                        .optional(),
                                parameterWithName("sort")
                                        .description(
                                                "요청 정렬 기준 \n(popularity/review) (기본값: popularity)")
                                        .optional(),
                                parameterWithName("query")
                                        .description(
                                                "검색할 키워드\n\n(정렬기준이 popularity일때만 요청 가능)")
                                        .optional(),
                                parameterWithName("depth3CategoryIds")
                                        .description(
                                                "필터링할 depth3 카테고리 고유 id값들\n(정렬기준이 popularity일때만 요청 가능)\n(id값은 Category API로 확인)\n(여러개일 경우 ,로 구분)")
                                        .optional(),
                                parameterWithName("regionIds")
                                        .description(
                                                "필터링할 지역 고유 id값들\n(2:제주시, 3:서귀포시)\n (정렬기준이 popularity일때만 요청 가능) \n (여러개일 경우 ,로 구분)")
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
}