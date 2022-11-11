package ru.yandex.practicum.filmorate.validator.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.sql.Date;
import java.time.LocalDate;


/**
 * Валидатор для аннотации {@link AfterCinemaBirthday}.
 */
public class CinemaBirthdayConstraintValidator implements ConstraintValidator<AfterCinemaBirthday, Date> {
    private static final LocalDate cinemaBirthday = LocalDate.of(1895, 12, 27);

    @Override
    public boolean isValid(Date date, ConstraintValidatorContext cxt) {
        return date.toLocalDate().isAfter(cinemaBirthday);
    }
}