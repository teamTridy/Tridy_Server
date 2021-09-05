package teamtridy.tridy.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import teamtridy.tridy.domain.entity.Place;
import teamtridy.tridy.domain.repository.PlaceRepository;
import teamtridy.tridy.dto.PlaceReadAllResponseDto;
import teamtridy.tridy.service.dto.PlaceDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;

    public PlaceReadAllResponseDto readAll(Integer page, Integer size, String query) {
        PageRequest pageRequest;
        pageRequest = PageRequest.of(page, size);

        Slice<Place> places = null;

        if (query != null) {
            query = query.strip().replace("\\s+", " ").replace(" ", "%");
            places = placeRepository.findAllByQuery(pageRequest, query);
        } else {
            places = placeRepository.findAll(pageRequest);
        }

        List<PlaceDto> placeDtos = places.stream()
                .map(PlaceDto::of)
                .collect(Collectors.toList());

        PlaceReadAllResponseDto placeReadAllResponseDto = PlaceReadAllResponseDto.builder()
                .currentPage(places.getNumber())
                .currentSize(places.getNumberOfElements())
                .hasNextPage(places.hasNext())
                .places(placeDtos).build();

        return placeReadAllResponseDto;
    }
}
