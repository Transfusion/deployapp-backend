CREATE TABLE "public"."verification_token" (
    "id" uuid NOT NULL,
    "user_id" uuid NOT NULL,
    "expiry" timestamp NOT NULL,
    "created_on" timestamp NOT NULL DEFAULT now(),
--    "email" varchar(254) NOT NULL,
    CONSTRAINT "verification_token_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY ("id")
);
