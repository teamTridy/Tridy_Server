package teamtridy.tridy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import teamtridy.tridy.controller.JejuAirport;
import teamtridy.tridy.dto.WeatherCurrentResponseDto;
import teamtridy.tridy.service.VillageForecastService.Grid;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureRestDocs // (1)
@SpringBootTest
class VillageForecastServiceTest {

    @Autowired
    VillageForecastService villageForecastService;

    @Test
    void getConvertGpsToGrid() {
        Grid grid = villageForecastService
                .convertGpsToGrid(33.9518128033, 126.3076233112);
        System.out.println(grid.x + ":" + grid.y);

    }

    @Test
    void getCurrentWeather() {
        WeatherCurrentResponseDto weatherCurrentResponseDto = villageForecastService
                .getCurrentWeather(JejuAirport.LATITUDE, JejuAirport.LONGITUDE,
                        JejuAirport.ADDRESS);
        System.out.println(weatherCurrentResponseDto.getDescription());
    }
}