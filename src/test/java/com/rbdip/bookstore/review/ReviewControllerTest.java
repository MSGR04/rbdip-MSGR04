package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewControllerTest {

    private ReviewService reviewService;
    private ReviewController controller;

    @BeforeEach
    void setUp() {
        reviewService = mock(ReviewService.class);
        controller = new ReviewController(reviewService);
    }

    @Test
    void addReviewReturnsNewId() {
        Review review = mock(Review.class);
        when(review.getId()).thenReturn(10L);
        when(reviewService.addReview(1L, "Ivan", 5, "Great")).thenReturn(review);

        Map<String, Object> response = controller.addReview(
                1L, Map.of("authorName", "Ivan", "rating", 5, "comment", "Great"));

        assertThat(response).containsEntry("id", 10L);
    }

    @Test
    void listReviewsReplacesNullCommentWithEmptyString() {
        Review withComment = mock(Review.class);
        when(withComment.getAuthorName()).thenReturn("Ivan");
        when(withComment.getRating()).thenReturn(5);
        when(withComment.getComment()).thenReturn("Great");

        Review withoutComment = mock(Review.class);
        when(withoutComment.getAuthorName()).thenReturn("Anna");
        when(withoutComment.getRating()).thenReturn(4);
        when(withoutComment.getComment()).thenReturn(null);

        when(reviewService.listReviews(1L)).thenReturn(List.of(withComment, withoutComment));

        List<Map<String, Object>> reviews = controller.listReviews(1L);

        assertThat(reviews).containsExactly(
                Map.of("authorName", "Ivan", "rating", 5, "comment", "Great"),
                Map.of("authorName", "Anna", "rating", 4, "comment", ""));
    }

    @Test
    void listReviewsIsEmptyWhenThereAreNoReviews() {
        when(reviewService.listReviews(1L)).thenReturn(List.of());

        assertThat(controller.listReviews(1L)).isEmpty();
    }
}
