package com.fulfillflow.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "fulfillflow.security.enabled=true")
class SecurityIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwords;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void createUsers() {
        users.save(new AppUser("admin@test.com", passwords.encode("admin-password"), UserRole.ADMIN));
        users.save(new AppUser("worker@test.com", passwords.encode("worker-password"), UserRole.WORKER));
    }

    @AfterEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM fulfillment_tasks");
        jdbc.update("DELETE FROM outbox_events");
        jdbc.update("DELETE FROM order_items");
        jdbc.update("DELETE FROM orders");
        jdbc.update("DELETE FROM inventory_reservations");
        jdbc.update("DELETE FROM inventory");
        jdbc.update("DELETE FROM products");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SEC-1\",\"name\":\"Secure Item\",\"initialQuantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        var token = login("admin@test.com", "admin-password");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SEC-2\",\"name\":\"Admin Item\",\"initialQuantity\":2}"))
                .andExpect(status().isCreated());
    }

    @Test
    void workerCannotCreateProduct() throws Exception {
        var token = login("worker@test.com", "worker-password");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SEC-3\",\"name\":\"Worker Item\",\"initialQuantity\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginReturnsSignedToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"worker@test.com\",\"password\":\"worker-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void onlyAdminCanCreateWorkerAccounts() throws Exception {
        var workerToken = login("worker@test.com", "worker-password");
        var adminToken = login("admin@test.com", "admin-password");
        var request = """
                {"email":"new.worker@test.com","password":"strong-password","role":"WORKER"}
                """;

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("WORKER"));
    }

    private String login(String email, String password) throws Exception {
        var body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
