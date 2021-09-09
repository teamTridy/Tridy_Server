package teamtridy.tridy.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

// 왜-Inner-class에-Static을-붙이는거지
// https://velog.io/@agugu95/%EC%99%9C-Inner-class%EC%97%90-Static%EC%9D%84-%EB%B6%99%EC%9D%B4%EB%8A%94%EA%B1%B0%EC%A7%80
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAddressResponseDto {

    private Meta meta;
    private List<Document> documents;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {

        @JsonProperty(value = "total_count")
        private Integer totalCount;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {

        private Address address;
        private RoadAddress roadAddress;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Address {

            @JsonProperty(value = "region_1depth_name")
            private String region1depthName;
            @JsonProperty(value = "region_2depth_name")
            private String region2depthName;
            @JsonProperty(value = "region_3depth_name")
            private String region3depthName;
        }

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RoadAddress {

            @JsonProperty(value = "region_1depth_name")
            private String region1depthName;
            @JsonProperty(value = "region_2depth_name")
            private String region2depthName;
            @JsonProperty(value = "region_3depth_name")
            private String region3depthName;
        }
    }
}
