package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"friends"})
public class User {

    private Long id;
    private String name;
    @Email(message = "Некорректная почта")
    @NonNull
    private String email;
    @NonNull
    @Pattern(regexp = "\\A\\S+\\Z", message = "Ошибка валидации, пустой логин или пробельные символы в логине пользователя")
    private String login;
    @NonNull
    @PastOrPresent(message = "Некорректная дата рождения")
    private Date birthday;
    private final Set<User> friends = new HashSet<>();
}
