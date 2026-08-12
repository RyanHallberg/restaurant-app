package com.ryanhallberg.restaurant.menu;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanhallberg.restaurant.common.error.NotFoundException;
import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.menu.dto.MenuCategoryResponse;
import com.ryanhallberg.restaurant.menu.dto.MenuItemResponse;

@Service
@Transactional(readOnly = true)
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    MenuService(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    public List<MenuCategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(MenuService::toResponse)
                .toList();
    }

    public PageResponse<MenuItemResponse> listItems(@Nullable Long categoryId, Pageable pageable) {
        var page = categoryId == null
                ? itemRepository.findByAvailableTrue(pageable)
                : itemRepository.findByAvailableTrueAndCategoryId(categoryId, pageable);
        return PageResponse.from(page.map(MenuService::toResponse));
    }

    public MenuItemResponse getItem(long id) {
        return itemRepository.findById(id)
                .map(MenuService::toResponse)
                .orElseThrow(() -> new NotFoundException("Menu item %d not found".formatted(id)));
    }

    private static MenuCategoryResponse toResponse(MenuCategory category) {
        return new MenuCategoryResponse(category.getId(), category.getName(), category.getDisplayOrder());
    }

    private static MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getCategory().getId(),
                item.getName(),
                item.getDescription(),
                item.getPriceCents(),
                item.getImageUrl(),
                item.isAvailable());
    }
}
