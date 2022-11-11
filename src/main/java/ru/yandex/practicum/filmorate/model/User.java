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
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"friends"})
public class User {

    private Long id;
    private String name;
    @Email(message = "Incorrect email")
    @NonNull
    private String email;
    @NonNull
    @Pattern(regexp = "\\A\\S+\\Z", message = "Validation error, empty login or whitespace characters in the user login")
    private String login;
    @NonNull
    @PastOrPresent(message = "Incorrect date of birth")
    private Date birthday;
    private final Set<User> friends = new HashSet<>();
}
