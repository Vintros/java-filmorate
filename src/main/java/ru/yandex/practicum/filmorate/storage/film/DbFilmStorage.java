package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.*;
import java.util.*;

@Repository
@Primary
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    public DbFilmStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQuery = "insert into films (name, description, release_date, duration, mpa_id) " +
                "values (?, ?, ?, ?, ?)";
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
        return getFilmById((long) keyHolder.getKey());
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "where film_id = ?; delete from genres where film_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            ps.setLong(6, film.getId());
            ps.setLong(7, film.getId());
            return ps;
        });
        addGenres(film.getGenres(), film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public void addLikeFilm(Long id, Long userId) {
        String sqlQuery = "insert into likes (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void removeLikeFilm(Long id, Long userId) {
        String sqlQuery = "delete from likes where film_id = ? and user_id = ?";
        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public List<Film> getFilmsWithoutGenres() {
        String sqlQuery = "select film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "from films left join mpa on films.mpa_id = mpa.mpa_id;";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = getFilmByIdWithoutGenres(id);
        List<Genre> genres = genreStorage.getGenresByFilmId(id);
        genres.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
        film.getGenres().addAll(genres);
        return film;
    }

    @Override
    public void removeFilmById(Long id) {
        String sqlQuery = "delete from films where film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private Film getFilmByIdWithoutGenres(Long id) {
        String sqlQuery = "select film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "from films join mpa on films.mpa_id = mpa.mpa_id where film_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
    }

    private void addGenres(Set<Genre> genres, Long id) {
        String sqlQuery = "insert into genres (film_id, genre_id) values (?, ?)";
        jdbcTemplate.batchUpdate(sqlQuery, genres, 100, (ps, genre) -> {
            ps.setLong(1, id);
            ps.setLong(2, genre.getId());
        });
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date"),
                rs.getLong("duration"),
                new Mpa(
                        rs.getLong("films.mpa_id"),
                        rs.getString("mpa.name")),
                new HashSet<>()
        );
        return film;
    }

    private Map<Long, List<Long>> extractUsersIdLiked(ResultSet rs) throws SQLException {
        Map<Long, List<Long>> usersIdLiked = new LinkedHashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            usersIdLiked.putIfAbsent(filmId, new ArrayList<>());
            Long userId = rs.getLong("user_id");
            usersIdLiked.get(filmId).add(userId);
        }
        return usersIdLiked;
    }

    public List<Film> getListPopularFilm(long count) {
        final String sqlQuery = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, " +
                "F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME, G.GENRE_ID " +
                "from FILMS F " +
                "join MPA M on M.MPA_ID = F.MPA_ID " +
                "left join GENRES G on F.FILM_ID = G.FILM_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "group by F.FILM_ID, G.GENRE_ID order by count(L.USER_ID) desc " +
                "limit ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    public List<Film> getListPopularFilmSortedByYear(int count, int year) {
        final String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, " +
                "F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME, G.GENRE_ID " +
                "FROM FILMS F " +
                "JOIN MPA M ON M.MPA_ID = F.MPA_ID " +
                "LEFT JOIN GENRES G ON F.FILM_ID = G.FILM_ID " +
                "LEFT JOIN LIKES L ON G.FILM_ID = L.FILM_ID " +
                "WHERE YEAR(F.RELEASE_DATE) = ? " +
                "GROUP BY F.FILM_ID, G.GENRE_ID ORDER BY COUNT(L.USER_ID) DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, year, count));
        return new ArrayList<>(films);
    }

    public List<Film> getListPopularFilmSortedByGenre(int count, long genreId) {
        final String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, " +
                "F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME, G.GENRE_ID " +
                "FROM FILMS F " +
                "JOIN MPA M ON M.MPA_ID = F.MPA_ID " +
                "LEFT JOIN GENRES G ON F.FILM_ID = G.FILM_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE G.GENRE_ID = ? " +
                "GROUP BY F.FILM_ID, G.GENRE_ID ORDER BY COUNT(L.USER_ID) DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, genreId, count));
        return new ArrayList<>(films);
    }

    public List<Film> findPopularFilmSortedByGenreAndYear(int count, long genreId, int year) {
        final String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, " +
                "F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME, G.GENRE_ID " +
                "FROM FILMS F " +
                "JOIN MPA M ON M.MPA_ID = F.MPA_ID " +
                "LEFT JOIN GENRES G ON F.FILM_ID = G.FILM_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE G.GENRE_ID = ? AND YEAR(F.RELEASE_DATE) = ? " +
                "GROUP BY F.FILM_ID, G.GENRE_ID ORDER BY COUNT(L.USER_ID) DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, genreId, year, count));
        return new ArrayList<>(films);
    }
}

