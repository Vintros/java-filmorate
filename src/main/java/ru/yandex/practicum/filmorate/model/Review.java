package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Review {

    private Long reviewId;
    @NonNull
    private Long userId;
    @NonNull
    private Long filmId;
    @NonNull
    private String content;
    @NonNull
    private Boolean isPositive;
    private Long useful;
}
