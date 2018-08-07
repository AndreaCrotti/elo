CREATE TABLE player (
       id SERIAL PRIMARY KEY,
       name VARCHAR NOT NULL,
       email VARCHAR NOT NULL
);

CREATE TABLE league (
       id SERIAL PRIMARY KEY,
       name VARCHAR NOT NULL
);

CREATE TABLE league_players (
       league_id INTEGER REFERENCES league (id),
       player_id INTEGER REFERENCES player (id)
);

CREATE TABLE game (
       id SERIAL PRIMARY KEY,

       -- when leagues are actually used we can make this not nullable
       league_id INTEGER REFERENCES league (id),
       p1 INTEGER NOT NULL REFERENCES player (id),
       p2 INTEGER NOT NULL REFERENCES player (id),

       p1_team VARCHAR NOT NULL,
       p2_team VARCHAR NOT NULL,

       p1_goals INTEGER NOT NULL,
       p2_goals INTEGER NOT NULL,

       played_at TIMESTAMP DEFAULT now(),
       recorded_at TIMESTAMP DEFAULT now()
 );
