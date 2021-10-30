package teamtridy.tridy.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component // 1
@Aspect // 2
public class RequestLoggingAspect {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);
    private ObjectMapper mapper = new ObjectMapper();

    private String clientIp = "";
    private String clientUrl = "";

    @PostConstruct
    public void init() throws UnknownHostException {
        mapper.findAndRegisterModules();
    }

    @Pointcut("within(teamtridy.tridy.controller..*)") // 3
    public void onRequest() {
    }

    @Around("teamtridy.tridy.aop.RequestLoggingAspect.onRequest()") // 4
    public Object controllerAroundLogging(ProceedingJoinPoint pjp) throws Throwable {
        String timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT)
                .format(new Timestamp(System.currentTimeMillis()));
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        this.clientIp = request.getRemoteAddr();
        this.clientUrl = request.getRequestURL().toString();
        String callFunction =
                pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName();

        RequestLog requestLog = new RequestLog();
        requestLog.setTimestamp(timeStamp);
        requestLog.setClientIp(clientIp);
        requestLog.setClientUrl(clientUrl);
        requestLog.setCallFunction(callFunction);
        requestLog.setType("[REQUEST]");
        requestLog.setParameter(mapper.writeValueAsString(request.getParameterMap()));
        log.info("{}", mapper.writeValueAsString(requestLog));

        Object result = pjp.proceed();

        timeStamp = new SimpleDateFormat(TIMESTAMP_FORMAT)
                .format(new Timestamp(System.currentTimeMillis()));

        requestLog.setTimestamp(timeStamp);
        requestLog.setType("[RESPONSE]");
        requestLog.setParameter(mapper.writeValueAsString(result));
        log.info("{}", mapper.writeValueAsString(requestLog));

        return result;
    }
}