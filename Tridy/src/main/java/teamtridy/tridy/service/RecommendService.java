package teamtridy.tridy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.domain.entity.Account;
import teamtridy.tridy.domain.entity.AccountInterest;
import teamtridy.tridy.domain.entity.Interest;
import teamtridy.tridy.domain.entity.Recommend;
import teamtridy.tridy.domain.entity.RecommendType;
import teamtridy.tridy.domain.repository.InterestRepository;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.domain.repository.RecommendRepository;
import teamtridy.tridy.domain.repository.RecommendRepository.DistanceIncludePlace;
import teamtridy.tridy.domain.repository.RecommendRepository.PlaceId;
import teamtridy.tridy.domain.repository.RecommendTypeRepository;
import teamtridy.tridy.dto.InterestRecommendPlaceDto;
import teamtridy.tridy.dto.InterestRecommendReadResponseDto;
import teamtridy.tridy.dto.MainRecommendReadResponseDto;
import teamtridy.tridy.service.dto.MainRecommendPlaceDto;
import teamtridy.tridy.service.dto.RecommendPlaceDto;

@RequiredArgsConstructor
@Service
public class RecommendService {

    private final Long MAIN_RECOMMEND_TYPE_ID = 1L;
    private final Long RELATED_RECOMMEND_TYPE_ID = 2L;
    private final Long INTEREST1_RECOMMEND_TYPE_ID = 3L;
    private final Long INTEREST2_RECOMMEND_TYPE_ID = 4L;
    private final Integer MAIN_RECOMMEND_COUNT = 5;

    private final TourService tourService;
    private final RecommendRepository recommendRepository;
    private final RecommendTypeRepository recommendTypeRepository;
    private final PlaceRepository placeRepository;
    private final InterestRepository interestRepository;

    /*
    getById() 는 해당 엔티티를 사용하기 전까진 DB 에 접근하지 않기 때문에 성능상으로 좀더 유리합니다.

따라서 특정 엔티티의 ID 값만 활용할 일이 있다면 DB 에 접근하지 않고 프록시만 가져와서 사용할 수 있습니다.
     */

    public InterestRecommendReadResponseDto readInterest(Account account) {

        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime todayEndTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
        RecommendType interest1RecommendType = recommendTypeRepository
                .getById(INTEREST1_RECOMMEND_TYPE_ID);
        RecommendType interest2RecommendType = recommendTypeRepository
                .getById(INTEREST2_RECOMMEND_TYPE_ID);

        List<Recommend> interest1Recommends = recommendRepository
                .findByAccountAndCreatedAtBetweenAndRecommendType(account, todayStartTime,
                        todayEndTime, interest1RecommendType);

        if (interest1Recommends.isEmpty()) { // 없으면 새로 생성
            return createInterest(account);
        }

        List<Recommend> interest2Recommends = recommendRepository
                .findByAccountAndCreatedAtBetweenAndRecommendType(account, todayStartTime,
                        todayEndTime, interest2RecommendType);

        List<RecommendPlaceDto> interest1RecommendPlaceDtos = interest1Recommends.stream()
                .map(interest1Recommend -> RecommendPlaceDto.of(interest1Recommend, account))
                .collect(
                        Collectors.toList());

        List<RecommendPlaceDto> interest2RecommendPlaceDtos = interest2Recommends.stream()
                .map(interest2Recommend -> RecommendPlaceDto.of(interest2Recommend, account))
                .collect(
                        Collectors.toList());

        InterestRecommendReadResponseDto interestRecommendReadResponseDto = InterestRecommendReadResponseDto
                .builder().interest1(InterestRecommendPlaceDto.builder()
                        .interestId(interest1Recommends.get(0).getInterest().getId())
                        .places(interest1RecommendPlaceDtos).build())
                .interest2(InterestRecommendPlaceDto.builder()
                        .interestId(interest2Recommends.get(0).getInterest().getId())
                        .places(interest2RecommendPlaceDtos).build()).build();

        return interestRecommendReadResponseDto;
    }

    @Transactional
    private InterestRecommendReadResponseDto createInterest(Account account) {
        List<Interest> allInterests = null;

        if (account.getHasTendency()) {
            allInterests = account.getAccountInterests().stream()
                    .map(AccountInterest::getInterest).collect(Collectors.toList());

        } else {
            allInterests = interestRepository.findAll();
        }

        Collections.shuffle(allInterests);
        List<Interest> randomInterests = allInterests.subList(0, 2);

        Interest interest1 = randomInterests.get(0);
        Interest interest2 = randomInterests.get(1);

        List<PlaceId> Interest1PlaceIds = recommendRepository
                .findPlaceTop3ByInterestIdOrderByRandom(account.getId(),
                        interest1.getId());
        List<PlaceId> Interest2PlaceIds = recommendRepository
                .findPlaceTop3ByInterestIdOrderByRandom(account.getId(),
                        interest2.getId());

        List<Recommend> interest1Recommends = Interest1PlaceIds.stream()
                .map(placeId -> Recommend.builder().account(account).interest(interest1)
                        .place(placeRepository.getById(placeId.getId()))
                        .recommendType(
                                recommendTypeRepository.getById(INTEREST1_RECOMMEND_TYPE_ID))
                        .congestion(tourService.getCongestionLevel(placeId.getId()))
                        .build()).collect(Collectors.toList());

        List<Recommend> interest2Recommends = Interest2PlaceIds.stream()
                .map(placeId -> Recommend.builder().account(account).interest(interest2)
                        .place(placeRepository.getById(placeId.getId()))
                        .recommendType(
                                recommendTypeRepository.getById(INTEREST2_RECOMMEND_TYPE_ID))
                        .congestion(tourService.getCongestionLevel(placeId.getId()))
                        .build()).collect(Collectors.toList());

        interest1Recommends = recommendRepository.saveAll(interest1Recommends);
        interest2Recommends = recommendRepository.saveAll(interest2Recommends);

        List<RecommendPlaceDto> interest1RecommendPlaceDtos = interest1Recommends.stream()
                .map(interest1Recommend -> RecommendPlaceDto.of(interest1Recommend, account))
                .collect(
                        Collectors.toList());

        List<RecommendPlaceDto> interest2RecommendPlaceDtos = interest2Recommends.stream()
                .map(interest2Recommend -> RecommendPlaceDto.of(interest2Recommend, account))
                .collect(
                        Collectors.toList());

        InterestRecommendReadResponseDto interestRecommendReadResponseDto = InterestRecommendReadResponseDto
                .builder().interest1(InterestRecommendPlaceDto.builder()
                        .interestId(interest1Recommends.get(0).getInterest().getId())
                        .places(interest1RecommendPlaceDtos).build())
                .interest2(InterestRecommendPlaceDto.builder()
                        .interestId(interest2Recommends.get(0).getInterest().getId())
                        .places(interest2RecommendPlaceDtos).build()).build();

        return interestRecommendReadResponseDto;
    }

    @Transactional
    public MainRecommendReadResponseDto readMain(Account account, Double latitude,
            Double longitude,
            String address,
            Boolean shouldBeIndoorsToday) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime todayEndTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
        RecommendType mainRecommendType = recommendTypeRepository.getById(MAIN_RECOMMEND_TYPE_ID);

        List<Recommend> todayMainRecommends = recommendRepository
                .findTop5ByAccountAndCreatedAtBetweenAndRecommendTypeOrderByCreatedAtDescOrderNum(
                        account,
                        todayStartTime,
                        todayEndTime,
                        mainRecommendType); // 오늘 생성된 메인추천이 있는지

        if (todayMainRecommends.isEmpty()) { //없으면 새로 만든 것 반환
            return createMain(account, latitude, longitude, address,
                    shouldBeIndoorsToday);
        }

        List<MainRecommendPlaceDto> mainRecommendPlaceDtos = todayMainRecommends.stream()
                .map(mainRecommend -> MainRecommendPlaceDto.of(mainRecommend, account))
                .collect(Collectors.toList());

        return MainRecommendReadResponseDto.builder()
                .address(address)
                .places(mainRecommendPlaceDtos)
                .build();
    }

    @CacheEvict(value = "mainRecommendCache", key = "#account.getId()")
    @Transactional
    public MainRecommendReadResponseDto createMain(Account account, Double latitude,
            Double longitude,
            String address,
            Boolean shouldBeIndoorsToday) {

        List<Recommend> todayMainRecommends = createMainExceptRelated(account, latitude, longitude,
                address,
                shouldBeIndoorsToday);

        todayMainRecommends.forEach(mainRecommend -> createdRelated(account,
                mainRecommend)); //이안에서 연관관계편의메소드로 연관된 것 리스트에 add

        List<MainRecommendPlaceDto> mainRecommendPlaceDtos = todayMainRecommends.stream()
                .map(mainRecommend -> MainRecommendPlaceDto.of(mainRecommend, account))
                .collect(Collectors.toList());

        return MainRecommendReadResponseDto.builder()
                .address(address)
                .places(mainRecommendPlaceDtos)
                .build();
    }

    /*
    리뷰x, 메인추천x, 날씨태그 고려

    관심사일치 상위 4개 :
                    범위일치 + 인기 50퍼 일치 + 여기서 랜덤정렬
                    -> 범위일치 + 인기 50퍼 일치x + 여기서 랜덤정렬
                    -> 범위일치x + 인기 50퍼 일치 + 여기서 랜덤정렬
                    -> 범위일치x + 인기 50퍼 일치x + 여기서 랜덤정렬

    관심사일치 하위 5개 - 관심사 일치 개수 = 최대 1개 :
                    범위일치 + 인기 50퍼 일치 + 여기서 랜덤정렬
                    -> 범위일치 + 인기 50퍼 일치x + 여기서 랜덤정렬
                    -> 범위일치x + 인기 50퍼 일치 + 여기서 랜덤정렬
                    -> 범위일치x + 인기 50퍼 일치x + 여기서 랜덤정렬
    */
    @Transactional
    private List<DistanceIncludePlace> readMainDistanceIncludePlaces(Account account,
            Double latitude, Double longitude, Boolean shouldBeIndoorsToday) {

        List<DistanceIncludePlace> recommendedPlace = new ArrayList<>();

        if (account.getHasTendency()) {
            List<DistanceIncludePlace> interestMatchedPlaceIds;
            List<DistanceIncludePlace> interestNotMatchedPlaceIds;

            Integer distanceLt30Order = (account.getIsPreferredFar()) ? 1 : 2;
            Integer viewPerRankGt50Order = (account.getIsPreferredPopular()) ? 1 : 2;
            Integer viewPerRankLt50Order = (account.getIsPreferredPopular()) ? 2 : 1;

            if (shouldBeIndoorsToday) {
                interestMatchedPlaceIds = recommendRepository
                        .findTopCountPlaceByInInterestAndIndoorWeather(account.getId(),
                                latitude, longitude, distanceLt30Order,
                                viewPerRankGt50Order, viewPerRankLt50Order,
                                MAIN_RECOMMEND_COUNT - 1); // 최대 4개

                interestNotMatchedPlaceIds = recommendRepository
                        .findTopCountPlaceByNotInInterestAndIndoorWeather(account.getId(),
                                latitude, longitude, distanceLt30Order,
                                viewPerRankGt50Order, viewPerRankLt50Order,
                                MAIN_RECOMMEND_COUNT - interestMatchedPlaceIds.size());

            } else {
                interestMatchedPlaceIds = recommendRepository
                        .findTopCountPlaceByInInterest(account.getId(),
                                latitude, longitude, distanceLt30Order,
                                viewPerRankGt50Order, viewPerRankLt50Order,
                                MAIN_RECOMMEND_COUNT - 1);

                interestNotMatchedPlaceIds = recommendRepository
                        .findTopCountPlaceByNotInInterest(account.getId(),
                                latitude, longitude, distanceLt30Order,
                                viewPerRankGt50Order, viewPerRankLt50Order,
                                MAIN_RECOMMEND_COUNT - interestMatchedPlaceIds.size());
            }

            recommendedPlace.addAll(interestMatchedPlaceIds);
            recommendedPlace.addAll(interestNotMatchedPlaceIds);
        } else {
            //성향이 없는 경우는 날씨(컨트롤러를 통해 받은 제주공항 기준 날씨)만 고려하고 랜덤으러 뽑으면됨. 왜냐하면 어떤 범위를 좋아한다고 가정되지 않았기때문이다.
            List<DistanceIncludePlace> interestNotConsiderPlaceIds;
            if (shouldBeIndoorsToday) {
                interestNotConsiderPlaceIds = recommendRepository
                        .findTopCountPlaceByIndoorWeather(account.getId(), latitude, longitude,
                                MAIN_RECOMMEND_COUNT);
            } else {
                interestNotConsiderPlaceIds = recommendRepository
                        .findTopCountPlace(account.getId(), latitude, longitude,
                                MAIN_RECOMMEND_COUNT);
            }
            recommendedPlace.addAll(interestNotConsiderPlaceIds);
        }

        return recommendedPlace;
    }

    @Transactional
    private List<Recommend> createMainExceptRelated(Account account, Double latitude,
            Double longitude,
            String address,
            Boolean shouldBeIndoorsToday) {

        RecommendType mainRecommendType = recommendTypeRepository.getById(MAIN_RECOMMEND_TYPE_ID);

        // 오늘 이미 같은 읍면동에서 메인이 추천됐다면 기존것 삭제
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime todayEndTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));

        List<Recommend> todayMainRecommendsByAddress = recommendRepository
                .findTop5ByAccountAndReferenceAddressAndCreatedAtBetweenAndRecommendType(
                        account,
                        address,
                        todayStartTime,
                        todayEndTime,
                        mainRecommendType);

        if (!todayMainRecommendsByAddress.isEmpty()) {
            recommendRepository.deleteAll(todayMainRecommendsByAddress);
        }

        List<DistanceIncludePlace> DistanceIncludePlaces = readMainDistanceIncludePlaces(
                account, latitude, longitude, shouldBeIndoorsToday);

        List<Recommend> mainRecommends = IntStream
                .range(0, DistanceIncludePlaces.size()) //size()-1까지 돈다
                .mapToObj(index -> Recommend.builder().account(account)
                        .place(placeRepository
                                .getById(DistanceIncludePlaces.get(index).getId()))
                        .distanceFromReference(
                                DistanceIncludePlaces.get(index).getDistance())
                        .recommendType(mainRecommendType) //main Recommend Type id = lL
                        .referenceAddress(address)
                        .orderNum(index)
                        .congestion(tourService
                                .getCongestionLevel(DistanceIncludePlaces.get(index).getId()))
                        .build())
                .collect(Collectors.toList());

        return recommendRepository.saveAll(mainRecommends);
    }

    @Transactional
    private List<Recommend> createdRelated(Account account, Recommend mainRecommend) {

        List<DistanceIncludePlace> DistanceIncludePlaces = recommendRepository
                .findTop3RelatedPlaceByMainRecommendAndInFoodOrCafeCategory(
                        mainRecommend.getPlace().getLatitude(),
                        mainRecommend.getPlace().getLongitude(),
                        account.getId());

        RecommendType relatedRecommendType = recommendTypeRepository
                .getById(RELATED_RECOMMEND_TYPE_ID);

        List<Recommend> relatedRecommends = IntStream
                .range(0, DistanceIncludePlaces.size()) //size()-1까지 돈다
                .mapToObj(index -> Recommend.builder().account(account)
                        .place(placeRepository
                                .getById(DistanceIncludePlaces.get(index).getId()))
                        .distanceFromReference(
                                DistanceIncludePlaces.get(index).getDistance())
                        .recommendType(relatedRecommendType)
                        .orderNum(index)
                        .build()
                        .setMainRecommend(mainRecommend)) //연관관계 편의메소드 사용
                .collect(Collectors.toList());

        return recommendRepository.saveAll(relatedRecommends);
    }

    @Transactional
    public void deleteExpiredMain() {
        LocalDateTime startTime = LocalDateTime
                .of(LocalDate.now().minusDays(3), LocalTime.of(0, 0, 0));

        LocalDateTime endTime = LocalDateTime
                .of(LocalDate.now().minusDays(3), LocalTime.of(23, 59, 59));

        RecommendType mainRecommend = recommendTypeRepository.getById(MAIN_RECOMMEND_TYPE_ID);

        recommendRepository.deleteAllByCreatedAtBetweenAndRecommendType(startTime, endTime,
                mainRecommend);

    }

    @Transactional
    public void deleteExpiredRelated() {
        LocalDateTime startTime = LocalDateTime
                .of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0));
        LocalDateTime endTime = LocalDateTime
                .of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));

        RecommendType relatedRecommendType = recommendTypeRepository
                .getById(RELATED_RECOMMEND_TYPE_ID);

        recommendRepository.deleteAllByCreatedAtBetweenAndRecommendType(startTime, endTime,
                relatedRecommendType);
    }


    @Transactional
    public void deleteExpiredInterest() {
        LocalDateTime startTime = LocalDateTime
                .of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0));
        LocalDateTime endTime = LocalDateTime
                .of(LocalDate.now().minusDays(1), LocalTime.of(23, 59, 59));

        RecommendType interest1RecommendType = recommendTypeRepository
                .getById(INTEREST1_RECOMMEND_TYPE_ID);
        RecommendType interest2RecommendType = recommendTypeRepository
                .getById(INTEREST2_RECOMMEND_TYPE_ID);

        recommendRepository.deleteAllByCreatedAtBetweenAndRecommendType(startTime, endTime,
                interest1RecommendType);
        recommendRepository.deleteAllByCreatedAtBetweenAndRecommendType(startTime, endTime,
                interest2RecommendType);

    }

    @Transactional
    public void deleteInterest(Account account) {
        recommendRepository.deleteAllByAccount(account);
    }
}
