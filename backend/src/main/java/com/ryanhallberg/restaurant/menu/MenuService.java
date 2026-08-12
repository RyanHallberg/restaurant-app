package com.ryanhallberg.restaurant.menu;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ryanhallberg.restaurant.common.error.NotFoundException;
import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.menu.dto.CategoryRequest;
import com.ryanhallberg.restaurant.menu.dto.MenuCategoryResponse;
import com.ryanhallberg.restaurant.menu.dto.MenuItemRequest;
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

    /** Admins see unavailable items too; the public menu never does. */
    public PageResponse<MenuItemResponse> listItems(
            @Nullable Long categoryId, boolean includeUnavailable, Pageable pageable) {
        var page = includeUnavailable
                ? (categoryId == null
                        ? itemRepository.findAll(pageable)
                        : itemRepository.findByCategoryId(categoryId, pageable))
                : (categoryId == null
                        ? itemRepository.findByAvailableTrue(pageable)
                        : itemRepository.findByAvailableTrueAndCategoryId(categoryId, pageable));
        return PageResponse.from(page.map(MenuService::toResponse));
    }

    /** Cross-feature lookup for order pricing: only available items count. */
    public List<MenuItemResponse> listAvailableByIds(java.util.Collection<Long> ids) {
        return itemRepository.findAllById(ids).stream()
                .filter(MenuItem::isAvailable)
                .map(MenuService::toResponse)
                .toList();
    }

    public MenuItemResponse getItem(long id, boolean includeUnavailable) {
        return itemRepository.findById(id)
                // Non-admins must not reach a hidden item by addressing its id
                // directly — same availability gate as the list endpoint.
                .filter(item -> includeUnavailable || item.isAvailable())
                .map(MenuService::toResponse)
                .orElseThrow(() -> new NotFoundException("Menu item %d not found".formatted(id)));
    }

    @Transactional
    public MenuCategoryResponse createCategory(CategoryRequest request) {
        var category = categoryRepository.save(new MenuCategory(request.name(), request.displayOrder()));
        return toResponse(category);
    }

    @Transactional
    public MenuCategoryResponse updateCategory(long id, CategoryRequest request) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category %d not found".formatted(id)));
        category.update(request.name(), request.displayOrder());
        return toResponse(category);
    }

    @Transactional
    public MenuItemResponse createItem(MenuItemRequest request) {
        var item = itemRepository.save(new MenuItem(
                requireCategory(request.categoryId()),
                request.name(),
                request.description(),
                request.priceCents(),
                request.imageUrl(),
                request.available()));
        return toResponse(item);
    }

    @Transactional
    public MenuItemResponse updateItem(long id, MenuItemRequest request) {
        var item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu item %d not found".formatted(id)));
        item.update(
                requireCategory(request.categoryId()),
                request.name(),
                request.description(),
                request.priceCents(),
                request.imageUrl(),
                request.available());
        return toResponse(item);
    }

    @Transactional
    public void deleteItem(long id) {
        if (!itemRepository.existsById(id)) {
            throw new NotFoundException("Menu item %d not found".formatted(id));
        }
        itemRepository.deleteById(id);
    }

    private MenuCategory requireCategory(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category %d not found".formatted(categoryId)));
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
