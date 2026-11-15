package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.order.OrderItemRepository;
import com.rbdip.bookstore.order.OrderRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderItemRepository = mock(OrderItemRepository.class);
        reviewService = new ReviewService(reviewRepository, orderRepository, orderItemRepository);
    }

    @Test
    void replacesMissingAuthorWithAnonymous() {
        when(reviewRepository.save(org.mockito.ArgumentMatchers.any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review saved = reviewService.addReview(1L, null, 5, "Great");

        assertThat(saved.getAuthorName()).isEqualTo("anonymous");
        assertThat(saved.getRating()).isEqualTo(5);
        assertThat(saved.getComment()).isEqualTo("Great");

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        org.mockito.Mockito.verify(reviewRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthorName()).isEqualTo("anonymous");
    }

    @Test
    void keepsProvidedAuthorAndChecksPurchaseHistory() {
        when(orderRepository.findAll()).thenReturn(List.of(mock(com.rbdip.bookstore.order.Order.class)));
        when(orderItemRepository.findAll()).thenReturn(List.of(mock(com.rbdip.bookstore.order.OrderItem.class)));
        when(reviewRepository.save(org.mockito.ArgumentMatchers.any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review saved = reviewService.addReview(1L, "Ivan Petrov", 4, null);

        assertThat(saved.getAuthorName()).isEqualTo("Ivan Petrov");
        assertThat(saved.getProductId()).isEqualTo(1L);
        assertThat(saved.getComment()).isNull();
    }

    @Test
    void listsReviewsForProduct() {
        Review first = new Review(1L, "Ivan", 5, "Great");
        Review second = new Review(1L, "Anna", 3, "Okay");
        when(reviewRepository.findByProductId(1L)).thenReturn(List.of(first, second));

        assertThat(reviewService.listReviews(1L)).containsExactly(first, second);
    }
}
