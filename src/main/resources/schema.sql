DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS director;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS mpa CASCADE;
DROP TABLE IF EXISTS genre CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS reviews_rating CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;

CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id bigint PRIMARY KEY,
    name   text
);

CREATE TABLE IF NOT EXISTS genre
(
    genre_id bigint PRIMARY KEY,
    name     text
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         text         NOT NULL,
    description  varchar(200) NOT NULL,
    release_date date         NOT NULL,
    duration     bigint       NOT NULL,
    mpa_id       integer REFERENCES mpa (mpa_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS genres
(
    film_id  bigint REFERENCES films (film_id) ON DELETE CASCADE,
    genre_id integer REFERENCES genre (genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS users
(
    user_id  bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name     text,
    email    text,
    login    text,
    birthday text
);

CREATE TABLE IF NOT EXISTS likes
(
    film_id bigint REFERENCES films (film_id) ON DELETE CASCADE,
    user_id bigint REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);


CREATE TABLE IF NOT EXISTS reviews
(
    review_id   bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    film_id     bigint  NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
    user_id     bigint  NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    content     text    NOT NULL,
    is_positive boolean NOT NULL
);

CREATE TABLE IF NOT EXISTS reviews_rating
(
    review_id   bigint  NOT NULL REFERENCES reviews (review_id) ON DELETE CASCADE,
    user_id     bigint  NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    is_positive boolean NOT NULL,
    PRIMARY KEY (review_id, user_id)
);

CREATE TABLE IF NOT EXISTS friends
(
    friendship_request bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id            bigint REFERENCES users (user_id) ON DELETE CASCADE,
    friend_user_id     bigint REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS director
(
    director_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name text NOT NULL
);

CREATE TABLE IF NOT EXISTS directors
(
    director_request bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    director_id bigint REFERENCES director (director_id) ON DELETE CASCADE,
    film_id bigint REFERENCES films (film_id) ON DELETE CASCADE
);
