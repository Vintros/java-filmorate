package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.DataAccessException;
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
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
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
        return getFilmById((long) keyHolder.getKey());
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
        return getFilmById(film.getId());
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
    public List<Film> getFilmsWithoutGenres() {
        String sqlQuery = "" +
                "SELECT film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "FROM films " +
                "LEFT JOIN mpa ON films.mpa_id = mpa.mpa_id";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
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
    public void removeFilmById(Long id) {
        String sqlQuery = "" +
                "DELETE FROM films " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name, COUNT (l.user_id) " +
                "FROM films f " +
                "LEFT OUTER JOIN likes l on f.film_id = l.film_id " +
                "JOIN mpa ON f.mpa_id = mpa.mpa_id " +
                "WHERE f.film_id IN " +
                "   (SELECT film_id " +
                "    FROM directors " +
                "    WHERE director_id = ?) " +
                "GROUP BY f.film_id ";
        if ("likes".equals(sortBy)) {
            sql += "ORDER BY count (l.user_id) DESC";
        } else if ("year".equals(sortBy)) {
            sql += "ORDER BY f.release_date";
        }
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
        Map<Long, List<Genre>> genresByFilmsId = genreStorage.getGenresByFilmsId();
        Map<Long, List<Director>> directorsByFilmsId = directorStorage.getDirectorsByFilmsId();
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

    @Override
    public List<Film> getRecommendations(Long id) {
        final String sqlQuery = "" +
                "SELECT user_id, film_id " +
                "FROM likes " +
                "GROUP BY user_id, film_id";
        List<Film> result = new ArrayList<>();

        // Получаем список Entry с id_user (key) и id_film (value)
        List<Map.Entry<Long, Long>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntry);
        if (dataList.isEmpty()) return result;

        // Составляем мапу данных для алгоритма
        Map<Long, ArrayList<Long>> data = getDataMap(dataList);
        if (data.isEmpty()) return result;

        // Ищем пользователя с которым имеется максимальное количество перечений
        Long mostIntersectionsUserId = getMostIntersectionsUserId(id, data);
        if (mostIntersectionsUserId == null) return result;

        // Получаем список фильмов-рекомендаций

        for (Long otherFilmId : data.get(mostIntersectionsUserId)) {
            if (!data.get(id).contains(otherFilmId)) {
                result.add(this.getFilmById(otherFilmId));
            }
        }

        return result;
    }

    public List<Film> getListPopularFilm(long count) {
        final String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    public List<Film> getListPopularFilmSortedByYear(int count, int year) {
        final String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE YEAR(f.release_date) = ? " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, year, count));
        return new ArrayList<>(films);
    }

    public List<Film> getListPopularFilmSortedByGenre(int count, long genreId) {
        final String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, g.genre_id " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN genres AS g ON f.film_id = g.film_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE g.genre_id = ? " +
                "GROUP BY f.film_id, g.genre_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, genreId, count));
        return new ArrayList<>(films);
    }

    public List<Film> findPopularFilmSortedByGenreAndYear(int count, long genreId, int year) {
        final String sql = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name, g.genre_id " +
                "FROM films AS f " +
                "JOIN mpa AS m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN genres AS g ON f.film_id = g.film_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE g.genre_id = ? = ? AND YEAR(f.release_date) = ? " +
                "GROUP BY f.film_id, g.genre_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        Set<Film> films = new HashSet<>(jdbcTemplate.query(sql, this::mapRowToFilm, genreId, year, count));
        return new ArrayList<>(films);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "" +
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
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
                "ORDER BY COUNT(l.film_id) DESC";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, userId, friendId);
    }


    @Override
    public List<Film> searchFilmsWithoutGenresAndDirectorsByTitle(String query) {
        // Получаем список данных ID фильма + Название фильма
        final String sqlQuery = "" +
                "SELECT name, film_id " +
                "FROM films";
        List<Map.Entry<Long, String>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryFilmIdFilmName);

        // Производим поиск подходящих по названию id
        List<Long> matchingIds = getMatchingIds(query, dataList);

        // Получаем ответ в виде отсортированного по популярности списка
        return getFilmsSortedByPopularity(matchingIds);
    }

    @Override
    public List<Film> searchFilmsWithoutGenresAndDirectorsByDirector(String query) {
        // Получаем список данных Имя директора + ID фильма
        final String sqlQuery = "" +
                "SELECT dir.name AS director_name, dirs.film_id " +
                "FROM directors AS dirs " +
                "LEFT JOIN director AS dir ON dirs.director_id = dir.director_id";
        List<Map.Entry<Long, String>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryFilmIdDirectorName);

        // Производим поиск подходящих id
        List<Long> matchingIds = getMatchingIds(query, dataList);

        // Получаем ответ в виде отсортированного по популярности списка
        return getFilmsSortedByPopularity(matchingIds);
    }

    @Override
    public List<Film> searchFilmsWithoutGenresAndDirectorsByTitleAndDirector(String query) {
        List<Film> result = new ArrayList<>();
        List<Film> filmsByTitle = searchFilmsWithoutGenresAndDirectorsByTitle(query);
        List<Film> filmsByDirector = searchFilmsWithoutGenresAndDirectorsByDirector(query);

        result.addAll(filmsByTitle);
        result.addAll(filmsByDirector);

        return result.stream().distinct()
                .sorted(Comparator.comparing(Film::getRate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void checkFilmExistsById(Long id) {
        try {
            getFilmById(id);
        } catch (DataAccessException e) {
            throw new UnknownFilmException(String.format("Фильм с id: %d не найден", id));
        }
    }

    @Override
    public void checkFilmNotExistById(Long id) {
        String sqlQuery = "" +
                "SELECT EXISTS " +
                "  (SELECT film_id " +
                "   FROM films " +
                "   WHERE film_id = ?)";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            if (rs.getBoolean(1)) throw new ExistsException("Фильм уже зарегистрирован");
        }, id);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date"),
                rs.getLong("duration"),
                new Mpa(
                        rs.getLong("films.mpa_id"),
                        rs.getString("mpa.name"))
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

    private Film getFilmByIdWithoutGenres(Long id) {
        String sqlQuery = "" +
                "SELECT film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "FROM films " +
                "JOIN mpa ON films.mpa_id = mpa.mpa_id " +
                "WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);
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

    private static List<Long> getMatchingIds(String query, List<Map.Entry<Long, String>> dataList) {
        List<Long> matchingIds = new ArrayList<>();
        for (Map.Entry<Long, String> entry : dataList) {
            if (entry.getValue().toLowerCase().contains(query.toLowerCase())) {
                matchingIds.add(entry.getKey());
            }
        }
        return matchingIds;
    }

    private List<Film> getFilmsSortedByPopularity(List<Long> matchingIds) {
        String inSql = String.join(",", Collections.nCopies(matchingIds.size(), "?"));
        String sqlQuery = String.format("" +
                "SELECT film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "FROM films " +
                "LEFT JOIN mpa ON films.mpa_id = mpa.mpa_id " +
                "WHERE film_id IN (%s)", inSql);

        List<Film> result = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, matchingIds.toArray());

        return populateFilmsWithLikes(result);
    }

    private List<Film> populateFilmsWithLikes(List<Film> films) {
        Map<Long, List<Long>> likesByFilmsId = this.getLikesByFilmsId();
        for (Film film : films) {
            if (likesByFilmsId.get(film.getId()) != null) {
                film.getUsersIdLiked().addAll(likesByFilmsId.get(film.getId()));
            }
        }
        return films.stream()
                .sorted(Comparator.comparing(Film::getRate).reversed())
                .collect(Collectors.toList());
    }

    private Map<Long, List<Long>> getLikesByFilmsId() {
        String sqlQuery = "" +
                "SELECT film_id, user_id " +
                "FROM likes";
        return jdbcTemplate.query(sqlQuery, this::extractLikesByFilmId);
    }

    private Map<Long, List<Long>> extractLikesByFilmId(ResultSet rs) throws SQLException {
        Map<Long, List<Long>> likesByFilmId = new LinkedHashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            likesByFilmId.putIfAbsent(filmId, new ArrayList<>());
            likesByFilmId.get(filmId).add(userId);
        }
        return likesByFilmId;
    }
}

