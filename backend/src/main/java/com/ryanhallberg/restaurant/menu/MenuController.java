package com.ryanhallberg.restaurant.menu;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ryanhallberg.restaurant.common.web.PageResponse;
import com.ryanhallberg.restaurant.menu.dto.CategoryRequest;
import com.ryanhallberg.restaurant.menu.dto.MenuCategoryResponse;
import com.ryanhallberg.restaurant.menu.dto.MenuItemRequest;
import com.ryanhallberg.restaurant.menu.dto.MenuItemResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/menu")
@Tag(name = "Menu", description = "Public menu browsing and admin management")
public class MenuController {

    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

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
    @Operation(summary = "List menu items, optionally filtered by category; admins also see unavailable items")
    public PageResponse<MenuItemResponse> listItems(
            @RequestParam(required = false) Long categoryId,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable,
            Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().contains(ROLE_ADMIN);
        return menuService.listItems(categoryId, admin, pageable);
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get a single menu item; admins can also fetch hidden items")
    public MenuItemResponse getItem(@PathVariable long id, Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().contains(ROLE_ADMIN);
        return menuService.getItem(id, admin);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a category (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public MenuCategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return menuService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update a category (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public MenuCategoryResponse updateCategory(@PathVariable long id, @Valid @RequestBody CategoryRequest request) {
        return menuService.updateCategory(id, request);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a menu item (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public MenuItemResponse createItem(@Valid @RequestBody MenuItemRequest request) {
        return menuService.createItem(request);
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update a menu item (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public MenuItemResponse updateItem(@PathVariable long id, @Valid @RequestBody MenuItemRequest request) {
        return menuService.updateItem(id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a menu item (admin); prefer toggling available=false to preserve history",
            security = @SecurityRequirement(name = "bearerAuth"))
    public void deleteItem(@PathVariable long id) {
        menuService.deleteItem(id);
    }
}
