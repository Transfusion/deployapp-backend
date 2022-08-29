CREATE TABLE "storage_credentials" (
    "id" uuid NOT NULL,
    "name" varchar(100),

    "user_id" uuid,
    CONSTRAINT "storage_credentials_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id"),

    "organization_id" uuid,
    CONSTRAINT "storage_credentials_organization_id_fkey" FOREIGN KEY ("organization_id") REFERENCES "public"."organizations"("id"),

    "created_on" timestamp NOT NULL DEFAULT now(),
    "checked_on" timestamp NOT NULL DEFAULT now(),
    "last_used" timestamp,

    PRIMARY KEY ("id"),

    CONSTRAINT "user_or_org_not_null" check (organization_id is not null or user_id is not null)
);