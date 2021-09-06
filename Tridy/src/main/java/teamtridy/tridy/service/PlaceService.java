package teamtridy.tridy.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.Pick;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.entity.Review;
import teamtridy.tridy.domain.repository.PickRepository;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.domain.repository.ReviewRepository;
import teamtridy.tridy.dto.*;
import teamtridy.tridy.exception.AlreadyExistsException;
import teamtridy.tridy.exception.NotFoundException;
import teamtridy.tridy.service.dto.PlaceDto;
import teamtridy.tridy.service.dto.ReviewDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final PickRepository pickRepository;
    private final ReviewRepository reviewRepository;

    public PlaceReadAllResponseDto readAllByQuery(Integer page, Integer size, String query) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Slice<Place> places = null;
        if (query != null) {
            query = query.strip().replace("\\s+", " ").replace(" ", "%");
            places = placeRepository.findAllByQuery(query, pageRequest);
        } else {
            places = placeRepository.findAll(pageRequest);
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

    public PlaceReadResponseDto read(Long placeId) {
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
    public PlaceReviewReadAllResponseDto readAllReview(Long placeId, Long lastReviewId, Integer size) {
        Place place = placeRepository
                .findById(placeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 장소 입니다."));

        PageRequest pageRequest = PageRequest.of(0, size);

        // get review info
        Long reviewTotalCount = reviewRepository.countByPlace(place);
        Slice<Review> reviews = reviewRepository.findByIdLessThanAndPlaceAndIsPrivateOrderByIdDesc(lastReviewId, place, false,pageRequest);
        List<ReviewDto> reviewDtos = reviews.stream()
                .map(ReviewDto::of)
                .collect(Collectors.toList());
        Long newLastReviewId = reviewDtos.get(reviewDtos.size() - 1).getId();

        // get rating info
        Float ratingAverage = reviewRepository.getRatingAverage(place);
        ArrayList<Integer> ratings = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        List<Float> ratingRatios = ratings.stream()
                .map(rating -> reviewRepository.countByPlaceAndRating(place, rating))
                .map(count -> Math.round(count / (float) reviewTotalCount * 100) / (float) 100.0 )//소수점 둘째자리까지 남기기
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
    public ReviewDto createReview(Account account, Long placeId, ReviewCreateRequestDto reviewCreateRequestDto) {
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
    public ReviewDto updateReview(Account account, Long placeId, Long reviewId, ReviewUpdateRequestDto reviewUpdateRequestDto) {
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
