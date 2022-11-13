package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownFilmException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQuery = "" +
                "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        if (!film.getGenres().isEmpty()) {
            addGenres(film.getGenres(), (long) keyHolder.getKey());
        }
        if (!film.getDirectors().isEmpty()) {
            addDirectors(film.getDirectors(), (long) keyHolder.getKey());
        }
        return getFilmByIdWithoutGenresAndDirectors((long) keyHolder.getKey());
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery = "" +
                "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?; " +
                "DELETE FROM genres " +
                "WHERE film_id = ?; " +
                "DELETE FROM directors " +
                "WHERE film_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            ps.setLong(6, film.getId());
            ps.setLong(7, film.getId());
            ps.setLong(8, film.getId());
            return ps;
        });
        addGenres(film.getGenres(), film.getId());
        addDirectors(film.getDirectors(), film.getId());
        return getFilmByIdWithoutGenresAndDirectors(film.getId());
    }

    @Override
    public void addLikeFilm(Long id, Long userId) {
        String sqlQuery = "" +
                "INSERT INTO likes (film_id, user_id) " +
                "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void removeLikeFilm(Long id, Long userId) {
        String sqlQuery = "" +
                "DELETE FROM likes " +
                "WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public List<Film> getFilmsWithoutGenresAndDirectors() {
        String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name, " +
                "       COUNT (l.user_id) rate " +
                "FROM films f " +
                "LEFT JOIN likes l on f.film_id = l.film_id " +
                "LEFT JOIN mpa ON f.mpa_id = mpa.mpa_id " +
                "GROUP BY f.film_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film getFilmByIdWithoutGenresAndDirectors(Long id) {
        String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name, " +
                "       COUNT (l.user_id) rate " +
                "FROM films f " +
                "LEFT JOIN likes l on f.film_id = l.film_id " +
                "JOIN mpa ON f.mpa_id = mpa.mpa_id " +
                "WHERE f.film_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
    }

    @Override
    public void removeFilmById(Long id) {
        String sqlQuery = "" +
                "DELETE FROM films " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public List<Film> getFilmsByDirectorWithoutGenresAndDirectors(Long directorId, String sortBy) {
        String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name, " +
                "       COUNT (l.user_id) rate " +
                "FROM films f " +
                "LEFT OUTER JOIN likes l on f.film_id = l.film_id " +
                "JOIN mpa ON f.mpa_id = mpa.mpa_id " +
                "WHERE f.film_id IN " +
                "   (SELECT film_id " +
                "    FROM directors " +
                "    WHERE director_id = ?) " +
                "GROUP BY f.film_id ";
        if ("likes".equals(sortBy)) {
            sqlQuery += "ORDER BY COUNT (l.user_id) DESC";
        } else if ("year".equals(sortBy)) {
            sqlQuery += "ORDER BY f.release_date";
        }
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, directorId);
    }

    @Override
    public List<Map.Entry<Long, Long>> getEntriesUserIdLikedFilmId() {
        final String sqlQuery = "" +
                "SELECT user_id, film_id " +
                "FROM likes " +
                "GROUP BY user_id, film_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMapEntry);
    }

    @Override
    public List<Film> getListPopularFilm(long count) {
        final String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, " +
                "       COUNT(l.user_id) rate " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY rate DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    @Override
    public List<Film> getListPopularFilmSortedByYear(int count, int year) {
        final String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, " +
                "       COUNT(l.user_id) rate " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE YEAR(f.release_date) = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY rate DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, year, count));
        return new ArrayList<>(films);
    }

    @Override
    public List<Film> getListPopularFilmSortedByGenre(int count, long genreId) {
        final String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, g.genre_id, " +
                "       COUNT (l.user_id) rate " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN genres AS g ON f.film_id = g.film_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE g.genre_id = ? " +
                "GROUP BY f.film_id, g.genre_id " +
                "ORDER BY rate DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, genreId, count));
        return new ArrayList<>(films);
    }

    @Override
    public List<Film> findPopularFilmSortedByGenreAndYear(int count, long genreId, int year) {
        final String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, g.genre_id," +
                "       COUNT (l.user_id) rate " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN genres AS g ON f.film_id = g.film_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE g.genre_id = ? = ? AND YEAR(f.release_date) = ? " +
                "GROUP BY f.film_id, g.genre_id " +
                "ORDER BY rate DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, genreId, year, count));
        return new ArrayList<>(films);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, " +
                "       COUNT (l.user_id) rate " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
                "JOIN likes AS l on l.film_id = f.film_id " +
                "WHERE f.film_id IN " +
                "   (SELECT film_id " +
                "    FROM likes " +
                "    WHERE user_id = ? " +
                "    INTERSECT SELECT film_id " +
                "    FROM likes " +
                "    WHERE user_id = ?) " +
                "GROUP BY f.film_id " +
                "ORDER BY rate DESC";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public List<Map.Entry<Long, String>> searchFilmsWithoutGenresAndDirectorsByTitle(String query) {
        final String sqlQuery = "" +
                "SELECT name, film_id " +
                "FROM films";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryFilmIdFilmName);
    }

    @Override
    public List<Map.Entry<Long, String>> searchFilmsWithoutGenresAndDirectorsByDirector(String query) {
        final String sqlQuery = "" +
                "SELECT dir.name AS director_name, dirs.film_id " +
                "FROM directors AS dirs " +
                "LEFT JOIN director AS dir ON dirs.director_id = dir.director_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryFilmIdDirectorName);
    }

    @Override
    public void checkFilmExistsById(Long id) {
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT film_id " +
                "   FROM films " +
                "   WHERE film_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (!rs.getBoolean(1)) throw new UnknownFilmException(String.format("Film with id: %d is not found", id));
        }, id);
    }

    @Override
    public void checkFilmNotExistById(Long id) {
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT film_id " +
                "   FROM films " +
                "   WHERE film_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (rs.getBoolean(1)) throw new ExistsException("The film already exists");
        }, id);
    }

    @Override
    public void checkUserLikeToFilmNotExist(Long id, Long userId) {
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT user_id " +
                "   FROM likes " +
                "   WHERE user_id = ? " +
                "     AND film_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (rs.getBoolean(1)) {
                throw new ExistsException(String.format("A user with id: %d has already liked a film with id: %d",
                        userId, id));
            }
        }, userId, id);
    }

    @Override
    public List<Film> getFilmsSortedByPopularity(List<Long> matchingIds) {
        String inSql = String.join(",", Collections.nCopies(matchingIds.size(), "?"));
        String sqlQuery = String.format("" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name, " +
                "COUNT (l.user_id) rate " +
                "FROM films f " +
                "LEFT JOIN likes l on f.film_id = l.film_id " +
                "LEFT JOIN mpa ON f.mpa_id = mpa.mpa_id " +
                "WHERE f.film_id IN (%s) " +
                "GROUP BY f.film_id " +
                "ORDER BY rate DESC", inSql);
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, matchingIds.toArray());
    }

    private void addGenres(Set<Genre> genres, Long id) {
        String sqlQuery = "" +
                "INSERT INTO genres (film_id, genre_id) " +
                "VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sqlQuery, genres, 100, (ps, genre) -> {
            ps.setLong(1, id);
            ps.setLong(2, genre.getId());
        });
    }

    private void addDirectors(Set<Director> directors, Long id) {
        String sqlQuery = "" +
                "INSERT INTO directors (director_id, film_id) " +
                "VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sqlQuery, directors, 100, (ps, director) -> {
            ps.setLong(1, director.getId());
            ps.setLong(2, id);
        });
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date"),
                rs.getLong("duration"),
                rs.getLong("rate"),
                new Mpa(
                        rs.getLong("films.mpa_id"),
                        rs.getString("mpa.name"))
        );
    }

    private Map.Entry<Long, Long> mapRowToMapEntry(ResultSet rs, int rowNum) throws SQLException {
        Long userId = rs.getLong("user_id");
        Long filmId = rs.getLong("film_id");
        return new AbstractMap.SimpleEntry<>(userId, filmId);
    }

    private Map.Entry<Long, String> mapRowToMapEntryFilmIdFilmName(ResultSet rs, int i) throws SQLException {
        Long filmId = rs.getLong("film_id");
        String name = rs.getString("name");
        return new AbstractMap.SimpleEntry<>(filmId, name);
    }

    private Map.Entry<Long, String> mapRowToMapEntryFilmIdDirectorName(ResultSet rs, int i) throws SQLException {
        Long filmId = rs.getLong("film_id");
        String name = rs.getString("director_name");
        return new AbstractMap.SimpleEntry<>(filmId, name);
    }
}

