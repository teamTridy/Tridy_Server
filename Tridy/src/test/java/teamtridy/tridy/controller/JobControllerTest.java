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
import static teamtridy.tridy.util.DocumentFormatGenerator.getDateFormat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import teamtridy.tridy.ApiDocumentationTest;
import teamtridy.tridy.dto.JobReadAllResponseDto;
import teamtridy.tridy.dto.JobReadResponseDto;
import teamtridy.tridy.service.dto.JobDto;

class JobControllerTest extends ApiDocumentationTest {

    @Test
    void readAll() throws Exception {
        //시큐리티 세팅

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        JobDto jobDto1 = JobDto.builder()
                .id(1L)
                .name("알바1")
                .imgUrl(null)
                .address("제주 서귀포시")
                .workingDate("2021-09-21,2021-09-27")
                .workingHour("09:00~12:00")
                .salary("시급 10000원")
                .isClosed(true)
                .build();

        JobDto jobDto2 = JobDto.builder()
                .id(1L)
                .name("알바2")
                .imgUrl(null)
                .address("제주 서귀포시")
                .workingDate("2021-09-21~2021-09-27")
                .workingHour("09:00~12:00")
                .salary("시급 10000원")
                .isClosed(false)
                .build();

        List<JobDto> jobDtos = Arrays.asList(jobDto1, jobDto2);
        JobReadAllResponseDto jobReadAllResponseDto = JobReadAllResponseDto.builder()
                .currentPage(1).currentSize(2).hasNextPage(false).jobs(jobDtos).build();

        given(jobService.readAllByDateOrQuery("2021-09-21", null, 1, 10))
                .willReturn(jobReadAllResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/jobs")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .queryParam("date", "2021-09-21")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("job-read-all",
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
                                parameterWithName("date")
                                        .description(
                                                "조회할 날짜 \n (query 파라미터 요청시 생략) \n (yyyy-MM-dd 형식)")
                                        .optional(),
                                parameterWithName("query")
                                        .description("검색할 키워드\n (date 파라미터 요청시 생략)").optional()
                        ),
                        responseFields(
                                fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지"),
                                fieldWithPath("currentSize").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지의 컨텐츠 개수"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN)
                                        .description("다음 페이지 존재 여부"),
                                subsectionWithPath("jobs").type("List<Job>")
                                        .description("알바 목록\n(없으면 [])")
                        ),
                        responseFields(
                                beneathPath("jobs").withSubsectionId("job"),
                                attributes(key("title").value("Job")),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("알바 고유 id값"),
                                fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                                        .description("알바 이미지").optional(),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("장소명"),
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("주소"),
                                fieldWithPath("workingDate").type(JsonFieldType.STRING)
                                        .description("근무 날짜 (기간)"),
                                fieldWithPath("workingHour").type(JsonFieldType.STRING)
                                        .description("근무 시간"),
                                fieldWithPath("salary").type(JsonFieldType.STRING)
                                        .description("급여"),
                                fieldWithPath("isClosed").type(JsonFieldType.BOOLEAN)
                                        .description("마감 여부")
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
        JobReadResponseDto jobReadResponseDto = JobReadResponseDto.builder()
                .id(1L)
                .name("알바2")
                .imgUrl(null)
                .address("제주 서귀포시")
                .workingDate("2021-09-21~2021-09-27")
                .workingHour("09:00~12:00")
                .salary("시급 10000원")
                .isClosed(false)
                .rep("064-0000-0000")
                .businessDescription("사업 내용입니다")
                .workingDay("월,금")
                .workingDescription("업무 내용입니다")
                .capacity(3)
                .deadline(LocalDate.now())
                .qualifications("지원 자격입니다")
                .comment("사장님의 한마디입니다")
                .build();

        given(jobService.read(1L))
                .willReturn(jobReadResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/jobs/{jobId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("job-read",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//요청 헤더
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "사용자 인증 수단, 액세스 토큰 값\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("jobId").description("알바 고유 id값")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("알바 고유 id값"),
                                fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                                        .description("알바 이미지").optional(),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("장소명"),
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("주소"),
                                fieldWithPath("workingDate").type(JsonFieldType.STRING)
                                        .description("근무 날짜 (기간)"),
                                fieldWithPath("workingHour").type(JsonFieldType.STRING)
                                        .description("근무시간"),
                                fieldWithPath("salary").type(JsonFieldType.STRING)
                                        .description("급여"),
                                fieldWithPath("isClosed").type(JsonFieldType.BOOLEAN)
                                        .description("마감 여부"),
                                fieldWithPath("rep").type(JsonFieldType.STRING)
                                        .description("대표 전화번호"),
                                fieldWithPath("businessDescription").type(JsonFieldType.STRING)
                                        .description("사업 내용"),
                                fieldWithPath("workingDay").type(JsonFieldType.STRING)
                                        .description("근무 요일"),
                                fieldWithPath("workingDescription").type(JsonFieldType.STRING)
                                        .description("업무 내용"),
                                fieldWithPath("capacity").type(JsonFieldType.NUMBER)
                                        .description("채용 인원"),
                                fieldWithPath("deadline").type(JsonFieldType.STRING)
                                        .attributes(getDateFormat())
                                        .description("마감일"),
                                fieldWithPath("qualifications").type(JsonFieldType.STRING)
                                        .description("지원 자격").optional(),
                                fieldWithPath("comment").type(JsonFieldType.STRING)
                                        .description("사장님의 한마디").optional()
                        )
                ));

    }
}