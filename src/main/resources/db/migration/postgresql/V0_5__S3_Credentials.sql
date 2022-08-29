CREATE TABLE "s3_credentials" (
    "id" uuid NOT NULL,
    CONSTRAINT "storage_credentials_id_fkey" FOREIGN KEY ("id") REFERENCES "public"."storage_credentials"("id"),

    "server" varchar(255) NOT NULL,
    "aws_region" varchar(30),
    "access_key" varchar(30) NOT NULL,
    "secret_key" varchar(50) NOT NULL,
    "bucket" varchar(100) NOT NULL,

    PRIMARY KEY ("id")
);