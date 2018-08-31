ALTER TABLE game
ALTER COLUMN p1_team DROP NOT NULL;

ALTER TABLE game
ALTER COLUMN p2_team DROP NOT NULL;

ALTER TABLE game
RENAME p1_team TO p1_using;

ALTER TABLE game
RENAME p2_team to p2_using;

ALTER TABLE game
RENAME p1_goals to p1_points;

ALTER TABLE game
RENAME p2_goals to p2_points;


CREATE TYPE game_type AS ENUM ('fifa', 'street-fighter');

ALTER TABLE league
ADD COLUMN game_type game_type NOT NULL default 'fifa';

-- in case we want to store what was the version of the game played as well
ALTER TABLE league
ADD COLUMN game_version VARCHAR;
