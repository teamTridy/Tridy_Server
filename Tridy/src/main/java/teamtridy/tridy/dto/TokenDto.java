package teamtridy.tridy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenDto {

    private String tokenType;
    private String accessToken;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date accessTokenExpiresIn;
    private String refreshToken;

    @Override
    public String toString() {
        return "Token";
    }
}
