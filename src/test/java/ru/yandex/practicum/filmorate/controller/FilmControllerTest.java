package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class FilmControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private Film film;

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilm() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200))
                .andExpect(jsonPath("$.mpa").isMap())
                .andExpect(jsonPath("$.mpa.id").value(1));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmWithGenres() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));
        film.getGenres().add(new Genre(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200))
                .andExpect(jsonPath("$.mpa").isMap())
                .andExpect(jsonPath("$.mpa.id").value(1))
                .andExpect(jsonPath("$.genres[0].id").value(1));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmTwice() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200));

        Film newFilm = new Film(1L, "Name", "Description Film", Date.valueOf(LocalDate.of( 2000, 1, 1)),
                200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(newFilm))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmDescriptionLength200() throws Exception {
        String rnString = RandomString.make(200);
        film = new Film("Name", rnString, Date.valueOf(LocalDate.of(1895, 12, 28)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(rnString));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmFailName() throws Exception {
        film = new Film("", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/films")).andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmFailDescription() throws Exception {
        film = new Film("Name", RandomString.make(201), Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmFailReleaseDate() throws Exception {
        film = new Film("Name", RandomString.make(150), Date.valueOf(LocalDate.of(1895, 12, 27)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmFailReleaseDateInFuture() throws Exception {
        film = new Film("Name", RandomString.make(150), Date.valueOf(LocalDate.of(2555, 12, 27)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createFilmFailDuration() throws Exception {
        film = new Film("Name", RandomString.make(150), Date.valueOf(LocalDate.of(2000, 1, 1)), -200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void updateFilm() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200));

        Film newFilm = new Film("New name", "New Description Film", Date.valueOf(LocalDate.of(2021, 5, 7)),
                300L, new Mpa(1L, null));
        newFilm.setId(1L);

        mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(newFilm))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.description").value("New Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2021-05-07"))
                .andExpect(jsonPath("$.duration").value(300));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void updateFilmFailId() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200));

        Film newFilm = new Film("New name", "New Description Film", Date.valueOf(LocalDate.of(2021, 5, 7)),
                300L, new Mpa(1L, null));
        newFilm.setId(-1L);

        mockMvc.perform(put("/films")
                        .content(objectMapper.writeValueAsString(newFilm))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getFilms() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[1]").doesNotExist())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Name"))
                .andExpect(jsonPath("$.[0].description").value("Description Film"))
                .andExpect(jsonPath("$.[0].releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.[0].duration").value(200));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getFilmById() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200))
                .andExpect(jsonPath("$.mpa.id").value(1));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getFilmByIncorrectId() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description Film"))
                .andExpect(jsonPath("$.releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$.duration").value(200));

        mockMvc.perform(get("/films/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getMostLikedFilms() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        Film film2 = new Film("SecondName", "Description", Date.valueOf(LocalDate.of(1999, 1, 10)), 200L, new Mpa(2L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        User user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        mockMvc.perform(put("/films/2/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void removeLikeFilm() throws Exception {
        film = new Film("Name", "Description Film", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        Film film2 = new Film("SecondName", "Description", Date.valueOf(LocalDate.of(1999, 1, 10)), 200L, new Mpa(2L, null));

        mockMvc.perform(post("/films")
                        .content(objectMapper.writeValueAsString(film2))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        User user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        mockMvc.perform(put("/films/2/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));

        mockMvc.perform(delete("/films/2/like/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

}