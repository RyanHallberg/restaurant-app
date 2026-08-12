package com.ryanhallberg.restaurant.menu;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.menu.dto.MenuCategoryResponse;
import com.ryanhallberg.restaurant.menu.dto.MenuItemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/menu")
@Tag(name = "Menu", description = "Public menu browsing")
public class MenuController {

    private final MenuService menuService;

    MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/categories")
    @Operation(summary = "List menu categories in display order")
    public List<MenuCategoryResponse> listCategories() {
        return menuService.listCategories();
    }

    @GetMapping("/items")
    @Operation(summary = "List available menu items, optionally filtered by category")
    public PageResponse<MenuItemResponse> listItems(
            @RequestParam(required = false) Long categoryId,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return menuService.listItems(categoryId, pageable);
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get a single menu item")
    public MenuItemResponse getItem(@PathVariable long id) {
        return menuService.getItem(id);
    }
}
