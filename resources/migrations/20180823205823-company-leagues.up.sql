CREATE TABLE company (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

       name VARCHAR NOT NULL
);

ALTER TABLE league ADD COLUMN company_id UUID NOT NULL;

ALTER TABLE league ADD CONSTRAINT company_link FOREIGN KEY (company_id) REFERENCES company(id);
