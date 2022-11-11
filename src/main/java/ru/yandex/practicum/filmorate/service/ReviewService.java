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
        filmStorage.checkFilmExists(review.getFilmId());
        userStorage.checkUserExists(review.getUserId());
        Review createdReview = reviewStorage.addReview(review);
        feedStorage.saveUserEvent(new Event(createdReview.getUserId(), createdReview.getReviewId(), "REVIEW", "ADD",
                new Date()));
        log.info("добавлен отзыв с id: {}, к фильму с id: {}", createdReview.getReviewId(), createdReview.getFilmId());
        return createdReview;
    }

    public Review getReviewById(Long id) {
        reviewStorage.checkReviewExists(id);
        log.info("запрошен отзыв с id: {}", id);
        return reviewStorage.getReviewById(id);
    }

    public void deleteReviewById(Long id) {
        Review ReviewToDelete = reviewStorage.getReviewById(id);
        reviewStorage.deleteReviewById(id);
        feedStorage.saveUserEvent(new Event(ReviewToDelete.getUserId(), ReviewToDelete.getReviewId(), "REVIEW", "REMOVE",
                new Date()));
        log.info("удален отзыв с id: {}", id);
    }

    public Review updateReviewById(Review review) {
        Review updatedReview = reviewStorage.updateReviewById(review);
        feedStorage.saveUserEvent(new Event(updatedReview.getUserId(), updatedReview.getReviewId(), "REVIEW", "UPDATE",
                new Date()));
        log.info("обновлен отзыв с id: {}", review.getReviewId());
        return updatedReview;
    }

    public List<Review> getReviewsByFilmIdOrAll(Long filmId, Long count) {
        log.info("запрошен список отзывов");
        return reviewStorage.getReviewsByFilmIdOrAll(filmId, count);
    }

    public void addLikeToReview(Long id, Long userId) {
        reviewStorage.checkReviewExists(id);
        userStorage.checkUserExists(userId);
        reviewStorage.addLikeToReview(id, userId);
        log.info("пользователь с id: {} поставил лайк отзыву с id: {}", userId, id);
    }

    public void addDislikeToReview(Long id, Long userId) {
        reviewStorage.checkReviewExists(id);
        userStorage.checkUserExists(userId);
        reviewStorage.addDislikeToReview(id, userId);
        log.info("пользователь с id: {} поставил дизлайк отзыву с id: {}", userId, id);
    }

    public void deleteLikeOrDislikeToReview(Long id, Long userId) {
        reviewStorage.checkReviewExists(id);
        userStorage.checkUserExists(userId);
        reviewStorage.deleteLikeOrDislikeToReview(id, userId);
        log.info("пользователь с id: {} поставил оценку отзыва с id: {}", userId, id);
    }
}
