package com.fulfillflow;

import com.fulfillflow.catalog.CreateProductRequest;
import com.fulfillflow.catalog.ProductResponse;
import com.fulfillflow.catalog.ProductService;
import com.fulfillflow.order.CreateOrderRequest;
import com.fulfillflow.order.OrderService;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fulfillflow.bootstrap.demo-data-enabled", havingValue = "true")
class DemoDataBootstrap implements ApplicationRunner {
    private final ProductService products;
    private final OrderService orders;

    DemoDataBootstrap(ProductService products, OrderService orders) {
        this.products = products;
        this.orders = orders;
    }

    @Override
    public void run(ApplicationArguments args) {
        var keyboard = ensureProduct("KEYBOARD-001", "Mechanical Keyboard", 25);
        var mouse = ensureProduct("MOUSE-001", "Wireless Mouse", 40);
        var headset = ensureProduct("HEADSET-001", "USB-C Headset", 18);

        ensureOrder("Avery Chen", keyboard, 2);
        ensureOrder("Jordan Rivera", mouse, 3);
        ensureOrder("Morgan Patel", headset, 1);
    }

    private ProductResponse ensureProduct(String sku, String name, int quantity) {
        return products.list().stream()
                .filter(product -> product.sku().equals(sku))
                .findFirst()
                .orElseGet(() -> products.create(new CreateProductRequest(
                        sku, name, "Portfolio demonstration inventory", quantity)));
    }

    private void ensureOrder(String customerName, ProductResponse product, int quantity) {
        if (!orders.existsForCustomer(customerName)) {
            orders.create(new CreateOrderRequest(customerName,
                    List.of(new CreateOrderRequest.Item(product.id(), quantity))));
        }
    }
}
