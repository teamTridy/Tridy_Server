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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static teamtridy.tridy.util.ApiDocumentUtils.getDocumentRequest;
import static teamtridy.tridy.util.ApiDocumentUtils.getDocumentResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import teamtridy.tridy.ApiDocumentationTest;
import teamtridy.tridy.dto.AccountReviewReadAllResponseDto;
import teamtridy.tridy.dto.PickReadAllResponseDto;
import teamtridy.tridy.dto.SigninEmailRequestDto;
import teamtridy.tridy.dto.SigninRequestDto;
import teamtridy.tridy.dto.SigninResponseDto;
import teamtridy.tridy.dto.SignupEmailRequestDto;
import teamtridy.tridy.dto.SignupRequestDto;
import teamtridy.tridy.dto.TendencyUpdateRequestDto;
import teamtridy.tridy.dto.TokenDto;
import teamtridy.tridy.service.dto.AccountDto;
import teamtridy.tridy.service.dto.AccountReviewDto;
import teamtridy.tridy.service.dto.PlaceDto;
import teamtridy.tridy.service.dto.SignupDto;
import teamtridy.tridy.service.dto.TendencyDto;

public class AccountControllerTest extends ApiDocumentationTest {

    private AccountDto getAccountDto() {
        return AccountDto.builder().id(1L).nickname("?????????").tendency(null)
                .build();
    }

    private AccountDto getAccountDto(TendencyDto tendencyDto) {
        return AccountDto.builder().id(1L).nickname("?????????").tendency(tendencyDto)
                .build();
    }

    private TokenDto getTokenDto() {
        return TokenDto.builder()
                .tokenType(BEARER_TYPE)
                .accessToken("{accessToken}")
                .accessTokenExpiresIn(new Date((new Date()).getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .refreshToken("{refreshToken}")
                .build();
    }

    private TendencyDto getTendencyDto() {
        List<Long> interestIds = Arrays.asList(1L, 4L, 5L, 6L);
        return TendencyDto.builder().isPreferredFar(true)
                .isPreferredPopular(false).interestIds(interestIds).build();
    }

    @Test
    void testIsDuplicatedNickname() throws Exception {
        //given
        String nickname = "?????????";

        given(accountService.isDuplicatedNickname(any(String.class)))
                .willReturn(false);

        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/duplicate/nickname")
                        .param("nickname", nickname)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isNoContent())
                .andDo(document("account-duplicate-nickname", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("nickname")
                                        .description("????????? \n(2??? ?????? 10??? ????????? ??????,??????,??????)")
                        )
                ));
    }

    @Test
    void testIsDuplicatedEmail() throws Exception {
        //given
        String email = "tridy@gmail.com";

        given(accountService.isDuplicatedEmail(any(String.class)))
                .willReturn(false);

        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/duplicate/email")
                        .param("email", email)
                        .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isNoContent())
                .andDo(document("account-duplicate-email", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("email")
                                        .description("????????? \n(????????? ??????)")
                        )
                ));
    }

    @Test
    void testSignin() throws Exception {
        //given
        TendencyDto tendencyDto = getTendencyDto();
        AccountDto accountDto = getAccountDto(tendencyDto);
        TokenDto tokenDto = getTokenDto();

        SigninResponseDto response = SigninResponseDto.builder()
                .account(accountDto)
                .token(tokenDto)
                .build();

        given(googleService.getSocialId(any(String.class)))
                .willReturn("{socialId}");

        given(accountService
                .signin(any(String.class), any(String.class))) // accountService.signin(socialId)
                .willReturn(response);

        //when
        SigninRequestDto signinRequestDto = SigninRequestDto.builder().socialType("google")
                .socialToken("{socialToken}").build();

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/accounts/signin")
                        .content(objectMapper.writeValueAsString(signinRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("account-signin", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("socialType").type(JsonFieldType.STRING)
                                        .description("?????? ??????\n(google/kakao/apple)"),
                                fieldWithPath("socialToken").type(JsonFieldType.STRING)
                                        .description("?????? ?????????")
                        ),
                        responseFields(
                                subsectionWithPath("account").type("Account").description("?????? ??????"),
                                subsectionWithPath("token").type("Token").description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("account").withSubsectionId("account"),
                                attributes(key("title").value("Account")),
                                accountFields
                        ),
                        responseFields(
                                beneathPath("account.tendency").withSubsectionId("tendency"),
                                attributes(key("title").value("Tendency")),
                                tendencyFields
                        ),
                        responseFields(
                                beneathPath("token").withSubsectionId("token"),
                                attributes(key("title").value("Token")),
                                tokenFields
                        )

                ));
    }

    @Test
    void testSigninEmail() throws Exception {
        //given
        TendencyDto tendencyDto = getTendencyDto();
        AccountDto accountDto = getAccountDto(tendencyDto);
        TokenDto tokenDto = getTokenDto();

        SigninResponseDto response = SigninResponseDto.builder()
                .account(accountDto)
                .token(tokenDto)
                .build();

        given(accountService
                .signin(any(String.class), any(String.class))) // accountService.signin(socialId)
                .willReturn(response);

        //when
        SigninEmailRequestDto signinEmailRequestDto = SigninEmailRequestDto.builder()
                .email("tridy@gmail.com")
                .password("tridy123!").build();

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/accounts/signin/email")
                        .content(objectMapper.writeValueAsString(signinEmailRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("account-signin-email", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("?????????\n(????????? ??????)"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description(
                                                "????????????\n(????????????,??????,?????? ????????????(@#$%^&+=-_!)?????? 6?????? ?????? 12?????? ??????)")
                        ),
                        responseFields(
                                subsectionWithPath("account").type("Account").description("?????? ??????"),
                                subsectionWithPath("token").type("Token").description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("account").withSubsectionId("account"),
                                attributes(key("title").value("Account")),
                                accountFields
                        ),
                        responseFields(
                                beneathPath("account.tendency").withSubsectionId("tendency"),
                                attributes(key("title").value("Tendency")),
                                tendencyFields
                        ),
                        responseFields(
                                beneathPath("token").withSubsectionId("token"),
                                attributes(key("title").value("Token")),
                                tokenFields
                        )

                ));
    }

    @Test
    void testSignup() throws Exception {
        //given
        AccountDto accountDto = getAccountDto();
        TokenDto tokenDto = getTokenDto();

        SigninResponseDto response = SigninResponseDto.builder()
                .account(accountDto)
                .token(tokenDto)
                .build();

        given(googleService.getSocialId(any(String.class)))
                .willReturn("1234");

        willDoNothing().given(accountService)
                .signup(any(SignupDto.class)); // accountService.signin(socialId)

        given(accountService
                .signin(any(String.class), any(String.class))) // accountService.signin(socialId)
                .willReturn(response);

        //when
        SignupRequestDto signupRequestDto = SignupRequestDto.builder().socialType("google")
                .socialToken("{socialToken}").nickname("tridy").build();

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/accounts/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto))
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(document("account-signup", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("socialType").type(JsonFieldType.STRING)
                                        .description("?????? ??????\n(google/kakao/apple)"),
                                fieldWithPath("socialToken").type(JsonFieldType.STRING)
                                        .description("?????? ?????????"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("????????? \n(2??? ?????? 10??? ????????? ??????,??????,??????)")
                        ),
                        responseFields(
                                subsectionWithPath("account").type("Account").description("?????? ??????"),
                                subsectionWithPath("token").type("Token").description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("account").withSubsectionId("account"),
                                attributes(key("title").value("Account")),
                                accountFields
                        ),
                        responseFields(
                                beneathPath("token").withSubsectionId("token"),
                                attributes(key("title").value("Token")),
                                tokenFields
                        )

                ));
    }

    @Test
    void testSignupEmail() throws Exception {
        //given
        AccountDto accountDto = getAccountDto();
        TokenDto tokenDto = getTokenDto();

        SigninResponseDto response = SigninResponseDto.builder()
                .account(accountDto)
                .token(tokenDto)
                .build();

        willDoNothing().given(accountService)
                .signup(any(SignupDto.class)); // accountService.signin(socialId)

        given(accountService
                .signin(any(String.class), any(String.class))) // accountService.signin(socialId)
                .willReturn(response);

        //when
        SignupEmailRequestDto signupEmailRequestDto = SignupEmailRequestDto.builder()
                .email("tridy@gmail.com")
                .password("tridy123!").nickname("tridy").build();

        ResultActions result = this.mockMvc.perform(
                post("/api/v1/accounts/signup/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupEmailRequestDto))
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(document("account-signup-email", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("?????????\n(????????? ??????)"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .description(
                                                "????????????\n(????????????,??????,?????? ????????????(@#$%^&+=-_!)?????? 6?????? ?????? 12?????? ??????)"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .description("????????? \n(2??? ?????? 10??? ????????? ??????,??????,??????)")
                        ),
                        responseFields(
                                subsectionWithPath("account").type("Account").description("?????? ??????"),
                                subsectionWithPath("token").type("Token").description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("account").withSubsectionId("account"),
                                attributes(key("title").value("Account")),
                                accountFields
                        ),
                        responseFields(
                                beneathPath("token").withSubsectionId("token"),
                                attributes(key("title").value("Token")),
                                tokenFields
                        )

                ));
    }

    @Test
    void testRead() throws Exception {
        //???????????? ??????

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        TendencyDto tendencyDto = getTendencyDto();
        AccountDto response = getAccountDto(tendencyDto);

        given(accountService.read(account, 1L))
                .willReturn(response);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/{accountId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("account-read", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("?????? ?????? id???")
                        ),
                        responseFields(
                                accountFields
                        ),
                        responseFields(
                                beneathPath("tendency").withSubsectionId("tendency"),
                                attributes(key("title").value("Tendency")),
                                tendencyFields
                        )
                ));
    }


    @Test
    void testUpdateTendency() throws Exception {
        //???????????? ??????

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        TendencyDto tendencyDto = getTendencyDto();
        AccountDto response = getAccountDto(tendencyDto);

        given(accountService
                .updateTendency(eq(account), eq(1L), any(TendencyUpdateRequestDto.class)))
                .willReturn(response);

        //when
        List<Long> interestIds = Arrays.asList(1L, 4L, 5L, 6L);
        TendencyUpdateRequestDto tendencyUpdateRequestDto = new TendencyUpdateRequestDto();
        tendencyUpdateRequestDto.setIsPreferredFar(true);
        tendencyUpdateRequestDto.setIsPreferredPopular(false);
        tendencyUpdateRequestDto.setInterestIds(interestIds);

        ResultActions result = this.mockMvc.perform(
                patch("/api/v1/accounts/{accountId}/tendency", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .content(objectMapper.writeValueAsString(tendencyUpdateRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("account-tendency-update", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("?????? ?????? id???")
                        ),
                        requestFields(tendencyFields),
                        responseFields(
                                accountFields
                        ),
                        responseFields(
                                beneathPath("tendency").withSubsectionId("tendency"),
                                attributes(key("title").value("Tendency")),
                                tendencyFields
                        )
                ));
    }

    @Test
    void testDelete() throws Exception {
        //???????????? ??????

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given

        willDoNothing().given(accountService).delete(account, 1L);

        //when
        ResultActions result = this.mockMvc.perform(
                delete("/api/v1/accounts/{accountId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent())
                .andDo(document("account-delete", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("?????? ?????? id???")
                        )
                ));
    }

    @Test
    void testReadAllPick() throws Exception {
        //???????????? ??????

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        List<String> hashtags1 = Arrays.asList("????????????", "????????????", "??????", "??????");
        List<String> hashtags2 = Arrays.asList("??????", "?????????", "?????????");

        PlaceDto placeDto1 = PlaceDto.builder().id(1L).address("????????????????????? ???????????? ????????? ????????? 284-12")
                .name("??????????????? [???????????? ??????????????????]")
                .hashtags(hashtags1)
                .imgUrl("http://tong.visitkorea.or.kr/cms/resource/85/1876185_image3_1.jpg")
                .isPicked(true).build();

        PlaceDto placeDto2 = PlaceDto.builder().id(5L).address("????????????????????? ????????? ????????? ????????? 44")
                .name("???????????????")
                .hashtags(hashtags2)
                .imgUrl(
                        "https://api.cdn.visitjeju.net/photomng/thumbnailpath/201804/30/7f6c7979-90ee-41fe-b353-613b8e18c425.jpg")
                .isPicked(true).build();

        List<PlaceDto> placeDtos = Arrays.asList(placeDto1, placeDto2);

        PickReadAllResponseDto pickReadAllResponseDto = PickReadAllResponseDto.builder()
                .currentPage(1).currentSize(2).hasNextPage(false).places(placeDtos).build();

        given(accountService.readAllPick(account, 1L, 1, 10))
                .willReturn(pickReadAllResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/{accountId}/picks", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("account-pick-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("?????? ?????? id???")
                        ),
                        requestParameters(
                                parameterWithName("page")
                                        .description("?????? ?????????\n(1 ??????)\n(?????????: 1)").optional(),
                                parameterWithName("size")
                                        .description("?????? ????????? ??????\n(1 ?????? 30 ??????) (?????????: 10)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                                        .description("?????? ?????????"),
                                fieldWithPath("currentSize").type(JsonFieldType.NUMBER)
                                        .description("?????? ???????????? ????????? ??????"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN)
                                        .description("?????? ????????? ?????? ??????"),
                                subsectionWithPath("places").type("List<Place>")
                                        .description("?????? ??????\n(????????? [])")
                        ),
                        responseFields(
                                beneathPath("places").withSubsectionId("place"),
                                attributes(key("title").value("Place")),
                                placeFields
                        )
                ));
    }

    @Test
    void testReadAllReview() throws Exception {
        // ???????????? ??????

        given(tokenProvider.validateToken(any(String.class)))
                .willReturn(true);

        given(tokenProvider.getAuthentication(any(String.class)))
                .willReturn(this.authentication);

        given(accountService.getCurrentAccount())
                .willReturn(this.account);

        //given
        LocalDate date = LocalDate.of(2021, 9, 1);

        AccountReviewDto accountReviewDto1 = AccountReviewDto.builder().id(29L).placeId(1L)
                .placeName("????????????")
                .comment("?????? ?????? ????????????!")
                .isPrivate(false).rating(5).createdAt(date.minusDays(5))
                .build();

        AccountReviewDto accountReviewDto2 = AccountReviewDto.builder().id(30L)
                .placeId(2L)
                .placeName("????????????")
                .isPrivate(true)
                .comment("????????? ?????? ?????????.")
                .rating(3)
                .createdAt(date)
                .build();

        List<AccountReviewDto> accountReviewDtos = Arrays
                .asList(accountReviewDto1, accountReviewDto2);

        AccountReviewReadAllResponseDto accountReviewReadAllResponseDto = AccountReviewReadAllResponseDto
                .builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .currentPage(1).currentSize(2).hasNextPage(false).reviews(accountReviewDtos)
                .build();

        given(accountService
                .readAllReviewByYearAndMonth(account, 1L, date.getYear(), date.getMonthValue(), 1,
                        10))
                .willReturn(accountReviewReadAllResponseDto);

        //when
        ResultActions result = this.mockMvc.perform(
                get("/api/v1/accounts/{accountId}/reviews", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer {accessToken}")
                        .queryParam("year", String.valueOf(date.getYear()))
                        .queryParam("month", String.valueOf(date.getMonthValue()))
                        .queryParam("page", "1")
                        .queryParam("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andDo(document("account-review-read-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestHeaders(//?????? ??????
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description(
                                                "????????? ?????? ??????, ????????? ?????? ???\nAuthorization: Bearer {ACCESS_TOKEN}")
                        ),
                        pathParameters(
                                parameterWithName("accountId").description("?????? ?????? id???")
                        ),
                        requestParameters(
                                parameterWithName("year")
                                        .description("????????? ??????"),
                                parameterWithName("month")
                                        .description("????????? ???").optional(),
                                parameterWithName("page")
                                        .description("?????? ?????????\n(1 ??????) (?????????: 1)").optional(),
                                parameterWithName("size")
                                        .description("?????? ????????? ??????\n(1 ?????? 30 ??????) (?????????: 10)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("year")
                                        .description("????????? ??????"),
                                fieldWithPath("month")
                                        .description("????????? ???"),
                                fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                                        .description("?????? ?????????"),
                                fieldWithPath("currentSize").type(JsonFieldType.NUMBER)
                                        .description("?????? ???????????? ????????? ??????"),
                                fieldWithPath("hasNextPage").type(JsonFieldType.BOOLEAN)
                                        .description("?????? ????????? ?????? ??????"),
                                subsectionWithPath("reviews").type("List<AccountReview>")
                                        .description("?????? ?????? ??????\n((????????? [])")
                        ),
                        responseFields(
                                beneathPath("reviews").withSubsectionId("accountReview"),
                                attributes(key("title").value("AccountReview")),
                                accountReviewResponseFields
                        )
                ));
    }


}