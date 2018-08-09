CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE player (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       name VARCHAR NOT NULL,
       email VARCHAR NOT NULL
);

CREATE TABLE league (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       name VARCHAR NOT NULL
);

CREATE TABLE league_players (
       league_id UUID REFERENCES league (id),
       player_id UUID REFERENCES player (id)
);

CREATE TABLE game (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

       -- when leagues are actually used we can make this not nullable
       league_id UUID REFERENCES league (id),
       p1 UUID NOT NULL REFERENCES player (id),
       p2 UUID NOT NULL REFERENCES player (id),

       p1_team VARCHAR NOT NULL,
       p2_team VARCHAR NOT NULL,

       p1_goals INTEGER NOT NULL,
       p2_goals INTEGER NOT NULL,

       played_at TIMESTAMP DEFAULT now(),
       recorded_at TIMESTAMP DEFAULT now()
 );
