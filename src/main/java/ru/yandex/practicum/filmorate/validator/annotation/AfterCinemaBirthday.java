package ru.yandex.practicum.filmorate.validator.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Аннотация, проверяющая дату фильма.
 */
@Documented
@Constraint(validatedBy = CinemaBirthdayConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterCinemaBirthday {
    String message() default "The release date of the film cannot be set earlier than 28.12.1895";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}