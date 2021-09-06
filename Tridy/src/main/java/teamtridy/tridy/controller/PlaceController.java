package teamtridy.tridy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.CurrentUser;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.dto.PlaceReadResponseDto;
import teamtridy.tridy.dto.ReviewCreateRequestDto;
import teamtridy.tridy.dto.ReviewUpdateRequestDto;
import teamtridy.tridy.service.PlaceService;
import teamtridy.tridy.service.dto.ReviewDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
@Validated
@Slf4j
public class PlaceController {
    public final PlaceService placeService;

    @GetMapping("/search")
    public ResponseEntity<PlaceReadAllResponseDto> readAllByQuery(@RequestParam(defaultValue = "1") @Min(1) Integer page,
                                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size,
                                                                  @RequestParam @Length(min = 2) String query) { //@RequestParam List<Long> subCatId
        return ResponseEntity.ok(placeService.readAllByQuery(page - 1, size, query));
    }

    /*
     * facebook style! 쿼리가 좋을까?
     * 만약 어떤 resource를 식별하고 싶으면 Path Variable을 사용하고,
     * 정렬이나 필터링을 한다면 Query Parameter를 사용하는 것이 Best Practice이다.
     * URI는 리소스 TYPE의 특정 인스턴스를 고유하게 식별하는 리소스 식별자
     * URI should only consist of parts that will never change and will continue to uniquely identify that resource throughout its lifetime
     */

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceReadResponseDto> read(@PathVariable Long placeId) {
        return new ResponseEntity(placeService.read(placeId), HttpStatus.OK);
    }

    @PostMapping("/{placeId}/picks")
    public ResponseEntity createPick(@CurrentUser Account account, @PathVariable Long placeId) {
        placeService.createPick(account, placeId);
        return new ResponseEntity(HttpStatus.CREATED);
    }


    @DeleteMapping("/{placeId}/picks")
    public ResponseEntity deletePick(@CurrentUser Account account, @PathVariable Long placeId) {
        placeService.deletePick(account, placeId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{placeId}/reviews")
    public ResponseEntity<ReviewDto> saveReview(@CurrentUser Account account, @PathVariable Long placeId, @Valid @RequestBody ReviewCreateRequestDto reviewCreateRequestDto) {
        return new ResponseEntity(placeService.createReview(account, placeId, reviewCreateRequestDto), HttpStatus.CREATED);
    }


    @PutMapping("/{placeId}/reviews/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(@CurrentUser Account account, @PathVariable Long placeId, @PathVariable Long reviewId, @Valid @RequestBody ReviewUpdateRequestDto reviewUpdateRequestDto) {
        return new ResponseEntity(placeService.updateReview(account, placeId, reviewId, reviewUpdateRequestDto), HttpStatus.OK);
    }

    @DeleteMapping("/{placeId}/reviews/{reviewId}")
    public ResponseEntity deleteReview(@CurrentUser Account account, @PathVariable Long placeId, @PathVariable Long reviewId) {
        placeService.deleteReview(account, placeId, reviewId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
