package teamtridy.tridy.service;


import java.util.ArrayList;
import java.util.Arrays;
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
import teamtridy.tridy.domain.entity.Pick;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Region;
import teamtridy.tridy.domain.entity.Review;
import teamtridy.tridy.domain.repository.CategoryRepository;
import teamtridy.tridy.domain.repository.PickRepository;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.domain.repository.RegionRepository;
import teamtridy.tridy.domain.repository.ReviewRepository;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.dto.PlaceReadResponseDto;
import teamtridy.tridy.dto.PlaceReviewReadAllResponseDto;
import teamtridy.tridy.dto.ReviewCreateRequestDto;
import teamtridy.tridy.dto.ReviewUpdateRequestDto;
import teamtridy.tridy.error.CustomException;
import teamtridy.tridy.error.ErrorCode;
import teamtridy.tridy.service.dto.PlaceDto;
import teamtridy.tridy.service.dto.ReviewDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PickRepository pickRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public PlaceReadAllResponseDto readAllPlaceByQuery(Account account, Integer page, Integer size,
            String query, List<Long> regionIds, List<Long> depth2CategoryIds) {
        List<Region> regions = null;
        if (regionIds != null) {
            regions = regionIds.stream()
                    .map(regionId -> regionRepository.findById(regionId)
                            .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND)))
                    .collect(Collectors.toList());
        }

        List<Category> depth3Categories = null;
        if (depth2CategoryIds != null) {
            depth3Categories = depth2CategoryIds.stream()
                    .map(subCatId -> categoryRepository
                            .findById(subCatId) // foreach 는 요소를 돌면서 실행되는 최종 작업
                            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)))
                    .map(Category::getChildren)
                    .flatMap(
                            Collection::stream) //stream을 이용하여 list합치기
                    .collect(Collectors.toList());
        }

        String cleanQuery = null;
        if (query != null) {
            cleanQuery = query.strip().replace("\\s+", " ").replace(" ", "%");
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Slice<Place> places = null;
        if (regions != null && depth3Categories != null) {
            places = placeRepository
                    .findAllByQueryAndCategoryInAndRegionIn(cleanQuery, depth3Categories,
                            regions,
                            pageRequest); // 검색할 때 in으로 해서 자식카테고리에 해당하는 장소들 모두 가져오기
        } else if (regions != null && depth3Categories == null) {
            places = placeRepository
                    .findAllByQueryAndRegionIn(cleanQuery, regions, pageRequest);
        } else if (regions == null && depth3Categories != null) {
            places = placeRepository
                    .findAllByQueryAndCategoryIn(cleanQuery, depth3Categories, pageRequest);
        } else {
            places = placeRepository.findAllByQuery(cleanQuery, pageRequest);
        }

        List<PlaceDto> placeDtos = places.stream()
                .map(place -> PlaceDto.of(place, account))
                .collect(Collectors.toList());

        PlaceReadAllResponseDto placeReadAllResponseDto = PlaceReadAllResponseDto.builder()
                .currentPage(places.getNumber() + 1)
                .currentSize(places.getNumberOfElements())
                .hasNextPage(places.hasNext())
                .places(placeDtos).build();

        return placeReadAllResponseDto;
    }

    public PlaceReadResponseDto read(Account account, Long placeId) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        return PlaceReadResponseDto.of(place, account);
    }

    @Transactional
    public void createPick(Account account, Long placeId) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        if (pickRepository.findByAccountAndPlace(account, place) != null) {
            throw new CustomException(ErrorCode.PICK_DUPLICATION);
        }

        Pick pick = Pick.builder().place(place).account(account).build();
        pickRepository.save(pick);
    }

    @Transactional
    public void deletePick(Account account, Long placeId) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        Pick pick = pickRepository.findByAccountAndPlace(account, place);
        if (pick == null) {
            throw new CustomException(ErrorCode.PICK_NOT_FOUND);
        }

        pickRepository.delete(pick);
    }

    @Transactional
    public PlaceReviewReadAllResponseDto readAllReview(Account account, Long placeId,
            Long lastReviewId,
            Integer size) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(0, size);

        // get review info
        Long reviewTotalCount = reviewRepository.countByPlace(place);
        Slice<Review> reviews = reviewRepository
                .findByIdLessThanAndPlaceAndIsPrivateOrderByIdDesc(lastReviewId, place, false,
                        pageRequest);
        List<ReviewDto> reviewDtos = reviews.stream()
                .map(review -> ReviewDto.of(review, account))
                .collect(Collectors.toList());
        Long newLastReviewId = reviewDtos.get(reviewDtos.size() - 1).getId();

        // get rating info
        Float ratingAverage = reviewRepository.getRatingAverage(place);
        ArrayList<Integer> ratings = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Float> ratingRatios = ratings.stream()
                .map(rating -> reviewRepository.countByPlaceAndRating(place, rating))
                .map(count -> Math.round(count / (float) reviewTotalCount * 100)
                        / (float) 100.0)//소수점 둘째자리까지 남기기
                .collect(Collectors.toList());

        return PlaceReviewReadAllResponseDto
                .builder()
                .lastReviewId(newLastReviewId)
                .currentSize(reviews.getNumberOfElements())
                .hasNextPage(reviews.hasNext())
                .ratingAverage(ratingAverage)
                .ratingRatios(ratingRatios)
                .reviewTotalCount(reviewTotalCount)
                .reviews(reviewDtos)
                .build();
    }

    @Transactional
    public ReviewDto createReview(Account account, Long placeId,
            ReviewCreateRequestDto reviewCreateRequestDto) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        Review review = reviewCreateRequestDto.toReview();
        review.setAccount(account);
        review.setPlace(place);

        review = reviewRepository.save(review);

        return ReviewDto.of(review, account);
    }

    @Transactional
    public ReviewDto updateReview(Account account, Long placeId, Long reviewId,
            ReviewUpdateRequestDto reviewUpdateRequestDto) {
        placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.getAccount().getId() != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        reviewUpdateRequestDto.apply(review);

        return ReviewDto.of(review, account);
    }

    @Transactional
    public void deleteReview(Account account, Long placeId, Long reviewId) {
        placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.getAccount().getId() != account.getId()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        reviewRepository.delete(review);
    }
}
