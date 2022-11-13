package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FeedStorage feedStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Review addReview(Review review) {
        filmStorage.checkFilmExistsById(review.getFilmId());
        userStorage.checkUserExistsById(review.getUserId());
        Review createdReview = reviewStorage.addReview(review);
        feedStorage.saveUserEvent(new Event(createdReview.getUserId(), createdReview.getReviewId(), "REVIEW", "ADD",
                new Date()));
        log.info("A review with id: {} is added to the movie with id: {}", createdReview.getReviewId(), createdReview.getFilmId());
        return createdReview;
    }

    public Review getReviewById(Long id) {
        reviewStorage.checkReviewExistsById(id);
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
        reviewStorage.checkReviewExistsById(id);
        userStorage.checkUserExistsById(userId);
        reviewStorage.addLikeToReview(id, userId);
        log.info("User with id: {} liked the review with id: {}", userId, id);
    }

    public void addDislikeToReview(Long id, Long userId) {
        reviewStorage.checkReviewExistsById(id);
        userStorage.checkUserExistsById(userId);
        reviewStorage.addDislikeToReview(id, userId);
        log.info("User with id: {} disliked the review with id: {}", userId, id);
    }

    public void deleteLikeOrDislikeToReview(Long id, Long userId) {
        reviewStorage.checkReviewExistsById(id);
        userStorage.checkUserExistsById(userId);
        reviewStorage.deleteLikeOrDislikeToReview(id, userId);
        log.info("User with id: {} deleted the review rating with id: {}", userId, id);
    }
}
