CREATE TABLE users (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       -- email should be migrated out from the other table first
       email VARCHAR NOT NULL,
       oauth2_token VARCHAR,
       -- do we need default values for this at all?
       created_at TIMESTAMP DEFAULT now(),
       last_login TIMESTAMP DEFAULT now(),
       active BOOLEAN default true
);

--TODO: should be NOT NULL as well whenever they are migrated
ALTER TABLE player ADD COLUMN user_id UUID;

ALTER TABLE player ADD CONSTRAINT user_link
FOREIGN KEY (user_id) REFERENCES users(id);

CREATE TABLE league_admins (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       league_id UUID NOT NULL REFERENCES league(id),
       user_id UUID NOT NULL REFERENCES users(id)
);

ALTER TABLE league_admins
ADD CONSTRAINT unique_user_league UNIQUE (league_id, user_id);

CREATE TABLE company_users (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
       company_id UUID NOT NULL REFERENCES company(id),
       user_id UUID NOT NULL REFERENCES users(id)
);

ALTER TABLE company_users
ADD CONSTRAINT unique_users_company UNIQUE (company_id, user_id);
