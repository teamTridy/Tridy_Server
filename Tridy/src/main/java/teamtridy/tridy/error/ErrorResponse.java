package teamtridy.tridy.error;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private LocalDateTime timestamp;
    private Integer status;
    private String message;
    private List<FieldError> details;

    public static ErrorResponse of(final ErrorCode code) {

        return ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(code.getStatus().value())
                .message(code.getMessage()).build();
    }

    public static ErrorResponse of(Integer code, String message) {
        return ErrorResponse.builder().timestamp(LocalDateTime.now()).status(code)
                .message(message).build();
    }

    public static ErrorResponse of(final ErrorCode code, BindingResult bindingResult) {
        List<FieldError> errors = new ArrayList<>();

        for (org.springframework.validation.FieldError fieldError : bindingResult
                .getFieldErrors()) {
            errors.add(FieldError.of(fieldError));
        }

        return ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(code.getStatus().value())
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