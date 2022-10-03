package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private long id;
    private String name;
    private Set<Long> friends = new HashSet<>();

    @Email(message = "Некорректная почта")
    @NonNull
    private String email;
    @NonNull
    @Pattern(regexp = "\\A\\S+\\Z", message = "Ошибка валидации, пустой логин или пробельные символы в логине пользователя")
    private String login;
    @NonNull
    @PastOrPresent(message = "Некорректная дата рождения")
    private LocalDate birthday;
}
