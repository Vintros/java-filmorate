package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
@Primary
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
        jdbcTemplate.update(sqlQuery, director.getName());
        String sqlQuery2 = "select director_id, name from director where name = ?";
        return jdbcTemplate.queryForObject(sqlQuery2, new DirectorMapper(), director.getName());
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "update director set name = ? where director_id = ?";
        jdbcTemplate.update(sqlQuery, director.getName(), director.getId());
        String sqlQuery2 = "select director_id, name from director where name = ?";
        return jdbcTemplate.queryForObject(sqlQuery2, new DirectorMapper(), director.getName());
    }

    @Override
    public void removeDirectorById(Long id) {
        String sqlQuery = "delete from director where director_id = ?";
        jdbcTemplate.update(sqlQuery, id);
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
