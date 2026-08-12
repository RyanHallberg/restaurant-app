package com.ryanhallberg.restaurant.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import com.ryanhallberg.restaurant.TestcontainersConfiguration;

/**
 * Full auth story against real Postgres: register -> login -> me, the seeded
 * admin's powers, and the 401/403 matrix on protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional // each test rolls back: the container (and its data) is shared across classes
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenFor(String email, String password) throws Exception {
        var response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asString();
    }

    @Test
    void registerLoginAndMeRoundTrip() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ada@example.com", "password": "s3cretpass", "fullName": "Ada Lovelace"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));

        var token = tokenFor("ada@example.com", "s3cretpass");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.fullName").value("Ada Lovelace"));
    }

    @Test
    void duplicateRegistrationConflicts() throws Exception {
        var body = """
                {"email": "dupe@example.com", "password": "s3cretpass", "fullName": "Dupe"}
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordIs401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@sageandember.example", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void seededAdminCanCreateMenuItemsCustomerCannot() throws Exception {
        var adminToken = tokenFor("admin@sageandember.example", "admin123");

        var itemBody = """
                {"categoryId": 1, "name": "Test Special", "description": "Integration test dish",
                 "priceCents": 1500, "imageUrl": null, "available": true}
                """;

        mockMvc.perform(post("/api/v1/menu/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(itemBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Special"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "customer@example.com", "password": "s3cretpass", "fullName": "Customer"}
                        """)).andExpect(status().isCreated());
        var customerToken = tokenFor("customer@example.com", "s3cretpass");

        mockMvc.perform(post("/api/v1/menu/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + customerToken)
                        .content(itemBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/menu/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reservationOwnershipIsEnforced() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "owner@example.com", "password": "s3cretpass", "fullName": "Owner"}
                        """)).andExpect(status().isCreated());
        var ownerToken = tokenFor("owner@example.com", "s3cretpass");

        var created = mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + ownerToken)
                        .content("""
                                {"customerName": "Owner", "customerEmail": "owner@example.com",
                                 "customerPhone": "555-0100", "partySize": 2,
                                 "date": "%s", "time": "20:00"}
                                """.formatted(java.time.LocalDate.now().plusDays(40))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "stranger@example.com", "password": "s3cretpass", "fullName": "Stranger"}
                        """)).andExpect(status().isCreated());
        var strangerToken = tokenFor("stranger@example.com", "s3cretpass");

        mockMvc.perform(post("/api/v1/reservations/" + id + "/cancel")
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/reservations/" + id + "/cancel")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/reservations/my")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void adminListAndStatusTransitionsWork() throws Exception {
        var adminToken = tokenFor("admin@sageandember.example", "admin123");

        mockMvc.perform(get("/api/v1/reservations").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        var created = mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName": "Walk In", "customerEmail": "walkin@example.com",
                                 "customerPhone": "555-0100", "partySize": 2,
                                 "date": "%s", "time": "20:30"}
                                """.formatted(java.time.LocalDate.now().plusDays(41))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/reservations/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/v1/reservations/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content("{\"status\": \"CANCELLED\"}"))
                .andExpect(status().isConflict());
    }
}
