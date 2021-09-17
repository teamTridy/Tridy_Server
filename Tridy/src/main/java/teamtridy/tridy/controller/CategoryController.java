package teamtridy.tridy.controller;

import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.CurrentUser;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.service.CategoryService;
import teamtridy.tridy.service.dto.CategoryDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Validated
@Slf4j
public class CategoryController {

    public final CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<CategoryDto> readAll() {
        return ResponseEntity.ok(categoryService.readAll());
    }

    /*
        Coupang Style: 1 Depth 카테고리 정보 조회는 노출카테고리코드 값을 0으로 설정 후 호출합니다.
        children: 조회된 카테고리 코드의 1 Depth 하위 카테고리 List 표시.(2 Depth 이상의 children 는 표시하지 않음.) children 카테고리가 없을 경우 null 또는 빈 배열 반환
    */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> read(@PathVariable("id") Long categoryId) {
        return new ResponseEntity(categoryService.read(categoryId), HttpStatus.OK);
    }

    @GetMapping("/{depth1CategoryId}/places")
    public ResponseEntity<PlaceReadAllResponseDto> readAllPlaceByDepth1AndQuery(
            @CurrentUser Account account,
            @PathVariable Long depth1CategoryId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30) @NotNull Integer size,
            @RequestParam(required = false) @Length(min = 2) String query,
            @RequestParam(required = false) List<Long> regionIds,
            @RequestParam(required = false) List<Long> depth3CategoryIds) {
        return new ResponseEntity(categoryService.readAllPlaceByDepth1AndQuery(account, page,
                size,
                depth1CategoryId,
                query, regionIds, depth3CategoryIds), HttpStatus.OK);
    }
}
