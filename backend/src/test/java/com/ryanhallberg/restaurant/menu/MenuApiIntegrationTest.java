package com.ryanhallberg.restaurant.menu;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import com.ryanhallberg.restaurant.TestcontainersConfiguration;

/**
 * Full-stack happy path against a real Postgres container: Flyway migrations
 * (schema + seed) actually run, so these tests also validate the SQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional // each test rolls back: the container (and its data) is shared across classes
class MenuApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void categoriesAreListedInDisplayOrder() throws Exception {
        mockMvc.perform(get("/api/v1/menu/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Starters"))
                .andExpect(jsonPath("$[4].name").value("Drinks"));
    }

    @Test
    void itemsPageContainsOnlyAvailableSeedData() throws Exception {
        mockMvc.perform(get("/api/v1/menu/items").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.content[*].available").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    @Test
    void itemsCanBeFilteredByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/menu/items").param("categoryId", "1").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].categoryId")
                        .value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(1))));
    }

    @Test
    void singleItemIsReturnedWithFullShape() throws Exception {
        mockMvc.perform(get("/api/v1/menu/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Charred Shishito Peppers"))
                .andExpect(jsonPath("$.priceCents").value(900))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void unknownItemYieldsProblemDetail404() throws Exception {
        mockMvc.perform(get("/api/v1/menu/items/99999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    void hiddenItemIs404ForAnonymousButVisibleToAdmin() throws Exception {
        // Item 1 exists and is available; hide it as admin, then confirm the
        // public single-item endpoint 404s while an admin can still fetch it.
        var updateBody = """
                {"categoryId": 1, "name": "Charred Shishito Peppers", "description": "hidden now",
                 "priceCents": 900, "imageUrl": null, "available": false}
                """;
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/menu/items/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/menu/items/1").with(anonymous()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/menu/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}
