package com.fulfillflow.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository products;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM order_items");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM inventory_reservations");
        jdbc.update("DELETE FROM inventory");
        jdbc.update("DELETE FROM products");
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        cleanDatabase();
    }

    @Test
    void createsOrderAndReservesEveryItem() throws Exception {
        var tote = products.saveAndFlush(new Product("ORDER-TOTE", "Order Tote", null, 8));
        var label = products.saveAndFlush(new Product("LABEL-ROLL", "Label Roll", null, 20));

        var response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName":"Jordan Lee",
                                  "items":[
                                    {"productId":"%s","quantity":2},
                                    {"productId":"%s","quantity":5}
                                  ]
                                }
                                """.formatted(tote.getId(), label.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("READY_TO_PICK"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn().getResponse();

        var location = response.getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Jordan Lee"));

        mockMvc.perform(get("/api/inventory/{id}", tote.getId()))
                .andExpect(jsonPath("$.quantityReserved").value(2));
        mockMvc.perform(get("/api/inventory/{id}", label.getId()))
                .andExpect(jsonPath("$.quantityReserved").value(5));
    }

    @Test
    void rollsBackEntireOrderWhenOneItemIsUnavailable() throws Exception {
        var available = products.saveAndFlush(new Product("AVAILABLE", "Available Item", null, 10));
        var unavailable = products.saveAndFlush(new Product("UNAVAILABLE", "Unavailable Item", null, 1));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName":"Morgan Kim",
                                  "items":[
                                    {"productId":"%s","quantity":4},
                                    {"productId":"%s","quantity":2}
                                  ]
                                }
                                """.formatted(available.getId(), unavailable.getId())))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/inventory/{id}", available.getId()))
                .andExpect(jsonPath("$.quantityReserved").value(0));
    }
}
