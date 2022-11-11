package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    /**
     * Метод добавляет в хранилище пользователя.
     *
     * @param user пользователь.
     * @return Добавленный пользователь.
     */
    User createUser(User user);

    /**
     * Метод обновляет пользователя в хранилище.
     *
     * @param user пользователь.
     * @return Обновлённый пользователь.
     */
    User updateUser(User user);

    /**
     * Метод возвращает список всех пользователей
     * в хранилище.
     *
     * @return Список всех пользователей.
     */
    List<User> getUsers();

    /**
     * Метод возвращает пользователя из хранилища
     * по его идентификатору.
     *
     * @param id идентификатор пользователя.
     * @return Пользователь, принадлежащий
     * идентификатору.
     */
    User getUserById(Long id);

    /**
     * Метод добавляет пользователя в друзья
     * другого пользователя.
     *
     * @param id       идентификатор пользователя.
     * @param friendId идентификатор пользователя-друга.
     */
    void addFriend(Long id, Long friendId);

    /**
     * Метод удаляет пользователя из друзей
     * другого пользователя.
     *
     * @param id       идентификатор пользователя.
     * @param friendId идентификатор
     *                 пользователя-бывшего друга.
     */
    void removeFriend(Long id, Long friendId);

    /**
     * Метод возвращает список общих друзей
     * между двумя пользователями.
     *
     * @param id       идентификатор одного
     *                 пользователя.
     * @param friendId идентификатор другого
     *                 пользователя.
     * @return Список общих друзей, между двумя
     * пользователями.
     */
    List<User> getCommonFriends(Long id, Long friendId);

    /**
     * Метод удаляет пользователя
     * по его идентификатору.
     *
     * @param id идентификатор пользователя.
     */
    void removeUserById(Long id);

    /**
     * Метод проверяет наличие пользователя
     * в хранилище.
     *
     * @param id идентификатор пользователя.
     */
    void checkUserExists(Long id);

    /**
     * Метод проверяет отсутствие пользователя
     * в хранилище.
     *
     * @param user проверяемый пользователь.
     */
    void checkUserNotExist(User user);
}
