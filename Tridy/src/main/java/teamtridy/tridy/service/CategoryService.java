package teamtridy.tridy.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Category;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Region;
import teamtridy.tridy.domain.repository.CategoryRepository;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.domain.repository.RegionRepository;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.error.CustomException;
import teamtridy.tridy.error.ErrorCode;
import teamtridy.tridy.service.dto.CategoryDto;
import teamtridy.tridy.service.dto.PlaceDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PlaceRepository placeRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public CategoryDto readAll() {
        Category category = categoryRepository.findByDepth(0);
        return CategoryDto.ofContainSetChildren(category); // Dto 내부에서 자식을 설정함 -> 재귀적으로 자식의 자식까지 설정됨
    }

    @Transactional
    public CategoryDto read(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        CategoryDto categoryDto = CategoryDto.of(category); // 자식은 설정하지 않음. 자신만 설정함.

        if (category.getChildren().size() != 0) { // 자식을 따로 설정해줌.
            List<CategoryDto> children = category.getChildren().stream()
                    .map(CategoryDto::of).collect(Collectors.toList()); //재귀x
            categoryDto.setChildren(children);
        }

        return categoryDto;
    }

    @Transactional
    public PlaceReadAllResponseDto readAllPlaceByDepth1AndQuery(Account account, Integer page,
            Integer size,
            Long depth1CategoryId,
            String query, List<Long> regionIds, List<Long> depth3CategoryIds) {

        Category depth1Category = categoryRepository
                .findById(depth1CategoryId) // foreach 는 요소를 돌면서 실행되는 최종 작업
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Region> regions = null;
        if (regionIds != null) {
            regions = regionIds.stream()
                    .map(regionId -> regionRepository.findById(regionId)
                            .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND)))
                    .collect(Collectors.toList());
        }

        List<Category> depth3Categories;
        if (depth3CategoryIds != null) { //필터링할 최하위 카테고리 ID 값이 들어왔으면
            depth3Categories = depth3CategoryIds.stream()
                    .map(subCatId -> categoryRepository
                            .findById(subCatId) // foreach 는 요소를 돌면서 실행되는 최종 작업
                            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)))
                    .collect(Collectors.toList());
        } else {
            depth3Categories = depth1Category.getChildren().stream() //depth2Categories
                    .map(Category::getChildren) //depth3Categories
                    .flatMap(
                            Collection::stream)
                    .collect(Collectors.toList());
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Slice<Place> places;

        if (query != null) {
            String cleanQuery = query.strip().replace("\\s+", " ").replace(" ", "%");

            if (regions != null) {
                places = placeRepository
                        .findAllByQueryAndCategoryInAndRegionIn(cleanQuery, depth3Categories,
                                regions,
                                pageRequest); // 검색할 때 in으로 해서 자식카테고리에 해당하는 장소들 모두 가져오기
            } else {
                places = placeRepository
                        .findAllByQueryAndCategoryIn(cleanQuery, depth3Categories, pageRequest);
            }
        } else {
            if (regions != null) {
                places = placeRepository
                        .findAllByCategoryInAndRegionIn(depth3Categories, regions, pageRequest);
            } else {
                places = placeRepository
                        .findAllByCategoryIn(depth3Categories, pageRequest);
            }
        }

        List<PlaceDto> placeDtos = places.stream()
                .map(place -> PlaceDto.of(place, account))
                .collect(Collectors.toList());

        return PlaceReadAllResponseDto.builder()
                .currentPage(places.getNumber() + 1)
                .currentSize(places.getNumberOfElements())
                .hasNextPage(places.hasNext())
                .places(placeDtos).build();
    }
}
