package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<Review> reviews = reviewStorage.getReviewsByFilmIdOrAll(filmId);
        Map<Long, Map<Long, Boolean>> usersRatingByReviews = reviewStorage.getRatingsByReviewsId();
        for (Review review : reviews) {
            if (usersRatingByReviews.get(review.getReviewId()) != null) {
                review.getUsersRating().putAll(usersRatingByReviews.get(review.getReviewId()));
                review.setUseful(review.getUseful());
            }
        }
        return reviews.stream()
                .sorted((r1, r2) -> (int) (r2.getUseful() - r1.getUseful()))
                .limit(count)
                .collect(Collectors.toList());
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
