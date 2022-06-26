package io.github.transfusion.deployapp.db.entities;

import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AuthProviderId implements Serializable {
    private static final long serialVersionUID = -4823643903866212097L;
    @Column(name = "provider_name", nullable = false, length = 20)
    private String providerName;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AuthProviderId entity = (AuthProviderId) o;
        return Objects.equals(this.userId, entity.userId) &&
                Objects.equals(this.providerName, entity.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, providerName);
    }

}