package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Date;
import java.util.List;

import static ru.yandex.practicum.filmorate.validator.Validator.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FeedStorage feedStorage;

    public Review addReview(Review review) {
        validateFilm(review.getFilmId());
        validateUser(review.getUserId());
        Review createdReview = reviewStorage.addReview(review);
        feedStorage.saveUserEvent(new Event(createdReview.getUserId(), createdReview.getReviewId(), "REVIEW", "ADD",
                new Date()));
        log.info("A review with id: {} is added to the movie with id: {}", createdReview.getReviewId(), createdReview.getFilmId());
        return createdReview;
    }

    public Review getReviewById(Long id) {
        validateReview(id);
        log.info("Review with id: {} is requested", id);
        return reviewStorage.getReviewById(id);
    }

    public void deleteReviewById(Long id) {
        Review ReviewToDelete = reviewStorage.getReviewById(id);
        reviewStorage.deleteReviewById(id);
        feedStorage.saveUserEvent(new Event(ReviewToDelete.getUserId(), ReviewToDelete.getReviewId(), "REVIEW", "REMOVE",
                new Date()));
        log.info("Review with id: {} is deleted", id);
    }

    public Review updateReviewById(Review review) {
        Review updatedReview = reviewStorage.updateReviewById(review);
        feedStorage.saveUserEvent(new Event(updatedReview.getUserId(), updatedReview.getReviewId(), "REVIEW", "UPDATE",
                new Date()));
        log.info("Review with id: {} is updated", review.getReviewId());
        return updatedReview;
    }

    public List<Review> getReviewsByFilmIdOrAll(Long filmId, Long count) {
        log.info("A list of reviews is requested");
        return reviewStorage.getReviewsByFilmIdOrAll(filmId, count);
    }

    public void addLikeToReview(Long id, Long userId) {
        validateReview(id);
        validateUser(userId);
        reviewStorage.addLikeToReview(id, userId);
        log.info("User with id: {} liked the review with id: {}", userId, id);
    }

    public void addDislikeToReview(Long id, Long userId) {
        validateReview(id);
        validateUser(userId);
        reviewStorage.addDislikeToReview(id, userId);
        log.info("User with id: {} disliked the review with id: {}", userId, id);
    }

    public void deleteLikeOrDislikeToReview(Long id, Long userId) {
        validateReview(id);
        validateUser(userId);
        reviewStorage.deleteLikeOrDislikeToReview(id, userId);
        log.info("User with id: {} deleted the review rating with id: {}", userId, id);
    }
}
