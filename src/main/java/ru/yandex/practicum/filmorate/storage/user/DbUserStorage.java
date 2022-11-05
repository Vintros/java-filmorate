package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;

    public DbUserStorage(JdbcTemplate jdbcTemplate, FilmStorage filmStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = filmStorage;
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

    @Override
    public List<Film> getRecommendations(Long id) {
        System.out.println("Gettin recomendations!");
        String sqlQuery = "SELECT USER_ID, FILM_ID FROM LIKES GROUP BY USER_ID, FILM_ID";

        // Получаем список Entry с id_user (key) и id_film (value)
        List<Map.Entry<Long, Long>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntry);

        // Составляем мапу данных для алгоритма
        Map<Long, ArrayList<Long>> data = getDataMap(dataList);

        // Ищем пользователя с которым имеется максимальное количество перечений
        Long mostIntersectionsUserId = getMostIntersectionsUserId(id, data);

        // Получаем список фильмов-рекомендаций
        List<Film> result = new ArrayList<>();
        for (Long otherFilmId : data.get(mostIntersectionsUserId)) {
            if (!data.get(id).contains(otherFilmId)) {
                result.add(filmStorage.getFilmById(otherFilmId));
            }
        }

        return result;
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

    private Map.Entry<Long, Long> mapRowToMapEntry(ResultSet rs, int rowNum) throws SQLException {
        Long filmId = rs.getLong("film_id");
        Long userId = rs.getLong("user_id");
        return new AbstractMap.SimpleEntry<>(userId, filmId);
    }

    private static Map<Long, ArrayList<Long>> getDataMap(List<Map.Entry<Long, Long>> dataList) {
        Map<Long, ArrayList<Long>> data = new HashMap<>();
        for (Map.Entry<Long, Long> entry : dataList) {
            Long entryKey = entry.getKey();
            if (data.containsKey(entryKey)) {
                Long value = entry.getValue();
                data.get(entryKey).add(value);
            } else {
                data.put(entryKey, new ArrayList<>(Collections.singletonList(entry.getValue())));
            }
        }
        return data;
    }

    private static Long getMostIntersectionsUserId(Long id, Map<Long, ArrayList<Long>> data) {
        Map<Long, Integer> frequency = new HashMap<>();
        for (Long userFilmId : data.get(id)) {
            for (Map.Entry<Long, ArrayList<Long>> user : data.entrySet()) {
                if (!user.getKey().equals(id)) {
                    if (user.getValue().contains(userFilmId)) {
                        if (frequency.containsKey(user.getKey())) {
                            frequency.replace(user.getKey(), frequency.get(user.getKey()) + 1);
                        } else {
                            frequency.put(user.getKey(), 1);
                        }
                    }
                }
            }
        }

        Optional<Map.Entry<Long, Integer>> mostIntersectionsUser = frequency.entrySet().stream().max(Map.Entry.comparingByValue());
        Long mostIntersectionsUserId = null;
        if (mostIntersectionsUser.isPresent()) {
            mostIntersectionsUserId = mostIntersectionsUser.get().getKey();
        }
        return mostIntersectionsUserId;
    }
}
