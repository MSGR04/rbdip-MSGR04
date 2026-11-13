package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderLineResolver {

    private final ProductRepository productRepository;
    private final OrderRequestValidator validator;

    public OrderLineResolver(ProductRepository productRepository, OrderRequestValidator validator) {
        this.productRepository = productRepository;
        this.validator = validator;
    }

    public List<OrderLine> resolve(List<CreateOrderRequest.Item> items) {
        List<OrderLine> lines = new ArrayList<>(items.size());
        for (CreateOrderRequest.Item item : items) {
            Product product = findProduct(item.productId());
            int quantity = validator.resolveQuantity(item.quantity());
            lines.add(new OrderLine(product, quantity));
        }
        return lines;
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product " + productId + " not found"));
    }
}
