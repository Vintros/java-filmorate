package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Primary
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public DbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        String sqlQuery = "insert into users (name, email, login, birthday) values (?, ?, ?, ?)";
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
        String sqlQuery = "update users set name = ?, email = ?, login = ?, birthday = ?" +
                "where user_id = ?";
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
        String sqlQuery = "select user_id, name, email, login, birthday from users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long friendId) {
        String sqlQuery = "select user_id, name, email, login, birthday from users where user_id in (" +
                "select friend_user_id from friends where ? = user_id) and user_id in (" +
                "select friend_user_id from friends where ? = user_id)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, id, friendId);
    }

    @Override
    public void removeUserById(Long id) {
        String sqlQuery = "delete from users where user_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public User getUserById(Long id) {
        String sqlQuery = "select user_id, name, email, login, birthday from users where user_id = ?";
        User user = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, id);
        sqlQuery = "select user_id, name, email, login, birthday from users where user_id in " +
                "(select friend_user_id from friends where user_id = ?)";
        List<User> friendsId = jdbcTemplate.query(sqlQuery, this::mapRowToUser, id);
        user.getFriends().addAll(friendsId);
        return user;
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        String sqlQuery = "insert into friends (user_id, friend_user_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, id, friendId);
    }

    @Override
    public void removeFriend(Long id, Long friendId) {
        String sqlQuery = "delete from friends where user_id = ? and friend_user_id = ?";
        jdbcTemplate.update(sqlQuery, id, friendId);
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
