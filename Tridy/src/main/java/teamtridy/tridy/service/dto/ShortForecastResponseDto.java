package teamtridy.tridy.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShortForecastResponseDto {

    @JsonProperty("response")
    private Response response;

    @Getter
    @Setter
    public static class Response {

        @JsonProperty("body")
        private Body body;
        @JsonProperty("header")
        private Header header;
    }

    @Getter
    @Setter
    public static class Body {

        @JsonProperty("totalCount")
        private Integer totalCount;
        @JsonProperty("numOfRows")
        private Integer numOfRows;
        @JsonProperty("pageNo")
        private Integer pageNo;
        @JsonProperty("items")
        private Items items;
        @JsonProperty("dataType")
        private String dataType;
    }

    @Getter
    @Setter
    public static class Items {

        @JsonProperty("item")
        private List<Item> item;
    }

    @Getter
    @Setter
    public static class Item {

        @JsonProperty("ny")
        private Integer ny;
        @JsonProperty("nx")
        private Integer nx;
        @JsonProperty("fcstValue")
        private String fcstValue;
        @JsonProperty("fcstTime")
        private String fcstTime;
        @JsonProperty("fcstDate")
        private String fcstDate;
        @JsonProperty("category")
        private String category;
        @JsonProperty("baseTime")
        private String baseTime;
        @JsonProperty("baseDate")
        private String baseDate;
    }

    @Getter
    @Setter
    public static class Header {

        @JsonProperty("resultMsg")
        private String resultMsg;
        @JsonProperty("resultCode")
        private String resultCode;
    }
}
