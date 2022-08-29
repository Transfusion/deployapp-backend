CREATE TABLE "ftp_credentials" (
    "id" uuid NOT NULL,
    CONSTRAINT "storage_credentials_id_fkey" FOREIGN KEY ("id") REFERENCES "public"."storage_credentials"("id"),

    "server" varchar(255) NOT NULL,
    "port" integer NOT NULL DEFAULT 21,
    "username" varchar(255) NOT NULL,
    "password" varchar(255) NOT NULL,
    "directory" varchar(4096) NOT NULL,
    "base_url" varchar(2048) NOT NULL,

    PRIMARY KEY ("id")
);