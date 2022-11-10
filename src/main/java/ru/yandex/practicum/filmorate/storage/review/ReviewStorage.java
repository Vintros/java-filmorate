package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    /**
     * Метод добавляет отзыв на фильм
     * в хранилище.
     *
     * @param review отзыв.
     * @return Добавленный объект отзыва.
     */
    Review addReview(Review review);

    /**
     * Метод возвращает отзыв на фильм по
     * его идентификатору.
     *
     * @param id идентификатор отзыва.
     * @return Отзыв, принадлежащий
     * идентификатору.
     */
    Review getReviewById(Long id);

    /**
     * Метод удалят отзыв на фильм, по его
     * идентификатору.
     *
     * @param id идентификатор отзыва.
     */
    void deleteReviewById(Long id);

    /**
     * Метод обновляет отзыв на фильм.
     *
     * @param review отзыв.
     * @return Обновлённый отзыв.
     */
    Review updateReviewById(Review review);

    /**
     * Метод возвращает список отсортированных по
     * полезности отзывов для всех фильмов или для
     * конкретного фильма, в случае если передан
     * идентификатор фильма.
     *
     * @param filmId идентификатор фильма.
     * @param count  размер списка.
     * @return Список популярных по полезности отзывов
     * для разных фильмов или для конкретного фильма.
     */
    List<Review> getReviewsByFilmIdOrAll(Long filmId, Long count);

    /**
     * Метод добавляет лайк отзыву.
     *
     * @param id     идентификатор отзыва.
     * @param userId идентификатор пользователя,
     *               поставившего лайк.
     */
    void addLikeToReview(Long id, Long userId);

    /**
     * Метод добавляет дизлайк отзыву.
     *
     * @param id     идентификатор отзыва.
     * @param userId идентификатор пользователя,
     *               поставившего дизлайк.
     */
    void addDislikeToReview(Long id, Long userId);

    /**
     * Метод удаляет отзыв пользователя.
     *
     * @param id     идентификатор отзыва.
     * @param userId идентификатор пользователя,
     *               оставившего отзыв.
     */
    void deleteLikeOrDislikeToReview(Long id, Long userId);
}
