package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownUserException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public DbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        String sqlQuery = "" +
                "INSERT INTO users (name, email, login, birthday) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, user.getBirthday());
            return ps;
        }, keyHolder);
        return getUserById((long) keyHolder.getKey());
    }

    @Override
    public User updateUser(User user) {
        String sqlQuery = "" +
                "UPDATE users " +
                "SET name = ?, email = ?, login = ?, birthday = ?" +
                "WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public List<User> getUsers() {
        String sqlQuery = "" +
                "SELECT user_id, name, email, login, birthday " +
                "FROM users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long friendId) {
        String sqlQuery = "" +
                "SELECT user_id, name, email, login, birthday " +
                "FROM users " +
                "WHERE user_id IN " +
                "   (SELECT friend_user_id " +
                "    FROM friends " +
                "    WHERE ? = user_id) AND user_id IN " +
                "   (SELECT friend_user_id " +
                "    FROM friends " +
                "    WHERE ? = user_id)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id, friendId);
    }

    @Override
    public User getUserById(Long id) {
        String sqlQuery = "" +
                "SELECT user_id, name, email, login, birthday " +
                "FROM users " +
                "WHERE user_id = ?";
        User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
        sqlQuery = "" +
                "SELECT user_id, name, email, login, birthday " +
                "FROM users " +
                "WHERE user_id IN " +
                "   (SELECT friend_user_id " +
                "    FROM friends " +
                "    WHERE user_id = ?)";
        List<User> friendsId = jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
        user.getFriends().addAll(friendsId);
        return user;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        String sqlQuery = "" +
                "INSERT INTO friends (user_id, friend_user_id) " +
                "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        String sqlQuery = "" +
                "DELETE FROM friends " +
                "WHERE user_id = ? AND friend_user_id = ?";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public void removeUserById(Long id) {
        String sqlQuery = "" +
                "DELETE FROM users " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public void validateUser(Long id) {
        try {
            getUserById(id);
        } catch (DataAccessException e) {
            throw new UnknownUserException(String.format("Пользователь с id: %d не найден", id));
        }
    }

    @Override
    public void checkUserNotExist(User user) {
        if (user.getId() == null) {
            return;
        }
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT user_id " +
                "   FROM users " +
                "   WHERE user_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (rs.getLong(1) == user.getId()) throw new ExistsException("Пользователь уже зарегистрирован");
        }, user.getId());
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getDate("birthday")
        );
    }
}
