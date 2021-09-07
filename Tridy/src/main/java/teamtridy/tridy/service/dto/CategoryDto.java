package teamtridy.tridy.service.dto;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import teamtridy.tridy.domain.entity.Category;

@Data
@Builder
public class CategoryDto {

    private Long id;
    private String name;
    private Integer depth;
    private List<CategoryDto> children;

    public static CategoryDto ofContainSetChildren(Category category) {
        CategoryDto categoryDto = CategoryDto.builder().id(category.getId())
                .name(category.getName()).depth(category.getDepth()).build();

        if (category.getChildren().size() != 0) {
            List<CategoryDto> children = category.getChildren().stream()
                    .map(CategoryDto::ofContainSetChildren).collect(Collectors.toList());
            categoryDto.setChildren(children);
        }

        return categoryDto;
    }

    public static CategoryDto of(Category category) {
        CategoryDto categoryDto = CategoryDto.builder().id(category.getId())
                .name(category.getName()).depth(category.getDepth()).build();
        return categoryDto;
    }
}
