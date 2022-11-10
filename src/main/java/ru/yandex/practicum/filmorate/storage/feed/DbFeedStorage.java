package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class DbFeedStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    public DbFeedStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveUserEvent(Event event) {
        String sqlQuery = "" +
                "INSERT INTO users_feed (user_id, entity_id, event_type, operation, event_time) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                event.getUserId(),
                event.getEntityId(),
                event.getEventType(),
                event.getOperation(),
                event.getTimestamp().getTime());
    }

    @Override
    public List<Event> getFeed(Long id) {
        String sqlQuery = "" +
                "SELECT event_id, user_id, entity_id, event_type, operation, event_time " +
                "FROM users_feed " +
                "WHERE user_id = ? " +
                "ORDER BY event_time";
        return jdbcTemplate.query(sqlQuery, this::mapRowToEvent, id);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return new Event(
                rs.getLong("event_id"),
                rs.getLong("user_id"),
                rs.getLong("entity_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                new Date(rs.getLong("event_time"))
        );
    }
}
