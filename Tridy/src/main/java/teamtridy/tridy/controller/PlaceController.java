package teamtridy.tridy.controller;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.CurrentUser;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.dto.PlaceReadResponseDto;
import teamtridy.tridy.dto.PlaceReviewReadAllResponseDto;
import teamtridy.tridy.dto.ReviewCreateRequestDto;
import teamtridy.tridy.dto.ReviewUpdateRequestDto;
import teamtridy.tridy.service.PlaceService;
import teamtridy.tridy.service.dto.PlaceReviewDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
@Validated
@Slf4j
public class PlaceController {

    public final PlaceService placeService;

    @GetMapping("/search")
    public ResponseEntity<PlaceReadAllResponseDto> readAllByQuery(@CurrentUser Account account,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size,
            @RequestParam @Length(min = 2) String query,
            @RequestParam(required = false) List<Long> regionIds,
            @RequestParam(required = false) List<Long> depth2CategoryIds) {
        return ResponseEntity
                .ok(placeService.readAllPlaceByQuery(account, page, size, query, regionIds,
                        depth2CategoryIds));
    }

    /*
     * facebook style! ????????? ??????????
     * ?????? ?????? resource??? ???????????? ????????? Path Variable??? ????????????,
     * ???????????? ???????????? ????????? Query Parameter??? ???????????? ?????? Best Practice??????.
     * URI??? ????????? TYPE??? ?????? ??????????????? ???????????? ???????????? ????????? ?????????
     * URI should only consist of parts that will never change and will continue to uniquely identify that resource throughout its lifetime
     */

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceReadResponseDto> read(@CurrentUser Account account,
            @PathVariable Long placeId) {
        return new ResponseEntity(placeService.read(account, placeId), HttpStatus.OK);
    }

    @PostMapping("/{placeId}/picks")
    public ResponseEntity createPick(@CurrentUser Account account, @PathVariable Long placeId) {
        placeService.createPick(account, placeId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @DeleteMapping("/{placeId}/picks")
    public ResponseEntity deletePick(@CurrentUser Account account, @PathVariable Long placeId) {
        placeService.deletePick(account, placeId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{placeId}/reviews")
    public ResponseEntity<PlaceReviewReadAllResponseDto> readAllReview(@CurrentUser Account account,
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "9223372036854775807") Long lastReviewId, //Long.MAX_VALUE
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size) {
        return new ResponseEntity(placeService.readAllReview(account, placeId, lastReviewId, size),
                HttpStatus.OK);
    }


    @PostMapping("/{placeId}/reviews")
    public ResponseEntity<PlaceReviewDto> saveReview(@CurrentUser Account account,
            @PathVariable Long placeId,
            @Valid @RequestBody ReviewCreateRequestDto reviewCreateRequestDto) {
        return new ResponseEntity(
                placeService.createReview(account, placeId, reviewCreateRequestDto),
                HttpStatus.CREATED);
    }


    @PutMapping("/{placeId}/reviews/{reviewId}")
    public ResponseEntity<PlaceReviewDto> updateReview(@CurrentUser Account account,
            @PathVariable Long placeId, @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequestDto reviewUpdateRequestDto) {
        return new ResponseEntity(
                placeService.updateReview(account, placeId, reviewId, reviewUpdateRequestDto),
                HttpStatus.OK);
    }

    @DeleteMapping("/{placeId}/reviews/{reviewId}")
    public ResponseEntity deleteReview(@CurrentUser Account account, @PathVariable Long placeId,
            @PathVariable Long reviewId) {
        placeService.deleteReview(account, placeId, reviewId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
