CREATE TABLE game (
       --TODO: add auto incrementing
       id SERIAL,
       p1_name VARCHAR NOT NULL,
       p2_name VARCHAR NOT NULL,
       p1_team VARCHAR NOT NULL,
       p2_team VARCHAR NOT NULL,
       p1_goals INT NOT NULL,
       p2_goals INT NOT NULL,
       played_at TIMESTAMP DEFAULT now()
 );
