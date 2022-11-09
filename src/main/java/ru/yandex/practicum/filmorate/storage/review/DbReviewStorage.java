package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UnknownReviewException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class DbReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    public DbReviewStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review addReview(Review review) {
        String sqlQuery = "insert into reviews (film_id, user_id, content, is_positive) " +
                "values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"review_id"});
            ps.setLong(1, review.getFilmId());
            ps.setLong(2, review.getUserId());
            ps.setString(3, review.getContent());
            ps.setBoolean(4, review.getIsPositive());
            return ps;
        }, keyHolder);
        return getReviewById((long) keyHolder.getKey());
    }

    @Override
    public Review getReviewById(Long id) {
        String sqlQuery = "select r.review_id, r.film_id, r.user_id, r.content, r.is_positive," +
                "count(rrp.review_id) - count(rrn.review_id) as useful " +
                "from reviews as r " +
                "left join (select review_id from reviews_rating where is_positive = true) as rrp " +
                "on r.review_id = rrp.review_id " +
                "left join (select review_id from reviews_rating where is_positive = false) as rrn " +
                "on r.review_id = rrn.review_id " +
                "where r.review_id = ? " +
                "group by r.review_id, r.film_id, r.user_id, r.content, r.is_positive;";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToReview, id);
    }

    @Override
    public void deleteReviewById(Long id) {
        String sqlQuery = "delete from reviews where review_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Review updateReviewById(Review review) {
        String sqlQuery = "update reviews set content = ?, is_positive = ? where review_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getReviewId());
            return ps;
        });
        return getReviewById(review.getReviewId());
    }

    @Override
    public List<Review> getReviewsByFilmIdOrAll(Long filmId, Long count) {
        String sqlQuery = "select r.review_id, r.film_id, r.user_id, r.content, r.is_positive, " +
                "count(rrp.review_id) - count(rrn.review_id) as useful " +
                "from reviews as r " +
                "left join (select review_id from reviews_rating where is_positive = true) as rrp " +
                "on r.review_id = rrp.review_id " +
                "left join (select review_id from reviews_rating where is_positive = false) as rrn " +
                "on r.review_id = rrn.review_id ";
        if (filmId != null && filmId > 0) {
            sqlQuery = sqlQuery + " where film_id = " + filmId;
        }
        sqlQuery = sqlQuery + "group by r.review_id, r.film_id, r.user_id, r.content, r.is_positive " +
                "order by useful desc " +
                "limit ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count);
    }

    @Override
    public void addLikeToReview(Long id, Long userId) {
        String sqlQuery = "insert into reviews_rating (review_id, user_id, is_positive) values (?, ?, true)";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void addDislikeToReview(Long id, Long userId) {
        String sqlQuery = "insert into reviews_rating (review_id, user_id, is_positive) values (?, ?, false)";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void deleteLikeOrDislikeToReview(Long id, Long userId) {
        String sqlQuery = "delete from reviews_rating where review_id = ? and user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void validateReview(Long id) {
        try {
            getReviewById(id);
        } catch (DataAccessException e) {
            throw new UnknownReviewException(String.format("Отзыв с id: %d не найден", id));
        }
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review(
                rs.getLong("review_id"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("useful")
        );
        return review;
    }
}
