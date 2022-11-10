package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
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
        String sqlQuery = "" +
                "INSERT INTO reviews (film_id, user_id, content, is_positive) " +
                "VALUES (?, ?, ?, ?)";
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
        String sqlQuery = "" +
                "SELECT r.review_id, r.film_id, r.user_id, r.content, r.is_positive, COUNT(rrp.review_id) - COUNT(rrn.review_id) AS useful " +
                "FROM reviews AS r " +
                "LEFT JOIN " +
                "   (SELECT review_id " +
                "    FROM reviews_rating " +
                "    WHERE is_positive = true) AS rrp ON r.review_id = rrp.review_id " +
                "LEFT JOIN " +
                "   (SELECT review_id " +
                "    FROM reviews_rating " +
                "    WHERE is_positive = false) AS rrn ON r.review_id = rrn.review_id " +
                "WHERE r.review_id = ? " +
                "GROUP BY r.review_id, r.film_id, r.user_id, r.content, r.is_positive";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToReview, id);
    }

    @Override
    public void deleteReviewById(Long id) {
        String sqlQuery = "" +
                "DELETE FROM reviews " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Review updateReviewById(Review review) {
        String sqlQuery = "" +
                "UPDATE reviews " +
                "SET content = ?, is_positive = ? " +
                "WHERE review_id = ?";
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
        String sqlQuery = "" +
                "SELECT r.review_id, r.film_id, r.user_id, r.content, r.is_positive, COUNT(rrp.review_id) - COUNT(rrn.review_id) AS useful " +
                "FROM reviews AS r " +
                "LEFT JOIN " +
                "   (SELECT review_id " +
                "    FROM reviews_rating " +
                "    WHERE is_positive = true) AS rrp ON r.review_id = rrp.review_id " +
                "LEFT JOIN " +
                "   (SELECT review_id " +
                "    FROM reviews_rating " +
                "    WHERE is_positive = false) AS rrn ON r.review_id = rrn.review_id ";
        if (filmId != null && filmId > 0) {
            sqlQuery = sqlQuery + " WHERE film_id = " + filmId;
        }
        sqlQuery = sqlQuery + "" +
                "GROUP BY r.review_id, r.film_id, r.user_id, r.content, r.is_positive " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToReview, count);
    }

    @Override
    public void addLikeToReview(Long id, Long userId) {
        String sqlQuery = "" +
                "INSERT INTO reviews_rating (review_id, user_id, is_positive) " +
                "VALUES (?, ?, true)";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void addDislikeToReview(Long id, Long userId) {
        String sqlQuery = "" +
                "INSERT INTO reviews_rating (review_id, user_id, is_positive) " +
                "VALUES (?, ?, false)";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void deleteLikeOrDislikeToReview(Long id, Long userId) {
        String sqlQuery = "" +
                "DELETE FROM reviews_rating " +
                "WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return new Review(
                rs.getLong("review_id"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("useful")
        );
    }
}
