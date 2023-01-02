package io.github.transfusion.deployapp.db.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("2")
public class ChangeEmailVerificationToken extends VerificationToken {
}
