DROP TABLE IF EXISTS "users" CASCADE;
CREATE TABLE "users" (
    "id" uuid NOT NULL DEFAULT gen_random_uuid(),
    "created_on" timestamp NOT NULL DEFAULT now(),
    "username" varchar(50),
    -- nullable in the case of anonymous accounts!
    "email" varchar(254),
    "password" varchar(255),
    "account_verified" bool NOT NULL DEFAULT false,
    "name" varchar(255),
    "last_logged_in" timestamp DEFAULT now(),
    PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX email_unq ON public.users USING btree (email);
CREATE UNIQUE INDEX username_unq ON public.users USING btree (username);

-- placeholder for anonymous users
INSERT INTO "users" ("id", "created_on", "username", "email", "password", "account_verified", "name", "last_logged_in") VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2022-08-01 15:28:10.263126', NULL, NULL, NULL, 'f', 'Anonymous User', '2022-08-01 15:28:10.263126');

DROP TABLE IF EXISTS "auth_provider" CASCADE;
CREATE TABLE "auth_provider" (
    "provider_key" varchar(255) NOT NULL,
    "user_id" uuid NOT NULL,
    "provider_name" varchar(20) NOT NULL,
    "provider_info_name" text,
    CONSTRAINT "auth_provider_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id"),
    PRIMARY KEY ("provider_name","user_id")
);

