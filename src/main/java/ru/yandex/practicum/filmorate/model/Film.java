package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Film {

    private Integer id;
    @NonNull
    @NotBlank(message = "Пустое поле названия фильма")
    private String name;
    @NonNull
    @Size(max = 200, message = "Описание должно быть не более 200 символов")
    private String description;
    @NonNull
    @PastOrPresent(message = "Некорректная дата фильма")
    private LocalDate releaseDate;
    @NonNull
    @Positive
    private Long duration;
}
