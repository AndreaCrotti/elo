CREATE TABLE game_kind (
       id UUID PRIMARY KEY,
       name VARCHAR(16) NOT NULL,
       variant VARCHAR(16),
       draw_allowed BOOLEAN NOT NULL,
       team_required BOOLEAN NOT NULL,
       min_points INTEGER DEFAULT 0,
       max_points INTEGER DEFAULT 10
);

ALTER TABLE league
ADD COLUMN game_kind VARCHAR(16) REFERENCES game_kind(name);
