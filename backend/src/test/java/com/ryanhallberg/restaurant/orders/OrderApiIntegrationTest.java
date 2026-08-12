package com.ryanhallberg.restaurant.orders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import com.ryanhallberg.restaurant.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional // each test rolls back: the container (and its data) is shared across classes
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndGetToken(String email) throws Exception {
        var response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "s3cretpass", "fullName": "Test User"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asString();
    }

    private String adminToken() throws Exception {
        var response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@porkfiction.example", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asString();
    }

    private static String orderBody(String cardNumber) {
        // Seed items: id 1 = Charred Shishito Peppers (900), id 2 = Crispy Calamari (1400).
        return """
                {"items": [{"menuItemId": 1, "quantity": 2}, {"menuItemId": 2, "quantity": 1}],
                 "payment": {"cardNumber": "%s", "expiry": "12/30", "cvc": "123"}}
                """.formatted(cardNumber);
    }

    @Test
    void orderingRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody("4111111111111111")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void placeOrderRepricesServerSideAndSnapshotsItems() throws Exception {
        var token = registerAndGetToken("orderer@example.com");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(orderBody("4111111111111111")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalCents").value(900 * 2 + 1400))
                .andExpect(jsonPath("$.paymentReference").isNotEmpty())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].itemName").value("Charred Shishito Peppers"));

        mockMvc.perform(get("/api/v1/orders/my").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void declinedCardYields402AndNoOrder() throws Exception {
        var token = registerAndGetToken("declined@example.com");

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(orderBody("4000000000000002")))
                .andExpect(status().isPaymentRequired())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Your card was declined"));

        mockMvc.perform(get("/api/v1/orders/my").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void ownershipIsEnforcedOnSingleOrderFetch() throws Exception {
        var owner = registerAndGetToken("owner-o@example.com");
        var created = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + owner)
                        .content(orderBody("4111111111111111")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        var stranger = registerAndGetToken("stranger-o@example.com");
        mockMvc.perform(get("/api/v1/orders/" + id).header("Authorization", "Bearer " + stranger))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/orders/" + id).header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void adminAdvancesOrderThroughItsLifecycle() throws Exception {
        var customer = registerAndGetToken("lifecycle@example.com");
        var created = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + customer)
                        .content(orderBody("4111111111111111")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();
        var admin = adminToken();

        // Customers cannot advance orders.
        mockMvc.perform(patch("/api/v1/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + customer)
                        .content("{\"status\": \"PREPARING\"}"))
                .andExpect(status().isForbidden());

        // Illegal jump PLACED -> COMPLETED.
        mockMvc.perform(patch("/api/v1/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + admin)
                        .content("{\"status\": \"COMPLETED\"}"))
                .andExpect(status().isConflict());

        for (var status : new String[] { "PREPARING", "READY", "COMPLETED" }) {
            mockMvc.perform(patch("/api/v1/orders/" + id + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + admin)
                            .content("{\"status\": \"%s\"}".formatted(status)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status));
        }

        // The customer sees the final state.
        mockMvc.perform(get("/api/v1/orders/" + id).header("Authorization", "Bearer " + customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
