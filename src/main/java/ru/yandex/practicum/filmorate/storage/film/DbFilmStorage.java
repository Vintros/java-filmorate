package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.*;
import java.util.*;

@Repository
@Primary
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public DbFilmStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, DirectorStorage directorStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
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
        if (!film.getDirectors().isEmpty()) {
            addDirectors(film.getDirectors(), (long) keyHolder.getKey());
        }
        return getFilmById((long) keyHolder.getKey());
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlQuery = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "where film_id = ?; delete from genres where film_id = ?; delete from directors where film_id = ?";
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
    public Map<Long, List<Long>> getUsersIdLiked() {
        String sqlQuery = "select film_id, user_id from likes";
        return jdbcTemplate.query(sqlQuery, this::extractUsersIdLiked);
    }

    @Override
    public List<Film> getFilmsWithoutGenres() {
        String sqlQuery = "select film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "from films left join mpa on films.mpa_id = mpa.mpa_id";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = getFilmByIdWithoutGenres(id);
        List<Genre> genres = genreStorage.getGenresByFilmId(id);
        genres.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
        film.getGenres().addAll(genres);
        List<Director> directors = directorStorage.getDirectorsByFilmId(id);
        directors.sort((d1, d2) -> (int) (d1.getId() - d2.getId()));
        film.getDirectors().addAll(directors);
        return film;
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sql = "select f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name, count (l.user_id) "
                + "from films f left outer join likes l on f.film_id = l.film_id join mpa on f.mpa_id = mpa.mpa_id "
                + "where f.film_id in (select film_id from directors where director_id = ?) "
                + "group by f.film_id ";
        if ("likes".equals(sortBy)) {
            sql += "order by count (l.user_id) DESC";
        } else if ("year".equals(sortBy)) {
            sql += "order by f.release_date";
        }
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
        Map<Long, List<Genre>> genresByFilmsId = genreStorage.getGenresByFilmsId();
        Map<Long, List<Director>> directorsByFilmsId = directorStorage.getGenresByFilmsId();
        for (Film film : films) {
            if (genresByFilmsId.get(film.getId()) != null) {
                film.getGenres().addAll(genresByFilmsId.get(film.getId()));
            }
            if (directorsByFilmsId.get(film.getId()) != null) {
                film.getDirectors().addAll(directorsByFilmsId.get(film.getId()));
            }
        }
        return films;
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

    private void addDirectors(Set<Director> directors, Long id) {
        String sqlQuery = "insert into directors (director_id, film_id) values (?, ?)";
        for (Director director : directors) {
            jdbcTemplate.update(sqlQuery, director.getId(), id);
        }
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
                        rs.getString("mpa.name"))
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
}

