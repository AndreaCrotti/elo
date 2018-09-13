-- this column is already in user anyway
ALTER TABLE player DROP COLUMN email;

-- make the email in user unique?
ALTER TABLE users ALTER COLUMN email set NOT NULL;
