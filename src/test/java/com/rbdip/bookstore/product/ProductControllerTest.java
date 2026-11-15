package com.rbdip.bookstore.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductControllerTest {

    private ProductRepository productRepository;
    private ProductController controller;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        controller = new ProductController(productRepository);
    }

    @Test
    void createProductReturnsSavedIdAndName() {
        Product saved = mock(Product.class);
        when(saved.getId()).thenReturn(1L);
        when(saved.getName()).thenReturn("Clean Code");
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Map<String, Object> response = controller.createProduct(
                Map.of("name", "Clean Code", "price", "35.00", "description", "book"));

        assertThat(response).containsEntry("name", "Clean Code").containsEntry("id", 1L);
    }

    @Test
    void listProductsMapsFieldsToStrings() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Refactoring");
        when(product.getPrice()).thenReturn(new BigDecimal("40.50"));
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Map<String, Object>> products = controller.listProducts();

        assertThat(products).containsExactly(
                Map.of("id", 1L, "name", "Refactoring", "price", "40.50"));
    }

    @Test
    void listProductsIsEmptyWhenRepositoryIsEmpty() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertThat(controller.listProducts()).isEmpty();
    }
}
