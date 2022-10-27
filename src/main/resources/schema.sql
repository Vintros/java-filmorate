CREATE TABLE IF NOT EXISTS mpa
(
    mpa_id bigint PRIMARY KEY,
    name   text
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         text         NOT NULL,
    description  varchar(200) NOT NULL,
    release_date date         NOT NULL,
    duration     bigint       NOT NULL,
    mpa_id       integer REFERENCES mpa (mpa_id)
);

CREATE TABLE IF NOT EXISTS genre
(
    genre_id bigint PRIMARY KEY,
    name     text
);

CREATE TABLE IF NOT EXISTS genres
(
    film_id  bigint REFERENCES films (film_id),
    genre_id integer REFERENCES genre (genre_id),
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

CREATE TABLE IF NOT EXISTS friends
(
    friendship_request bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id            bigint REFERENCES users (user_id),
    friend_user_id     bigint REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    film_id bigint REFERENCES films (film_id),
    user_id bigint REFERENCES users (user_id),
    PRIMARY KEY (film_id, user_id)
);

