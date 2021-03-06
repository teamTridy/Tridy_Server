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
import teamtridy.tridy.service.dto.PlaceReviewDto;

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
                            .findById(subCatId) // foreach ??? ????????? ????????? ???????????? ?????? ??????
                            .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)))
                    .map(Category::getChildren)
                    .flatMap(
                            Collection::stream) //stream??? ???????????? list?????????
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
                            pageRequest); // ????????? ??? in?????? ?????? ????????????????????? ???????????? ????????? ?????? ????????????
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
        Long reviewTotalCount = reviewRepository.countByPlaceAndIsPrivate(place, false);

        if (reviewTotalCount != 0) {
            Slice<Review> reviews = reviewRepository
                    .findByIdLessThanAndPlaceAndIsPrivateOrderByIdDesc(lastReviewId, place, false,
                            pageRequest);

            List<PlaceReviewDto> placeReviewDtos = reviews.stream()
                    .map(review -> PlaceReviewDto.of(review, account))
                    .collect(Collectors.toList());
            Long newLastReviewId = placeReviewDtos.get(placeReviewDtos.size() - 1).getId();

            // get rating info
            Float ratingAverage = reviewRepository.getRatingAverageAndIsPrivate(place, false);
            ArrayList<Integer> ratings = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
            List<Float> ratingRatios = ratings.stream()
                    .map(rating -> reviewRepository
                            .countByPlaceAndIsPrivateAndRating(place, false, rating))
                    .map(count -> Math.round(count / (float) reviewTotalCount * 100)
                            / (float) 100.0)//????????? ?????????????????? ?????????
                    .collect(Collectors.toList());

            return PlaceReviewReadAllResponseDto
                    .builder()
                    .lastReviewId(newLastReviewId)
                    .currentSize(reviews.getNumberOfElements())
                    .hasNextPage(reviews.hasNext())
                    .ratingAverage(ratingAverage)
                    .ratingRatios(ratingRatios)
                    .reviewTotalCount(reviewTotalCount)
                    .reviews(placeReviewDtos)
                    .build();
        } else {
            return PlaceReviewReadAllResponseDto
                    .builder()
                    .lastReviewId(null)
                    .currentSize(0)
                    .hasNextPage(false)
                    .ratingAverage(0f)
                    .ratingRatios(Arrays.asList(0f, 0f, 0f, 0f, 0f))
                    .reviewTotalCount(0L)
                    .reviews(Arrays.asList())
                    .build();
        }
    }

    @Transactional
    public PlaceReviewDto createReview(Account account, Long placeId,
            ReviewCreateRequestDto reviewCreateRequestDto) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        Review review = reviewCreateRequestDto.toReview();
        review.setAccount(account);
        review.setPlace(place);

        review = reviewRepository.save(review);

        return PlaceReviewDto.of(review, account);
    }

    @Transactional
    public PlaceReviewDto updateReview(Account account, Long placeId, Long reviewId,
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

        return PlaceReviewDto.of(review, account);
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
