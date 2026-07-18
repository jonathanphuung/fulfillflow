package com.fulfillflow.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
        jdbc.update("DELETE FROM outbox_events");
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

        org.assertj.core.api.Assertions.assertThat(
                jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE event_type = 'order.created'", Integer.class))
                .isEqualTo(1);
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
        org.assertj.core.api.Assertions.assertThat(
                jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class))
                .isZero();
    }

    @Test
    void completesPickingWorkflowAndConsumesStock() throws Exception {
        var picked = products.saveAndFlush(new Product("PICKED", "Picked Item", null, 6));
        var missing = products.saveAndFlush(new Product("MISSING", "Missing Item", null, 4));
        var created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Sam Rivera","items":[
                                  {"productId":"%s","quantity":2},
                                  {"productId":"%s","quantity":1}
                                ]}
                                """.formatted(picked.getId(), missing.getId())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(created, "$.id");
        String pickedItemId = JsonPath.read(created, "$.items[0].id");
        String missingItemId = JsonPath.read(created, "$.items[1].id");

        mockMvc.perform(post("/api/orders/{id}/start", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKING"));
        mockMvc.perform(post("/api/orders/{orderId}/items/{itemId}/pick", orderId, pickedItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].status").value("PICKED"));
        mockMvc.perform(post("/api/orders/{orderId}/items/{itemId}/unavailable", orderId, missingItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[1].status").value("UNAVAILABLE"));
        mockMvc.perform(post("/api/orders/{id}/complete", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/inventory/{id}", picked.getId()))
                .andExpect(jsonPath("$.quantityOnHand").value(4))
                .andExpect(jsonPath("$.quantityReserved").value(0));
        mockMvc.perform(get("/api/inventory/{id}", missing.getId()))
                .andExpect(jsonPath("$.quantityOnHand").value(4))
                .andExpect(jsonPath("$.quantityReserved").value(0));
    }

    @Test
    void rejectsCompletingOrderWithUnresolvedItems() throws Exception {
        var product = products.saveAndFlush(new Product("PENDING-ITEM", "Pending Item", null, 2));
        var created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Alex Chen","items":[
                                  {"productId":"%s","quantity":1}
                                ]}
                                """.formatted(product.getId())))
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(created, "$.id");

        mockMvc.perform(post("/api/orders/{id}/start", orderId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/orders/{id}/complete", orderId))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelsOrderAndReleasesReservedStock() throws Exception {
        var product = products.saveAndFlush(new Product("CANCEL-ITEM", "Cancelled Item", null, 3));
        var created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerName":"Taylor Park","items":[
                                  {"productId":"%s","quantity":2}
                                ]}
                                """.formatted(product.getId())))
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(created, "$.id");

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.items[0].status").value("CANCELLED"));
        mockMvc.perform(get("/api/inventory/{id}", product.getId()))
                .andExpect(jsonPath("$.quantityOnHand").value(3))
                .andExpect(jsonPath("$.quantityReserved").value(0));
    }
}
