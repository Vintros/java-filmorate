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
    public Collection<Director> getDirectors() {
        String sqlQuery = "select director_id, name from director";
        return jdbcTemplate.query(sqlQuery, new DirectorMapper());
    }

    @Override
    public Director getDirectorById(Long id) {
        String sqlQuery = "select director_id, name from director where director_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, new DirectorMapper(), id);
    }

    @Override
    public Director addDirector(Director director) {
        String sqlQuery = "insert into director (name) values (?)";
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
        String sqlQuery = "update director set name = ? where director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public void removeDirectorById(Long id) {
        String sqlQuery = "delete from director where director_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Map<Long, List<Director>> getDirectorsByFilmsId() {
        String sqlQuery = "select directors.film_id, director.director_id, director.name from directors join director "
                + "on directors.director_id = director.director_id order by directors.film_id";
        return jdbcTemplate.query(sqlQuery, this::extractDirectorsByFilmId);
    }

    @Override
    public List<Director> getDirectorsByFilmId(Long id) {
        String sqlQuery = "select * from director where director_id in (select director_id from directors where film_id = ?)";
        return jdbcTemplate.query(sqlQuery, new DirectorMapper(), id);
    }

    @Override
    public void validateDirector(Long id) {
        try {
            getDirectorById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new UnknownDirectorException(String.format("Режиссёр с id: %d не найден", id));
        }
    }

    @Override
    public void checkDirectorNotExist(Director director) {
        if (director.getId() == null) {
            return;
        }
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT director_id " +
                "   FROM director " +
                "   WHERE director_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (rs.getLong(1) == director.getId()) throw new ExistsException("Режиссёр уже зарегистрирован");
        }, director.getId());
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
