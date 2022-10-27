package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenreAndMpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getAllMpa() throws Exception {
        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[5]").doesNotExist())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("G"))
                .andExpect(jsonPath("$.[4].id").value(5))
                .andExpect(jsonPath("$.[4].name").value("NC-17"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getMpaById() throws Exception {
        mockMvc.perform(get("/mpa/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("PG"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getMpaByIncorrectId() throws Exception {
        mockMvc.perform(get("/mpa/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getAllGenres() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[6]").doesNotExist())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].name").value("Комедия"))
                .andExpect(jsonPath("$.[5].id").value(6))
                .andExpect(jsonPath("$.[5].name").value("Боевик"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getGenresById() throws Exception {
        mockMvc.perform(get("/genres/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Драма"));
    }

    @Test
    @Sql(scripts = {"file:./src/test/java/setup_test.sql"})
    void getGenresByIncorrectId() throws Exception {
        mockMvc.perform(get("/genres/9"))
                .andExpect(status().isNotFound());
    }
}
