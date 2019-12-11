CREATE TABLE game_kind (
       name VARCHAR(16) PRIMARY KEY,
       draw_allowed BOOLEAN NOT NULL,
       team_required BOOLEAN NOT NULL,
       min_points INTEGER DEFAULT 0,
       max_points INTEGER DEFAULT 10
);
