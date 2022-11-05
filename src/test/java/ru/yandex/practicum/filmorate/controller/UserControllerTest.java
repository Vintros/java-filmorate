package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
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
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private User user;

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserWithoutName() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"))
                .andExpect(jsonPath("$.friends").isArray())
                .andExpect(jsonPath("$.friends").isEmpty());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserTwice() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));

        User newUser = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));
        newUser.setName("Name");
        newUser.setId(1L);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserEmptyRequestBody() throws Exception {

        mockMvc.perform(post("/users")).andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserWithName() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));
        user.setName("Name");

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));
    }


    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserFailLogin() throws Exception {
        user = new User("mail@mail.ru", "Login Loginovich", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserFailEmail() throws Exception {
        user = new User("@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void createUserFailBirthday() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(2555, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void updateUser() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));

        User newUser = new User("mail@yandex.ru", "newLogin", Date.valueOf(LocalDate.of(1950, 8, 20)));
        newUser.setName("Good Name");
        newUser.setId(1L);

        mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsBytes(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Good Name"))
                .andExpect(jsonPath("$.email").value("mail@yandex.ru"))
                .andExpect(jsonPath("$.login").value("newLogin"))
                .andExpect(jsonPath("$.birthday").value("1950-08-20"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void updateUserUnknown() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));

        User newUser = new User("mail@yandex.ru", "newLogin", Date.valueOf(LocalDate.of(1950, 8, 20)));
        newUser.setName("Good Name");
        newUser.setId(-1L);

        mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsBytes(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getUsers() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[1]").doesNotExist())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Login"))
                .andExpect(jsonPath("$.[0].email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.[0].login").value("Login"))
                .andExpect(jsonPath("$.[0].birthday").value("1946-08-20"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getUserById() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getUserByIncorrectId() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.email").value("mail@mail.ru"))
                .andExpect(jsonPath("$.login").value("Login"))
                .andExpect(jsonPath("$.birthday").value("1946-08-20"));

        mockMvc.perform(get("/users/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void addAndGetFriends() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user))
                .contentType(MediaType.APPLICATION_JSON));

        User user2 = new User("gmail@gmail.ru", "SecondLogin", Date.valueOf(LocalDate.of(1955, 3, 1)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("SecondLogin"))
                .andExpect(jsonPath("$[0].email").value("gmail@gmail.ru"))
                .andExpect(jsonPath("$[0].login").value("SecondLogin"))
                .andExpect(jsonPath("$[0].birthday").value("1955-03-01"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void deleteFriend() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user))
                .contentType(MediaType.APPLICATION_JSON));

        User user2 = new User("gmail@gmail.ru", "SecondLogin", Date.valueOf(LocalDate.of(1955, 3, 1)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("SecondLogin"))
                .andExpect(jsonPath("$[0].email").value("gmail@gmail.ru"))
                .andExpect(jsonPath("$[0].login").value("SecondLogin"))
                .andExpect(jsonPath("$[0].birthday").value("1955-03-01"));

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void GetCommonFriends() throws Exception {
        user = new User("mail@mail.ru", "Login", Date.valueOf(LocalDate.of(1946, 8, 20)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user))
                .contentType(MediaType.APPLICATION_JSON));

        User user2 = new User("gmail@gmail.ru", "SecondLogin", Date.valueOf(LocalDate.of(1955, 3, 1)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        User user3 = new User("nomail@nomail.ru", "ThirdLogin", Date.valueOf(LocalDate.of(1950, 3, 1)));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsBytes(user3))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(put("/users/1/friends/3"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/2/friends/3"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("ThirdLogin"))
                .andExpect(jsonPath("$[0].email").value("nomail@nomail.ru"))
                .andExpect(jsonPath("$[0].login").value("ThirdLogin"))
                .andExpect(jsonPath("$[0].birthday").value("1950-03-01"));

        mockMvc.perform(get("/users/2/friends/common/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void shouldReturn200andListOnGetRecommendationsWhenValidRequest() throws Exception {
        User user1 = new User("user1@mail.ru", "login1", Date.valueOf(LocalDate.of(1946, 8, 20)));
        User user2 = new User("user2@mail.ru", "login2", Date.valueOf(LocalDate.of(1946, 8, 20)));
        User user3 = new User("user3@mail.ru", "login3", Date.valueOf(LocalDate.of(1946, 8, 20)));
        Film film1 = new Film("Name 1", "Description Film 1", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));
        Film film2 = new Film("Name 2", "Description Film 2", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));
        Film film3 = new Film("Name 3", "Description Film 3", Date.valueOf(LocalDate.of(2000, 1, 1)), 200L, new Mpa(1L, null));

        postUser(user1);
        postUser(user2);
        postUser(user3);

        postFilm(film1);
        postFilm(film2);
        postFilm(film3);

        likeFilm(1L,1L);
        likeFilm(2L,1L);
        likeFilm(1L,2L);
        likeFilm(2L,2L);
        likeFilm(3L,2L);

        mockMvc.perform(get("/users/{id}/recommendations", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("Name 3"))
                .andExpect(jsonPath("$[0].description").value("Description Film 3"))
                .andExpect(jsonPath("$[0].releaseDate").value("2000-01-01"))
                .andExpect(jsonPath("$[0].duration").value(200));
    }

    private void postUser(User user) throws Exception {
        mockMvc.perform(
                post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private void postFilm(Film film) throws Exception {
        mockMvc.perform(
                post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private void likeFilm(Long filmId, Long userId) throws Exception {
        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, userId));
    }
}