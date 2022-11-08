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
import java.util.stream.Collectors;

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
    public void removeFilmById(Long id) {
        String sqlQuery = "delete from films where film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
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

    @Override
    public List<Film> getRecommendations(Long id) {
        final String sqlQuery = "SELECT USER_ID, FILM_ID FROM LIKES GROUP BY USER_ID, FILM_ID";
        List<Film> result = new ArrayList<>();

        // Получаем список Entry с id_user (key) и id_film (value)
        List<Map.Entry<Long, Long>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryLongLong);
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


    private Map.Entry<Long, Long> mapRowToMapEntryLongLong(ResultSet rs, int rowNum) throws SQLException {
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

    public List<Film> getListPopularFilm(long count) {
        final String sqlQuery = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, " +
                "F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME " +
                "from FILMS F " +
                "join MPA M on M.MPA_ID = F.MPA_ID " +
                "left join LIKES L on F.FILM_ID = L.FILM_ID " +
                "group by F.FILM_ID order by count(L.USER_ID) desc " +
                "limit ?";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
    }

    public List<Film> getListPopularFilmSortedByYear(int count, int year) {
        final String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, " +
                "F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME " +
                "FROM FILMS F " +
                "JOIN MPA M ON M.MPA_ID = F.MPA_ID " +
                "LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "WHERE YEAR(F.RELEASE_DATE) = ? " +
                "GROUP BY F.FILM_ID ORDER BY COUNT(L.USER_ID) DESC " +
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

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sqlQuery = "select f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, mpa.name " +
                "from films f join mpa on f.mpa_id = mpa.mpa_id " +
                "join likes l on l.film_id = f.film_id " +
                "where f.film_id in (select film_id from likes where user_id = ? " +
                "intersect select film_id from likes where user_id = ?) " +
                "group by f.film_id " +
                "order by count(l.film_id) desc;";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm, userId, friendId);
    }



    @Override
    public List<Film> searchFilmsWithoutGenresAndDirectorsByTitle(String query) {
        // Получаем список данных ID фильма + Название фильма
        final String sqlQuery = "SELECT NAME, FILM_ID FROM FILMS";
        List<Map.Entry<Long, String>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryFilmIdFilmName);

        // Производим поиск подходящих по названию id
        List<Long> matchingIds = getMatchingIds(query, dataList);

        // Получаем ответ в виде отсортированного по популярности списка
        return getFilmsSortedByPopularity(matchingIds);
    }

    private Map.Entry<Long, String> mapRowToMapEntryFilmIdFilmName(ResultSet rs, int i) throws SQLException {
        Long filmId = rs.getLong("film_id");
        String name = rs.getString("name");

        return new AbstractMap.SimpleEntry<>(filmId, name);
    }

    @Override
    public List<Film> searchFilmsWithoutGenresAndDirectorsByDirector(String query) {
        // Получаем список данных Имя директора + ID фильма
        final String sqlQuery = "SELECT DIR.NAME AS DIRECTOR_NAME, DIRS.FILM_ID FROM DIRECTORS AS DIRS " +
                "LEFT JOIN DIRECTOR AS DIR ON DIRS.DIRECTOR_ID = DIR.DIRECTOR_ID";
        List<Map.Entry<Long, String>> dataList = jdbcTemplate.query(sqlQuery, this::mapRowToMapEntryFilmIdDirectorName);

        // Производим поиск подходящих id
        List<Long> matchingIds = getMatchingIds(query, dataList);

        // Получаем ответ в виде отсортированного по популярности списка
        return getFilmsSortedByPopularity(matchingIds);
    }

    private Map.Entry<Long, String> mapRowToMapEntryFilmIdDirectorName(ResultSet rs, int i) throws SQLException {
        Long filmId = rs.getLong("film_id");
        String name = rs.getString("director_name");
        return new AbstractMap.SimpleEntry<>(filmId, name);
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
        String sqlQuery = String.format("SELECT film_id, films.name, description, release_date, duration, films.mpa_id, mpa.name " +
                "FROM films LEFT JOIN mpa ON films.mpa_id = mpa.mpa_id " +
                "WHERE film_id in (%s)", inSql);

        List<Film> result = jdbcTemplate.query(sqlQuery, matchingIds.toArray(), this::mapRowToFilm);

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
        String sqlQuery = "select film_id, user_id from likes";
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

