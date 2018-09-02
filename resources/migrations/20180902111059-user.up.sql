CREATE TABLE users (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       -- email should be migrated out from the other table first
       email VARCHAR NOT NULL UNIQUE,
       oauth2_token VARCHAR,
       -- do we need default values for this at all?
       created_at TIMESTAMP DEFAULT now(),
       last_login TIMESTAMP DEFAULT now()
);

--TODO: should be NOT NULL as well whenever they are migrated
ALTER TABLE player
ADD COLUMN user_id UUID;

ALTER TABLE player
ADD CONSTRAINT user_link
FOREIGN KEY (user_id)
REFERENCES users(id);
