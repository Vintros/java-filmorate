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
        String sqlQuery = "select review_id, film_id, user_id, content, is_positive " +
                "from reviews where review_id = ?";
        Review review = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToReview, id);
        String sqlQueryRating = "select user_id, is_positive from reviews_rating where review_id = ?";
        Map<Long, Boolean> usersRating = jdbcTemplate.query(sqlQueryRating, this::mapRowToReviewRating, id);
        review.getUsersRating().putAll(usersRating);
        review.setUseful(review.getUseful());
        return review;
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
    public List<Review> getReviewsByFilmIdOrAll(Long filmId) {
        String sqlQuery = "select review_id, film_id, user_id, content, is_positive " +
                "from reviews";
        if (filmId != null && filmId > 0) {
            sqlQuery = sqlQuery + " where film_id = " + filmId;
        }
        return jdbcTemplate.query(sqlQuery, this::mapRowToReview);
    }

    @Override
    public Map<Long, Map<Long, Boolean>> getRatingsByReviewsId() {
        String sqlQuery = "select review_id, user_id, is_positive from reviews_rating";
        return jdbcTemplate.query(sqlQuery, this::mapRowToRatingByReviewsId);
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

    private Map<Long, Map<Long, Boolean>> mapRowToRatingByReviewsId(ResultSet rs) throws SQLException {
        Map<Long, Map<Long, Boolean>> ratingsByReviewsId = new LinkedHashMap<>();
        while (rs.next()) {
            Long reviewId = rs.getLong("review_id");
            ratingsByReviewsId.putIfAbsent(reviewId, new HashMap<>());
            Long userId = rs.getLong("user_id");
            Boolean isPositive = rs.getBoolean("is_positive");
            ratingsByReviewsId.get(reviewId).put(userId, isPositive);
        }
        return ratingsByReviewsId;
    }

    private Map<Long, Boolean> mapRowToReviewRating(ResultSet rs) throws SQLException {
        Map<Long, Boolean> usersRating = new HashMap<>();
        while (rs.next()) {
            Long userId = rs.getLong("user_id");
            Boolean isPositive = rs.getBoolean("is_positive");
            usersRating.put(userId, isPositive);
        }
        return usersRating;
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review(
                rs.getLong("review_id"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                null
        );
        return review;
    }
}
