package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

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
    @JsonIgnore
    private final Map<Long, Boolean> usersRating = new HashMap<>();
    private Long useful;

    public Long getUseful() {
        return usersRating.values().stream()
                .mapToLong(v -> v ? 1L: -1L )
                .sum();
    }
}
