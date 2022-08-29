-- to flesh out in later migrations
DROP TABLE IF EXISTS "organizations" CASCADE;
CREATE TABLE "organizations" (
    "id" uuid NOT NULL DEFAULT gen_random_uuid(),
    "created_on" timestamp NOT NULL DEFAULT now(),
    "name" varchar(255),
    PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX orgname_unq ON public.organizations USING btree (name);