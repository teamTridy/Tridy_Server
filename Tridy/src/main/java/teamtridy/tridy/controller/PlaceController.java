package teamtridy.tridy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.service.PlaceService;

import javax.validation.constraints.NotNull;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
@Validated
@Slf4j
public class PlaceController {
    public final PlaceService placeService;

    @GetMapping("")
    public ResponseEntity<PlaceReadAllResponseDto> readAll(@RequestParam(defaultValue = "1") @NotNull Integer page,
                                                           @RequestParam(defaultValue = "10") @NotNull Integer size) {
        return ResponseEntity.ok(placeService.readAll(page, size));
    }
}
