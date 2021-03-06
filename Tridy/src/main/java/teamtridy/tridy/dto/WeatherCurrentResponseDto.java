package teamtridy.tridy.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherCurrentResponseDto {

    private LocalDateTime time;
    private String address;
    private Integer temp;
    private String description;
}
