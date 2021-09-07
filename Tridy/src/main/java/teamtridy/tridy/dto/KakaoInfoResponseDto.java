package teamtridy.tridy.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)  //이 클래스에 정의되지 않은 필드가 있을 경우에 무시하겠다는 뜻
public class KakaoInfoResponseDto {

    @JsonProperty(value = "id")
    //POJO-JSON 상호변환시 정확한 멤버 변수에 맵핑해주는 역할을 한다. Jackson이 제공하는 기능으로 Gson을 사용한다면 Gson이 제공하는 기능을 사용해야 한다.
    private Long id;

    @JsonProperty(value = "expires_in")
    private Integer expiresIn;

    @JsonProperty(value = "app_id")
    private Integer appId;
}
