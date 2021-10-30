package teamtridy.tridy.aop;

import lombok.Data;

@Data
public class RequestLog {

    String type;
    String timestamp;
    String clientIp;
    String clientUrl;
    String callFunction;
    String parameter;
}