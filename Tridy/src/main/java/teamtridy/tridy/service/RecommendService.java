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
    getById() ??? ?????? ???????????? ???????????? ????????? DB ??? ???????????? ?????? ????????? ??????????????? ?????? ???????????????.

????????? ?????? ???????????? ID ?????? ????????? ?????? ????????? DB ??? ???????????? ?????? ???????????? ???????????? ????????? ??? ????????????.
     */

    public InterestRecommendReadResponseDto readInterest(Account account) {

        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
        LocalDateTime todayEndTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
        RecommendType interest1RecommendType = recommendTypeRepository
                .getById(INTEREST1_RECOMMEND_TYPE_ID);
        RecommendType interest2RecommendType = recommendTypeRepository
                .getById(INTEREST2_RECOMMEND_TYPE_ID);

        List<Recommend> interest1Recommends = recommendRepository
                .findByAccountAndCreatedAtBetweenAndRecommendTypeOrderById(account, todayStartTime,
                        todayEndTime, interest1RecommendType);

        if (interest1Recommends.isEmpty()) { // ????????? ?????? ??????
            return createInterest(account);
        }

        List<Recommend> interest2Recommends = recommendRepository
                .findByAccountAndCreatedAtBetweenAndRecommendTypeOrderById(account, todayStartTime,
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
                        mainRecommendType); // ?????? ????????? ??????????????? ?????????

        if (todayMainRecommends.isEmpty()) { //????????? ?????? ?????? ??? ??????
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
                mainRecommend)); //???????????? ?????????????????????????????? ????????? ??? ???????????? add

        List<MainRecommendPlaceDto> mainRecommendPlaceDtos = todayMainRecommends.stream()
                .map(mainRecommend -> MainRecommendPlaceDto.of(mainRecommend, account))
                .collect(Collectors.toList());

        return MainRecommendReadResponseDto.builder()
                .address(address)
                .places(mainRecommendPlaceDtos)
                .build();
    }

    /*
    ??????x, ????????????x, ???????????? ??????

    ??????????????? ?????? 4??? :
                    ???????????? + ?????? 50??? ?????? + ????????? ????????????
                    -> ???????????? + ?????? 50??? ??????x + ????????? ????????????
                    -> ????????????x + ?????? 50??? ?????? + ????????? ????????????
                    -> ????????????x + ?????? 50??? ??????x + ????????? ????????????

    ??????????????? ?????? 5??? - ????????? ?????? ?????? = ?????? 1??? :
                    ???????????? + ?????? 50??? ?????? + ????????? ????????????
                    -> ???????????? + ?????? 50??? ??????x + ????????? ????????????
                    -> ????????????x + ?????? 50??? ?????? + ????????? ????????????
                    -> ????????????x + ?????? 50??? ??????x + ????????? ????????????
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
                                MAIN_RECOMMEND_COUNT - 1); // ?????? 4???

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
            //????????? ?????? ????????? ??????(??????????????? ?????? ?????? ???????????? ?????? ??????)??? ???????????? ???????????? ????????????. ???????????? ?????? ????????? ??????????????? ???????????? ?????????????????????.
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

        // ?????? ?????? ?????? ??????????????? ????????? ??????????????? ????????? ??????
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
            recommendRepository
                    .deleteTop5ByAccountAndReferenceAddressAndCreatedAtBetweenAndRecommendType(
                            account,
                            address,
                            todayStartTime,
                            todayEndTime,
                            mainRecommendType);
        }

        List<DistanceIncludePlace> DistanceIncludePlaces = readMainDistanceIncludePlaces(
                account, latitude, longitude, shouldBeIndoorsToday);

        List<Recommend> mainRecommends = IntStream
                .range(0, DistanceIncludePlaces.size()) //size()-1?????? ??????
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
                .range(0, DistanceIncludePlaces.size()) //size()-1?????? ??????
                .mapToObj(index -> Recommend.builder().account(account)
                        .place(placeRepository
                                .getById(DistanceIncludePlaces.get(index).getId()))
                        .distanceFromReference(
                                DistanceIncludePlaces.get(index).getDistance())
                        .recommendType(relatedRecommendType)
                        .orderNum(index)
                        .build()
                        .setMainRecommend(mainRecommend)) //???????????? ??????????????? ??????
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
        RecommendType interest1RecommendType = recommendTypeRepository
                .getById(INTEREST1_RECOMMEND_TYPE_ID);
        RecommendType interest2RecommendType = recommendTypeRepository
                .getById(INTEREST2_RECOMMEND_TYPE_ID);

        recommendRepository.deleteAllByAccountAndRecommendType(account, interest1RecommendType);
        recommendRepository.deleteAllByAccountAndRecommendType(account, interest2RecommendType);
    }
}
