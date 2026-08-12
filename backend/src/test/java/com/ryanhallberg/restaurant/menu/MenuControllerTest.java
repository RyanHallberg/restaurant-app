package com.ryanhallberg.restaurant.menu;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ryanhallberg.restaurant.common.config.SecurityConfig;
import com.ryanhallberg.restaurant.common.error.NotFoundException;
import com.ryanhallberg.restaurant.menu.dto.MenuCategoryResponse;

/**
 * Web slice: verifies the security stub's route rules and Problem Details
 * mapping without a database.
 */
@WebMvcTest(MenuController.class)
@Import(SecurityConfig.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Test
    void menuReadsArePublic() throws Exception {
        when(menuService.listCategories())
                .thenReturn(List.of(new MenuCategoryResponse(1L, "Starters", 1)));

        mockMvc.perform(get("/api/v1/menu/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void missingItemMapsToProblemDetail404() throws Exception {
        when(menuService.getItem(anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Menu item 42 not found"));

        mockMvc.perform(get("/api/v1/menu/items/42"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void nonPublicRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isUnauthorized());
    }
}
