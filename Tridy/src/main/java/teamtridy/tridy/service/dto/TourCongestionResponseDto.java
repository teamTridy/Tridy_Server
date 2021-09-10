package teamtridy.tridy.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourCongestionResponseDto {

    @JsonProperty("response")
    private Response response;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        @JsonProperty("body")
        private Body body;
        @JsonProperty("header")
        private Header header;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {

        @JsonProperty("totalCount")
        private Integer totalCount;
        @JsonProperty("pageNo")
        private Integer pageNo;
        @JsonProperty("numOfRows")
        private Integer numOfRows;
        @JsonProperty("items")
        private Items items;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {

        @JsonProperty("item")
        private Item item;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("title")
        private String title;
        @JsonProperty("estiNum")
        private Double estiNum;
        @JsonProperty("estiDecoRat")
        private Double estiDecoRat;
        @JsonProperty("estiDecoDivCd")
        private Integer estiDecoDivCd;
        @JsonProperty("contentId")
        private Integer contentId;
        @JsonProperty("cncrtAccPsonNum")
        private Double cncrtAccPsonNum;
        @JsonProperty("baseYmd")
        private Integer baseYmd;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {

        @JsonProperty("resultMsg")
        private String resultMsg;
        @JsonProperty("resultCode")
        private String resultCode;
    }
}
