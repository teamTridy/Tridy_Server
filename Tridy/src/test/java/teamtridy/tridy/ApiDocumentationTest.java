package teamtridy.tridy;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static teamtridy.tridy.util.DocumentFormatGenerator.getDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import teamtridy.tridy.config.TokenProvider;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.service.AccountService;
import teamtridy.tridy.service.AppleService;
import teamtridy.tridy.service.CategoryService;
import teamtridy.tridy.service.GoogleService;
import teamtridy.tridy.service.JobService;
import teamtridy.tridy.service.KakaoService;
import teamtridy.tridy.service.OpenWeatherService;
import teamtridy.tridy.service.PlaceService;
import teamtridy.tridy.service.RecommendService;

// https://velog.io/@hydroniumion/BE2%EC%A3%BC%EC%B0%A8-Spring-Rest-Docs-%EC%A0%81%EC%9A%A9%EA%B8%B0-2 드디어 작동하는 참고 문서!
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureRestDocs // (1)
@SpringBootTest
public abstract class ApiDocumentationTest {

    protected static final String BEARER_TYPE = "bearer";
    protected static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 365; // 1년

    protected FieldDescriptor[] placeFields = new FieldDescriptor[]{
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
            fieldWithPath("isPicked").type(JsonFieldType.BOOLEAN)
                    .description("장소 찜 여부")

    };

    protected FieldDescriptor[] recommendFields = new FieldDescriptor[]{
            fieldWithPath("id").type(JsonFieldType.NUMBER)
                    .description("장소 고유 id값"),
            fieldWithPath("name").type(JsonFieldType.STRING)
                    .description("장소 이름"),
            fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                    .description("장소 이미지").optional(),
            fieldWithPath("hashtags").type("List<String>")
                    .description("장소 해시태그 리스트"),
            fieldWithPath("isPicked").type(JsonFieldType.BOOLEAN)
                    .description("장소 찜 여부"),
            fieldWithPath("congestion").type(JsonFieldType.NUMBER)
                    .description(
                            "장소 혼잡도 레벨 (1:쾌적, 2:여유, 3:보통, 4:약간혼잡, 5:혼잡) \n (메인 추천 장소, 관심활동 추천 장소는 없으면 null) (연관 추천 장소는 무조건 null로 표시))").optional(),
            fieldWithPath("distance").type(JsonFieldType.NUMBER)
                    .description(
                            "장소와의 거리 (메인 추천 장소: 추천 당시 위치와의 거리, 연관 추천 장소: 메인추천 장소 위치와의 거리)\n  (메인 추천 장소, 연관 추천 장소는 필수값) (관심활동 추천 장소는 무조건 null로 표시))").optional(),
    };

    protected FieldDescriptor[] mainRecommendFields = {
            fieldWithPath("id").type(JsonFieldType.NUMBER)
                    .description("장소 고유 id값"),
            fieldWithPath("name").type(JsonFieldType.STRING)
                    .description("장소 이름"),
            fieldWithPath("imgUrl").type(JsonFieldType.STRING)
                    .description("장소 이미지").optional(),
            fieldWithPath("hashtags").type("List<String>")
                    .description("장소 해시태그 리스트"),
            fieldWithPath("isPicked").type(JsonFieldType.BOOLEAN)
                    .description("장소 찜 여부"),
            fieldWithPath("congestion").type(JsonFieldType.NUMBER)
                    .description(
                            "장소 혼잡도 레벨 (1:쾌적, 2:여유, 3:보통, 4:약간혼잡, 5:혼잡) \n (메인 추천 장소, 관심활동 추천 장소는 없으면 null) (연관 추천 장소는 무조건 null로 표시))").optional(),
            fieldWithPath("distance").type(JsonFieldType.NUMBER)
                    .description(
                            "장소와의 거리 (메인 추천 장소: 추천 당시 위치와의 거리, 연관 추천 장소: 메인추천 장소 위치와의 거리)\n  (메인 추천 장소, 연관 추천 장소는 필수값) (관심활동 추천 장소는 무조건 null로 표시))").optional(),
            subsectionWithPath("relateds").type("List<RecommendPlace>")
                    .description("연관 추천 장소 목록 (추천 장소 정보 구조는 동일)")

    };


    protected FieldDescriptor[] reviewRequestFields = new FieldDescriptor[]{
            fieldWithPath("comment").type(JsonFieldType.STRING)
                    .description("리뷰 내용"),
            fieldWithPath("rating").type(JsonFieldType.NUMBER)
                    .description("리뷰 평점 (1 이상 5 이하)"),
            fieldWithPath("isPrivate").type(JsonFieldType.BOOLEAN)
                    .description("리뷰 비공개 여부 (타임라인에만 노출 여부)")
    };

    protected FieldDescriptor[] placeReviewResponseFields = new FieldDescriptor[]{
            fieldWithPath("id").type(JsonFieldType.NUMBER)
                    .description("리뷰 고유 id값"),
            fieldWithPath("comment").type(JsonFieldType.STRING)
                    .description("리뷰 내용"),
            fieldWithPath("authorNickname").type(JsonFieldType.STRING)
                    .description("리뷰 작성자 닉네임"),
            fieldWithPath("rating").type(JsonFieldType.NUMBER)
                    .description("리뷰 평점 (1 이상 5 이하)"),
            fieldWithPath("createdAt").attributes(getDateFormat())
                    .type(JsonFieldType.STRING)
                    .description("리뷰 작성일"),
            fieldWithPath("isAuthor").type(JsonFieldType.BOOLEAN)
                    .description("리뷰 작성자 여부 (삭제/수정 가능 여부)")
    };

    protected FieldDescriptor[] accountReviewResponseFields = new FieldDescriptor[]{
            fieldWithPath("id").type(JsonFieldType.NUMBER)
                    .description("리뷰 고유 id값"),
            fieldWithPath("placeId").type(JsonFieldType.NUMBER)
                    .description("장소 고유 id값"),
            fieldWithPath("placeName").type(JsonFieldType.STRING)
                    .description("장소 이름"),
            fieldWithPath("comment").type(JsonFieldType.STRING)
                    .description("리뷰 내용"),
            fieldWithPath("rating").type(JsonFieldType.NUMBER)
                    .description("리뷰 평점 (1 이상 5 이하)"),
            fieldWithPath("createdAt").attributes(getDateFormat())
                    .type(JsonFieldType.STRING)
                    .description("리뷰 작성일"),
            fieldWithPath("isPrivate").type(JsonFieldType.BOOLEAN).description(
                    "리뷰 비공개 여부 (타임라인에만 표시 여부)"),
    };

    protected FieldDescriptor[] tendencyFields = {
            fieldWithPath("isPreferredFar").type(JsonFieldType.BOOLEAN)
                    .description("범위 먼 곳(~30Km) 선호 여부"),
            fieldWithPath("isPreferredPopular").type(JsonFieldType.BOOLEAN)
                    .description("인기 많은 곳 선호 여부"),
            fieldWithPath("interestIds").type("List<Number>")
                    .description(
                    "관심활동 id값 \n(3개 이상 5개 이하)\n(1:경관포토, 2:휴식힐링, 3:테마공원, 4:역사유적지, 5:문화예술, 6:액티비티, 7:체험관광, 8:쇼핑, 9:걷기등산)")
    };

    protected FieldDescriptor[] accountFields = new FieldDescriptor[]{
            fieldWithPath("id").type(JsonFieldType.NUMBER)
                    .description("계정 고유 id값"),
            fieldWithPath("nickname").type(JsonFieldType.STRING)
                    .description("닉네임 \n(2자 이상 10자 이내의 한글,영어,숫자)"),
            subsectionWithPath("tendency").type("Tendency")
                    .description("계정 성향 정보 (없으면 null)").optional()
    };

    protected FieldDescriptor[] tokenFields = new FieldDescriptor[]{
            fieldWithPath("tokenType").type(JsonFieldType.STRING)
                    .description("토큰 타입"),
            fieldWithPath("accessToken").type(JsonFieldType.STRING)
                    .description("사용자 인증 수단, 액세스 토큰"),
            fieldWithPath("accessTokenExpiresIn").type(JsonFieldType.STRING)
                    .attributes(getDateFormat())
                    .description("토큰 만료일"),
            fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                    .description("리프레쉬 토큰")
    };


    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    protected OpenWeatherService openWeatherService;
    @MockBean
    protected KakaoService kakaoService;
    @MockBean
    protected RecommendService recommendService;

    @MockBean
    protected TokenProvider tokenProvider;
    @MockBean
    protected AccountService accountService;
    @MockBean
    protected PlaceService placeService;

    protected Account account;
    protected Authentication authentication;

    @MockBean
    protected CategoryService categoryService;

    @MockBean
    protected GoogleService googleService;

    @MockBean
    protected AppleService appleService;

    @MockBean
    protected JobService jobService;


    @BeforeEach // 테스트 클래스 테스트 시작시 1번만호출
    public void setUp(WebApplicationContext webApplicationContext,
            RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation).snippets())
                .build();

        Account account = Account.builder().id(1L).socialId("{socialId}").build();
        this.account = account;

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(new String[]{"ROLE_USER"})
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User("{socialId}", "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "",
                authorities);
        this.authentication = authentication;
    }

}