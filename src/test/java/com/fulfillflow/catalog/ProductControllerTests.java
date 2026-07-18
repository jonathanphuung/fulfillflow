package com.fulfillflow.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndReturnsProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": " bin-100 ",
                                  "name": "Storage Bin",
                                  "description": "A durable picking bin",
                                  "initialQuantity": 24
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.sku").value("BIN-100"))
                .andExpect(jsonPath("$.name").value("Storage Bin"))
                .andExpect(jsonPath("$.quantityOnHand").value(24))
                .andExpect(jsonPath("$.quantityReserved").value(0));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("BIN-100"));
    }

    @Test
    void rejectsDuplicateSku() throws Exception {
        var request = """
                {"sku":"ITEM-1","name":"First item","initialQuantity":1}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A product with SKU ITEM-1 already exists"));
    }

    @Test
    void validatesProductInput() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","name":"","initialQuantity":-1}
                                """))
                .andExpect(status().isBadRequest());
    }
}
