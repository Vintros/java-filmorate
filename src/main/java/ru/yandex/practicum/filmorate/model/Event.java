package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Event {

    private Long eventId;
    private final Long userId;
    private final Long entityId;
    private final String eventType;
    private final String operation;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private final Date timestamp;
}
