package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.*;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }


    public Review addReview(Review review) {
        validateFilm(review.getFilmId());
        validateUser(review.getUserId());
        return reviewStorage.addReview(review);
    }

    public Review getReviewById(Long id) {
        validateReview(id);
        return reviewStorage.getReviewById(id);
    }

    public void deleteReviewById(Long id) {
        reviewStorage.deleteReviewById(id);
    }

    public Review updateReviewById(Review review) {
        return reviewStorage.updateReviewById(review);
    }

    public List<Review> getReviewsByFilmIdOrAll(Long filmId, Long count) {
        return reviewStorage.getReviewsByFilmIdOrAll(filmId, count);
    }

    public void addLikeToReview(Long id, Long userId) {
        validateReview(id);
        validateUser(userId);
        reviewStorage.addLikeToReview(id, userId);
    }

    public void addDislikeToReview(Long id, Long userId) {
        validateReview(id);
        validateUser(userId);
        reviewStorage.addDislikeToReview(id, userId);
    }

    public void deleteLikeOrDislikeToReview(Long id, Long userId) {
        validateReview(id);
        validateUser(userId);
        reviewStorage.deleteLikeOrDislikeToReview(id, userId);
    }
}
