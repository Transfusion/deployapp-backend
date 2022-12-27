package io.github.transfusion.deployapp.db.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("1")
public class SignupVerificationToken extends VerificationToken {
}
