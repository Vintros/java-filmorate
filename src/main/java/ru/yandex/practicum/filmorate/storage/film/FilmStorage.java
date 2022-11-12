package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {
    /**
     * Метод добавляет фильм в хранилище.
     *
     * @param film объект фильма.
     * @return Добавленный фильм.
     */
    Film addFilm(Film film);

    /**
     * Метод обновляет фильм в хранилище.
     *
     * @param film объект фильма.
     * @return Обновлённый фильм.
     */
    Film updateFilm(Film film);

    /**
     * Метод возвращает список фильмов,
     * без заполненных жанров
     *
     * @return список фильмов без заполненных
     * жанров.
     */
    List<Film> getFilmsWithoutGenresAndDirectors();

    /**
     * Метод добавляет в хранилище лайк.
     *
     * @param id     идентификатор фильма.
     * @param userId идентификатор пользователя.
     */
    void addLikeFilm(Long id, Long userId);

    /**
     * Метод удаляет лайк из хранилища.
     *
     * @param id     идентификатор фильма.
     * @param userId идентификатор пользователя.
     */
    void removeLikeFilm(Long id, Long userId);

    /**
     * Метод возвращает фильм по его идентификатору.
     *
     * @param id идентификатор фильма.
     * @return Фильм, принадлежащий идентификатору.
     */
    Film getFilmByIdWithoutGenresAndDirectors(Long id);

    /**
     * Метод удаляет фильм из хранилища по его
     * идентификатору
     *
     * @param id идентификатор фильма.
     */
    void removeFilmById(Long id);

    /**
     * Метод возвращает список фильмов по
     * идентификатору режиссёра и сортирует
     * его по параметру сортировки.
     *
     * @param directorId идентификатор режиссёра.
     * @param sortBy     параметр сортировки.
     * @return Список фильмов, связанный с режиссёром
     * и отсортированный по параметру сортировки.
     */
    List<Film> getFilmsByDirectorWithoutGenresAndDirectors(Long directorId, String sortBy);

    /**
     * Метод возвращает список рекомендованных
     * фильмов по идентификатору пользователя.
     * Рекомендация определяется по следующему
     * алгоритму:
     * <ol>
     *   <li>
     *     Находится пользователь с максимально похожим
     *     вкусом.
     *   </li>
     *   <li>
     *     У найденного пользователя определяются фильмы,
     *     которые ещё не смотрел первый пользователь
     *   </li>
     * </ol>
     *
     * @param id идентификатор пользователя.
     * @return Список рекомендованных фильмов.
     */
    List<Map.Entry<Long, Long>> getEntriesUserIdLikedFilmId(Long id); // todo исправить описание

    /**
     * Метод возвращает список популярных
     * (по количеству лайков) фильмов.
     *
     * @param count размер списка.
     * @return Список популярных фильмов.
     */
    List<Film> getListPopularFilm(long count);

    /**
     * Метод возвращает список популярных
     * (по количеству лайков) фильмов,
     * отобранных по году.
     *
     * @param count размер списка.
     * @param year  год для выборки.
     * @return Список популярных фильмов,
     * отобранных по году.
     */
    List<Film> getListPopularFilmSortedByYear(int count, int year);

    /**
     * Метод возвращает список популярных
     * (по количеству лайков) фильмов,
     * отобранных по жанру.
     *
     * @param count   размер списка.
     * @param genreId идентификатор жанра для выборки.
     * @return Список популярных фильмов,
     * отобранных по жанру.
     */
    List<Film> getListPopularFilmSortedByGenre(int count, long genreId);

    /**
     * Метод возвращает список популярных
     * (по количеству лайков) фильмов,
     * отобранных по жанру и году.
     *
     * @param count   размер списка.
     * @param genreId идентификатор жанра
     *                для выборки.
     * @param year    год для выборки.
     * @return Список популярных фильмов,
     * отобранных по жанру и году.
     */
    List<Film> findPopularFilmSortedByGenreAndYear(int count, long genreId, int year);

    /**
     * Метод возвращает список общих понравившихся
     * фильмов между пользователями.
     *
     * @param userId   идентификатор первого
     *                 пользователя.
     * @param friendId идентификатор второго
     *                 пользователя.
     * @return Список общих понравившихся фильмов,
     * между пользователями.
     */
    List<Film> getCommonFilms(Long userId, Long friendId);

    /**
     * Метод возвращает список фильмов, найденных
     * по названию.
     *
     * @param query текст для поиска.
     * @return Список фильмов, найденных по названию.
     */
    List<Map.Entry<Long, String>> searchFilmsWithoutGenresAndDirectorsByTitle(String query);

    /**
     * Метод возвращает список фильмов, найденных
     * по режиссёру.
     *
     * @param query текст для поиска.
     * @return Список фильмов, найденных по режиссёру.
     */
    List<Map.Entry<Long, String>> searchFilmsWithoutGenresAndDirectorsByDirector(String query);

    /**
     * Метод проверяет наличие фильма
     * в хранилище.
     *
     * @param id идентификатор фильма.
     */
    void checkFilmExistsById(Long id);

    /**
     * Метод проверяет отсутствие фильма
     * в хранилище.
     *
     * @param id идентификатор фильма.
     */
    void checkFilmNotExistById(Long id);

    /**
     * Метод проверяет отсутствие лайка
     * фильма в хранилище.
     *
     * @param id идентификатор пользователя,
     *           поставившего лайк.
     */
    void checkUserLikeToFilmNotExist(Long id, Long userId);

    List<Film> getFilmsSortedByPopularity(List<Long> matchingIds);
}
