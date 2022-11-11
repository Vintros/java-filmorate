package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ExistsException;
import ru.yandex.practicum.filmorate.exception.UnknownDirectorException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class DbDirectorStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    public DbDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getDirectors() {
        String sqlQuery = "" +
                "SELECT director_id, name " +
                "FROM director";
        return jdbcTemplate.query(sqlQuery, new DirectorMapper());
    }

    @Override
    public Director getDirectorById(Long id) {
        String sqlQuery = "" +
                "SELECT director_id, name " +
                "FROM director " +
                "WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, new DirectorMapper(), id);
    }

    @Override
    public Director addDirector(Director director) {
        String sqlQuery = "" +
                "INSERT INTO director (name) " +
                "VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        return getDirectorById(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "" +
                "UPDATE director " +
                "SET name = ? " +
                "WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public void removeDirectorById(Long id) {
        String sqlQuery = "" +
                "DELETE FROM director " +
                "WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Map<Long, List<Director>> getDirectorsByFilmsId() {
        String sqlQuery = "" +
                "SELECT directors.film_id, director.director_id, director.name " +
                "FROM directors " +
                "JOIN director ON directors.director_id = director.director_id " +
                "ORDER BY directors.film_id";
        return jdbcTemplate.query(sqlQuery, this::extractDirectorsByFilmId);
    }

    @Override
    public List<Director> getDirectorsByFilmId(Long id) {
        String sqlQuery = "" +
                "SELECT * " +
                "FROM director " +
                "WHERE director_id IN " +
                "   (SELECT director_id " +
                "    FROM directors " +
                "    WHERE film_id = ?)";
        return jdbcTemplate.query(sqlQuery, new DirectorMapper(), id);
    }

    @Override
    public void checkDirectorExistsById(Long id) {
        try {
            getDirectorById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new UnknownDirectorException(String.format("Режиссёр с id: %d не найден", id));
        }
    }

    @Override
    public void checkDirectorNotExistById(Long id) {
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT director_id " +
                "   FROM director " +
                "   WHERE director_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (rs.getBoolean(1)) throw new ExistsException("Режиссёр уже зарегистрирован");
        }, id);
    }

    private Map<Long, List<Director>> extractDirectorsByFilmId(ResultSet rs) throws SQLException {
        Map<Long, List<Director>> directorsByFilmId = new LinkedHashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("directors.film_id");
            directorsByFilmId.putIfAbsent(filmId, new ArrayList<>());
            Director director = new Director();
            director.setId(rs.getLong("director.director_id"));
            director.setName(rs.getString("director.name"));
            directorsByFilmId.get(filmId).add(director);
        }
        return directorsByFilmId;
    }

    private static class DirectorMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("name"));
            return director;
        }
    }
}
