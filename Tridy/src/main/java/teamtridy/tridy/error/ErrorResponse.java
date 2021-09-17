package teamtridy.tridy.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private Date timestamp;
    private Integer code;
    private String message;
    private List<FieldError> details;

    public static ErrorResponse of(final ErrorCode code) {

        return ErrorResponse.builder().timestamp(new Date()).code(code.getStatus().value())
                .message(code.getMessage()).build();
    }

    public static ErrorResponse of(Integer code, String message) {
        return ErrorResponse.builder().timestamp(new Date()).code(code)
                .message(message).build();
    }

    public static ErrorResponse of(final ErrorCode code, BindingResult bindingResult) {
        List<FieldError> errors = new ArrayList<>();

        for (org.springframework.validation.FieldError fieldError : bindingResult
                .getFieldErrors()) {
            errors.add(FieldError.of(fieldError));
        }

        return ErrorResponse.builder().timestamp(new Date()).code(code.getStatus().value())
                .message(code.getMessage()).details(errors).build();
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {

        private String field;
        private String value;
        private String reason;

        public static FieldError of(org.springframework.validation.FieldError fieldError) {
            return FieldError.builder().field(fieldError.getField())
                    .value(ObjectUtils.nullSafeToString(fieldError.getRejectedValue()))
                    .reason(fieldError.getDefaultMessage())
                    .build();
        }
    }
}