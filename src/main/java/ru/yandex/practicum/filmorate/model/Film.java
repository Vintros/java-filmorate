package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.yandex.practicum.filmorate.validator.annotation.AfterCinemaBirthday;

import javax.validation.constraints.*;
import java.sql.Date;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"mpa", "genres", "usersIdLiked"})
public class Film {

    private Long id;
    @NonNull
    @NotBlank(message = "Empty movie title field")
    private String name;
    @NonNull
    @Size(max = 200, message = "The description should contain no more than 200 characters")
    private String description;
    @NonNull
    @AfterCinemaBirthday
    private Date releaseDate;
    @NonNull
    @Positive
    private Long duration;
    @NonNull
    private Mpa mpa;
    private final Set<Genre> genres = new TreeSet<>(Comparator.comparingLong(Genre::getId));
    private final Set<Director> directors = new HashSet<>();
    @JsonIgnore
    private final Set<Long> usersIdLiked = new HashSet<>();

    public int getRate() {
        return usersIdLiked.size();
    }
}
