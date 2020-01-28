CREATE TABLE game_kind (
       id UUID PRIMARY KEY,
       name VARCHAR(16) NOT NULL,
       config JSONB NOT NULL
);

ALTER TABLE league
ADD COLUMN game_kind UUID REFERENCES game_kind(id);
