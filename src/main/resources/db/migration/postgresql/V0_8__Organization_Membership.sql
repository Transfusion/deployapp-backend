CREATE TABLE "public"."organization_membership" (
    "organization_id" uuid NOT NULL,
    "user_id" uuid NOT NULL,
    "joined_on" timestamp NOT NULL DEFAULT now(),
    "role" varchar(10) NOT NULL,
    CONSTRAINT "organization_membership_organization_id_fkey" FOREIGN KEY ("organization_id") REFERENCES "public"."organizations"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "organization_membership_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY ("organization_id","user_id")
);

