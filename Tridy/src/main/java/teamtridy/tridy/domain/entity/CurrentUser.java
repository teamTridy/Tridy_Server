package teamtridy.tridy.domain.entity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "@accountService.getCurrentAccount()")
//인증된 사용자의 Principal 정보를 참조할 수 있다. 여기서 말하는 Principal은 우리가 인증할 때 Authentication 에 들어있는 첫번째 파라미터 이다.
public @interface CurrentUser {

}
