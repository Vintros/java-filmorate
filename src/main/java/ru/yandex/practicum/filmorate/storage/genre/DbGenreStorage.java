package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class DbGenreStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public DbGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sqlQuery = "select * from genre";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public Genre getGenreById(Long id) {
        String sqlQuery = "select * from genre where genre_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
    }

    @Override
    public List<Genre> getGenresByFilmId(Long id) {
        String sqlQuery = "select * from genre where genre_id in (select genre_id from genres where film_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre, id);
    }

    @Override
    public Map<Long, List<Genre>> getGenresByFilmsId() {
        String sqlQuery = "select genres.film_id, genre.genre_id, genre.name from genres join genre on " +
                "genres.genre_id = genre.genre_id order by genres.film_id";
        return jdbcTemplate.query(sqlQuery, this::extractGenresByFilmId);
    }

    private Map<Long, List<Genre>> extractGenresByFilmId(ResultSet rs) throws SQLException {
        Map<Long, List<Genre>> genresByFilmId = new LinkedHashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("genres.film_id");
            genresByFilmId.putIfAbsent(filmId, new ArrayList<>());
            Genre genre = new Genre(
                    rs.getLong("genre.genre_id"),
                    rs.getString("genre.name")
            );
            genresByFilmId.get(filmId).add(genre);
        }
        return genresByFilmId;
    }

    public Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre(
                rs.getLong("genre_id"),
                rs.getString("name")
        );
        return genre;
    }
}
