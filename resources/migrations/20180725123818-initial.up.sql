CREATE TABLE game (
       --TODO: add auto incrementing
       id SERIAL,
       winner VARCHAR NOT NULL,
       loser VARCHAR NOT NULL,
       winning_team VARCHAR NOT NULL,
       losing_team VARCHAR NOT NULL,
       winning_goals INT NOT NULL,
       losing_goals INT NOT NULL,
       played_at TIMESTAMP DEFAULT now()
 );
