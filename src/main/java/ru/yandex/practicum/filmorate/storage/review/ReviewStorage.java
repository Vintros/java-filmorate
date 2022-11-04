package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Map;

public interface ReviewStorage {
    Review addReview(Review review);

    Review getReviewById(Long id);

    void deleteReviewById(Long id);

    Review updateReviewById(Review review);

    List<Review> getReviewsByFilmIdOrAll(Long filmId);

    Map<Long, Map<Long, Boolean>> getRatingsByReviewsId();

    void addLikeToReview(Long id, Long userId);

    void addDislikeToReview(Long id, Long userId);

    void deleteLikeOrDislikeToReview(Long id, Long userId);
}