package io.github.transfusion.deployapp.db.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "organization_membership")
public class OrganizationMembership {
    @EmbeddedId
    private OrganizationMembershipId id;

    @MapsId("organizationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_on", nullable = false)
    private Instant joinedOn;

    @Column(name = "role", nullable = false, length = 10)
    private String role;

    public OrganizationMembershipId getId() {
        return id;
    }

    public void setId(OrganizationMembershipId id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getJoinedOn() {
        return joinedOn;
    }

    public void setJoinedOn(Instant joinedOn) {
        this.joinedOn = joinedOn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}