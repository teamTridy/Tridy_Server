package teamtridy.tridy.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    INVALID_AUTHORIZED(UNAUTHORIZED, "자격 증명이 유효하지 않습니다."),
    EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다."),
    EXPIRED_SOCIAL_TOKEN(UNAUTHORIZED, "만료된 소셜 토큰입니다."),
    INVALID_TOKEN(UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_SOCIAL_TOKEN(UNAUTHORIZED, "유효하지 않은 소셜 토큰입니다."),


    /* 403 FORBIDDEN : 권한이 없는 사용자 */
    //클라이언트가 허용된 범위 외의 리소스에 접근하려고 할 때 REST API는 403으로 응답해야 한다. https://onecellboy.tistory.com/347
    ACCESS_DENIED(FORBIDDEN,
            "해당 리소스에 대한 권한이 없습니다."),

    /* 400 BAD_REQUEST : 요청값 검증 실패 */
    INVALID_INPUT_VALUE(BAD_REQUEST, "요청 값이 유효하지 않습니다."),

    /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
    ACCOUNT_NOT_FOUND(NOT_FOUND, "존재하지 않는 유저입니다."),
    CATEGORY_NOT_FOUND(NOT_FOUND, "존재하지 않는 카테고리입니다."),
    PLACE_NOT_FOUND(NOT_FOUND, "존재하지 않는 장소입니다."),
    INTEREST_NOT_FOUND(NOT_FOUND, "존재하지 않는 관심활동입니다."),
    REGION_NOT_FOUND(NOT_FOUND, "존재하지 않는 지역입니다."),
    REVIEW_NOT_FOUND(NOT_FOUND, "존재하지 않는 리뷰입니다."),
    PICK_NOT_FOUND(NOT_FOUND, "찜하지 않은 장소입니다."),
    SORT_NOT_FOUND(NOT_FOUND, "존재하지 않는 정렬 기준입니다"),
    JOB_NOT_FOUND(NOT_FOUND, "존재하지 않는 아르바이트입니다."),

    /* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
    ACCOUNT_DUPLICATION(CONFLICT, "이미 가입된 계정입니다"),
    NICKNAME_DUPLICATION(CONFLICT, "이미 사용 중인 닉네임입니다"),
    PICK_DUPLICATION(CONFLICT, "이미 찜한 장소입니다"),
    REVIEW_DUPLICATION(CONFLICT, "이미 좋아요한 글입니다"),
    EMAIL_DUPLICATION(CONFLICT, "이미 사용 중인 이메일입니다"),

    /* 500 */
    INTER_SERVER_ERROR(INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    EXTERNAL_SERVER_ERROR(INTERNAL_SERVER_ERROR, "외부 API 호출 오류입니다.");


    private final HttpStatus status;
    private final String message;
}