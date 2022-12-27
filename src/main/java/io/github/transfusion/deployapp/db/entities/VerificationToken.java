package io.github.transfusion.deployapp.db.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_token")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        discriminatorType = DiscriminatorType.INTEGER,
        name = "type"
//        https://stackoverflow.com/questions/56044518/hibernate-ddl-validation-smallint-vs-int2
//        columnDefinition = "TINYINT(1)"
)
public class VerificationToken {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id"/*, insertable = false, updatable = false*/)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @Column(name = "expiry", nullable = false)
    private Instant expiry;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

//    @Column(name = "type", nullable = false)
//    private Integer type;
//
//    public Integer getType() {
//        return type;
//    }
//
//    public void setType(Integer type) {
//        this.type = type;
//    }

//    @Column(name = "email", nullable = false, length = 254)
//    private String email;
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiry() {
        return expiry;
    }

    public void setExpiry(Instant expiry) {
        this.expiry = expiry;
    }

    /* we may decouple AccountVerificationService into its own microservice;
    it may not have access to the actual User Entity.*/
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}