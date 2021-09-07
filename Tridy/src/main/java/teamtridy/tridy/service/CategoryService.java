package teamtridy.tridy.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamtridy.tridy.domain.entity.Category;
import teamtridy.tridy.domain.repository.CategoryRepository;
import teamtridy.tridy.exception.NotFoundException;
import teamtridy.tridy.service.dto.CategoryDto;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto readAll() {
        Category category = categoryRepository.findByDepth(0);
        return CategoryDto.ofContainSetChildren(category); // Dto 내부에서 자식을 설정함 -> 재귀적으로 자식의 자식까지 설정됨
    }

    @Transactional
    public CategoryDto read(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 카테고리 입니다."));

        CategoryDto categoryDto = CategoryDto.of(category); // 자식은 설정하지 않음. 자신만 설정함.

        if (category.getChildren().size() != 0) { // 자식을 따로 설정해줌.
            List<CategoryDto> children = category.getChildren().stream()
                    .map(CategoryDto::of).collect(Collectors.toList()); //재귀x
            categoryDto.setChildren(children);
        }

        return categoryDto;
    }
}
