package teamtridy.tridy.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Category;
import teamtridy.tridy.domain.entity.Location;
import teamtridy.tridy.domain.entity.Pick;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Review;
import teamtridy.tridy.domain.repository.CategoryRepository;
import teamtridy.tridy.domain.repository.LocationRepository;
import teamtridy.tridy.domain.repository.PickRepository;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.domain.repository.ReviewRepository;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.dto.PlaceReadResponseDto;
import teamtridy.tridy.dto.PlaceReviewReadAllResponseDto;
import teamtridy.tridy.dto.ReviewCreateRequestDto;
import teamtridy.tridy.dto.ReviewUpdateRequestDto;
import teamtridy.tridy.exception.AlreadyExistsException;
import teamtridy.tridy.exception.NotFoundException;
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
    private final LocationRepository locationRepository;

    public PlaceReadAllResponseDto readAll(Account account, Integer page, Integer size,
        String query, List<Long> locationIds, List<Long> category2Ids) {
        List<Location> locations = null;
        if (locationIds != null) {
            locations = locationIds.stream()
                .map(locationId -> locationRepository.findById(locationId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 지역입니다.")))
                .collect(Collectors.toList());
        }

        List<Category> category3s = null;
        if (category2Ids != null) {
            category3s = category2Ids.stream()
                .map(subCatId -> categoryRepository
                    .findById(subCatId) // foreach 는 요소를 돌면서 실행되는 최종 작업
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리입니다.")))
                .map(category -> category.getChildCategories())
                .flatMap(
                    list -> list.stream()) //stream을 이용하여 list합치기 https://jekal82.tistory.com/60
                .collect(Collectors.toList());
        }

        String cleanQuery = null;
        if (query != null) {
            cleanQuery = query.strip().replace("\\s+", " ").replace(" ", "%");
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Slice<Place> places = null;
        if (locations != null && category3s != null) {
            places = placeRepository
                .findAllByQueryAndCategoryInAndLocationIn(cleanQuery, category3s, locations,
                    pageRequest); // 검색할 때 in으로 해서 자식카테고리에 해당하는 장소들 모두 가져오기
        } else if (locations != null && category3s == null) {
            places = placeRepository
                .findAllByQueryAndLocationIn(cleanQuery, locations, pageRequest);
        } else if (locations == null && category3s != null) {
            places = placeRepository
                .findAllByQueryAndCategoryIn(cleanQuery, category3s, pageRequest);
        } else {
            places = placeRepository.findAllByQuery(cleanQuery, pageRequest);
        }

        List<PlaceDto> placeDtos = places.stream()
            .map(PlaceDto::of)
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
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));
        return PlaceReadResponseDto.of(place);
    }

    @Transactional
    public void createPick(Account account, Long placeId) {
        Place place = placeRepository
            .findById(placeId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        if (pickRepository.findByAccountAndPlace(account, place) != null) {
            throw new AlreadyExistsException("이미 찜한 장소입니다.");
        }

        Pick pick = Pick.builder().place(place).account(account).build();
        pickRepository.save(pick);
    }

    @Transactional
    public void deletePick(Account account, Long placeId) {
        Place place = placeRepository
            .findById(placeId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        Pick pick = pickRepository.findByAccountAndPlace(account, place);
        if (pick == null) {
            throw new NotFoundException("찜한 장소가 아닙니다.");
        }

        pickRepository.delete(pick);
    }

    @Transactional
    public PlaceReviewReadAllResponseDto readAllReview(Long placeId, Long lastReviewId,
        Integer size) {
        Place place = placeRepository
            .findById(placeId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        PageRequest pageRequest = PageRequest.of(0, size);

        // get review info
        Long reviewTotalCount = reviewRepository.countByPlace(place);
        Slice<Review> reviews = reviewRepository
            .findByIdLessThanAndPlaceAndIsPrivateOrderByIdDesc(lastReviewId, place, false,
                pageRequest);
        List<ReviewDto> reviewDtos = reviews.stream()
            .map(ReviewDto::of)
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
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        Review review = reviewCreateRequestDto.toReview();
        review.setAccount(account);
        review.setPlace(place);

        review = reviewRepository.save(review);

        return ReviewDto.of(review);
    }

    @Transactional
    public ReviewDto updateReview(Account account, Long placeId, Long reviewId,
        ReviewUpdateRequestDto reviewUpdateRequestDto) {
        placeRepository
            .findById(placeId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 리뷰입니다."));

        if (review.getAccount().getId() != account.getId()) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        reviewUpdateRequestDto.apply(review);

        return ReviewDto.of(review);
    }

    public void deleteReview(Account account, Long placeId, Long reviewId) {
        placeRepository
            .findById(placeId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 리뷰입니다."));

        if (review.getAccount().getId() != account.getId()) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }
}
