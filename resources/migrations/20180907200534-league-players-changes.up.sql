ALTER TABLE league_players
ADD COLUMN id UUID PRIMARY KEY DEFAULT uuid_generate_v4();

ALTER TABLE league_players
ALTER COLUMN league_id SET NOT NULL;

ALTER TABLE league_players
ALTER COLUMN player_id SET NOT NULL;
