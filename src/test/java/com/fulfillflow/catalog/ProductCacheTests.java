package com.fulfillflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class ProductCacheTests {
    @Autowired private ProductRepository products;
    @Autowired private ProductService productService;
    @Autowired private CacheManager caches;
    @Autowired private JdbcTemplate jdbc;

    @AfterEach
    void cleanDatabase() {
        caches.getCacheNames().forEach(name -> caches.getCache(name).clear());
        jdbc.update("DELETE FROM inventory");
        jdbc.update("DELETE FROM products");
    }

    @Test
    void cachesProductReads() {
        var product = products.saveAndFlush(new Product("CACHE-1", "Cached Name", null, 1));

        assertThat(productService.get(product.getId()).name()).isEqualTo("Cached Name");
        jdbc.update("UPDATE products SET name = 'Database Name' WHERE id = ?", product.getId());

        assertThat(productService.get(product.getId()).name()).isEqualTo("Cached Name");
        caches.getCache("product").evict(product.getId());
        assertThat(productService.get(product.getId()).name()).isEqualTo("Database Name");
    }
}
