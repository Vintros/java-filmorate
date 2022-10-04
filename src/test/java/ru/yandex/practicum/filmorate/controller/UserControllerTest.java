package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private User user;

    @Autowired
    private UserController userController;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createUserWithoutName() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));

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
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createUserTwice() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));

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

        User newUser = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));
        newUser.setName("Name");
        newUser.setId(1L);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserEmptyRequestBody() throws Exception {

        mockMvc.perform(post("/users")).andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createUserWithName() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));
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
    void createUserFailLogin() throws Exception {
        user = new User("mail@mail.ru", "Login Loginovich", LocalDate.of(1946, 8, 20));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserFailEmail() throws Exception {
        user = new User("@mail.ru", "Login", LocalDate.of(1946, 8, 20));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserFailBirthday() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(2555, 8, 20));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsBytes(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUser() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));

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

        User newUser = new User("mail@yandex.ru", "newLogin", LocalDate.of(1950, 8, 20));
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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUserUnnown() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));

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

        User newUser = new User("mail@yandex.ru", "newLogin", LocalDate.of(1950, 8, 20));
        newUser.setName("Good Name");
        newUser.setId(-1L);

        mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsBytes(newUser))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void getUsers() throws Exception {
        user = new User("mail@mail.ru", "Login", LocalDate.of(1946, 8, 20));

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
}