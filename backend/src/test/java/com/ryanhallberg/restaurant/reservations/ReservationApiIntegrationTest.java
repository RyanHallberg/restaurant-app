package com.ryanhallberg.restaurant.reservations;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ryanhallberg.restaurant.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional // each test rolls back: the container (and its data) is shared across classes
class ReservationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String body(LocalDate date, String time, String email) {
        return """
                {
                  "customerName": "Ada Lovelace",
                  "customerEmail": "%s",
                  "customerPhone": "555-0100",
                  "partySize": 4,
                  "date": "%s",
                  "time": "%s"
                }
                """.formatted(email, date, time);
    }

    @Test
    void availabilityListsAllSlotsForAFutureDate() throws Exception {
        mockMvc.perform(get("/api/v1/reservations/availability")
                        .param("date", LocalDate.now().plusDays(20).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots.length()").value(22))
                .andExpect(jsonPath("$.slots[0].time").value("11:00"))
                .andExpect(jsonPath("$.slots[0].available").value(true));
    }

    @Test
    void bookingReturnsConfirmationAndConsumesCapacity() throws Exception {
        var date = LocalDate.now().plusDays(21);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(date, "19:00", "ada@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.confirmationCode").isNotEmpty())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Fill the remaining 9 tables in the 19:00 slot.
        for (int i = 0; i < 9; i++) {
            mockMvc.perform(post("/api/v1/reservations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body(date, "19:00", "guest" + i + "@example.com")))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/v1/reservations/availability")
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[?(@.time=='19:00')].available").value(false))
                .andExpect(jsonPath("$.slots[?(@.time=='19:30')].available").value(true));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(date, "19:00", "late@example.com")))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("No tables left")));
    }

    @Test
    void offGridTimeYields409() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(LocalDate.now().plusDays(22), "18:45", "ada@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRequestYields400WithFieldErrors() throws Exception {
        var invalid = """
                {
                  "customerName": "",
                  "customerEmail": "not-an-email",
                  "customerPhone": "555-0100",
                  "partySize": 0,
                  "date": "%s",
                  "time": "19:00"
                }
                """.formatted(LocalDate.now().plusDays(23));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.errors.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }
}
