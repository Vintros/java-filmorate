package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review getReviewById(Long id);

    void deleteReviewById(Long id);

    Review updateReviewById(Review review);

    List<Review> getReviewsByFilmIdOrAll(Long filmId, Long count);

    void addLikeToReview(Long id, Long userId);

    void addDislikeToReview(Long id, Long userId);

    void deleteLikeOrDislikeToReview(Long id, Long userId);
}
