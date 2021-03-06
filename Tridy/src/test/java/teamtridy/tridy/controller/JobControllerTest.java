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
        //???????????? ??????

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        JobDto jobDto1 = JobDto.builder()
                .id(1L)
                .name("??????1")
                .imgUrl(null)
                .address("?????? ????????????")
                .workingDate("2021-09-21,2021-09-27")
                .workingHour("09:00~12:00")
                .salary("?????? 10000???")
                .isClosed(true)
                .build();

        JobDto jobDto2 = JobDto.builder()
                .id(1L)
                .name("??????2")
                .imgUrl(null)
                .address("?????? ????????????")
                .workingDate("2021-09-21~2021-09-27")
                .workingHour("09:00~12:00")
                .salary("?????? 10000???")
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
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        requestParameters(
                                parameterWithName("page")
                                        .description("?????? ?????????\n(1 ??????) (?????????: 1)").optional(),
                                parameterWithName("size")
                                        .description("?????? ????????? ??????\n(1 ?????? 30 ??????) (?????????: 10)")
                                        .optional(),
                                parameterWithName("date")
                                        .description(
                                                "????????? ?????? \n (query ???????????? ????????? ??????) \n (yyyy-MM-dd ??????)")
                                        .optional(),
                                parameterWithName("query")
                                        .description("????????? ?????????\n (date ???????????? ????????? ??????)").optional()
                        ),
                        responseFields(
                                fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                                        .description("?????? ?????????"),
                                fieldWithPath("currentSize").type(JsonFieldType.NUMBER)
                                        .description("?????? ???????????? ????????? ??????"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN)
                                        .description("?????? ????????? ?????? ??????"),
                                subsectionWithPath("jobs").type("List<Job>")
                                        .description("?????? ??????\n(????????? [])")
                        ),
                        responseFields(
                                beneathPath("jobs").withSubsectionId("job"),
                                attributes(key("title").value("Job")),
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("?????? ?????? id???"),
                                fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                                        .description("?????? ?????????").optional(),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("?????????"),
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("??????"),
                                fieldWithPath("workingDate").type(JsonFieldType.STRING)
                                        .description("?????? ?????? (??????)"),
                                fieldWithPath("workingHour").type(JsonFieldType.STRING)
                                        .description("?????? ??????"),
                                fieldWithPath("salary").type(JsonFieldType.STRING)
                                        .description("??????"),
                                fieldWithPath("isClosed").type(JsonFieldType.BOOLEAN)
                                        .description("?????? ??????")
                        )
                ));
    }

    @Test
    void read() throws Exception {
        // ???????????? ??????
        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        JobReadResponseDto jobReadResponseDto = JobReadResponseDto.builder()
                .id(1L)
                .name("??????2")
                .imgUrl(null)
                .address("?????? ????????????")
                .workingDate("2021-09-21~2021-09-27")
                .workingHour("09:00~12:00")
                .salary("?????? 10000???")
                .isClosed(false)
                .rep("064-0000-0000")
                .businessDescription("?????? ???????????????")
                .workingDay("???,???")
                .workingDescription("?????? ???????????????")
                .capacity(3)
                .deadline(LocalDate.now())
                .qualifications("?????? ???????????????")
                .comment("???????????? ??????????????????")
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
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("jobId").description("?????? ?????? id???")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("?????? ?????? id???"),
                                fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                                        .description("?????? ?????????").optional(),
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("?????????"),
                                fieldWithPath("address").type(JsonFieldType.STRING)
                                        .description("??????"),
                                fieldWithPath("workingDate").type(JsonFieldType.STRING)
                                        .description("?????? ?????? (??????)"),
                                fieldWithPath("workingHour").type(JsonFieldType.STRING)
                                        .description("????????????"),
                                fieldWithPath("salary").type(JsonFieldType.STRING)
                                        .description("??????"),
                                fieldWithPath("isClosed").type(JsonFieldType.BOOLEAN)
                                        .description("?????? ??????"),
                                fieldWithPath("rep").type(JsonFieldType.STRING)
                                        .description("?????? ????????????"),
                                fieldWithPath("businessDescription").type(JsonFieldType.STRING)
                                        .description("?????? ??????"),
                                fieldWithPath("workingDay").type(JsonFieldType.STRING)
                                        .description("?????? ??????"),
                                fieldWithPath("workingDescription").type(JsonFieldType.STRING)
                                        .description("?????? ??????"),
                                fieldWithPath("capacity").type(JsonFieldType.NUMBER)
                                        .description("?????? ??????"),
                                fieldWithPath("deadline").type(JsonFieldType.STRING)
                                        .attributes(getDateFormat())
                                        .description("?????????"),
                                fieldWithPath("qualifications").type(JsonFieldType.STRING)
                                        .description("?????? ??????").optional(),
                                fieldWithPath("comment").type(JsonFieldType.STRING)
                                        .description("???????????? ?????????").optional()
                        )
                ));

    }
}