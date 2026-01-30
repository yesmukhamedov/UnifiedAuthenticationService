CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       user_id UUID NOT NULL UNIQUE,
                       status VARCHAR(50),
                       created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE emails (
                        id SERIAL PRIMARY KEY,
                        user_id UUID NOT NULL,
                        email_ciphertext BYTEA NOT NULL,
                        email_iv BYTEA NOT NULL,
                        verified BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE user_phones (
                             id SERIAL PRIMARY KEY,
                             user_id UUID NOT NULL,
                             phone_ciphertext BYTEA NOT NULL,
                             phone_iv BYTEA NOT NULL,
                             verified BOOLEAN DEFAULT FALSE,
                             created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE user_passwords (
                                id SERIAL PRIMARY KEY,
                                user_id UUID NOT NULL,
                                password_hash TEXT NOT NULL,
                                created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE user_profiles (
                               id SERIAL PRIMARY KEY,
                               user_id UUID NOT NULL,
                               first_name_ciphertext BYTEA NOT NULL,
                               last_name_ciphertext BYTEA NOT NULL,
                               birthdate_ciphertext BYTEA NOT NULL,
                               iv BYTEA NOT NULL,
                               created_at TIMESTAMP DEFAULT now()
);
