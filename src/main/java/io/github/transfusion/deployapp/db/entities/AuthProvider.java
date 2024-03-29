package io.github.transfusion.deployapp.db.entities;

import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "auth_provider")
public class AuthProvider {
    @EmbeddedId
    private AuthProviderId id;

    @MapsId("userId")
    // many authproviders to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider_key", nullable = false)
     private String providerKey;

    @Column(name = "provider_info_name")
    @Type(type = "org.hibernate.type.TextType")
    private String providerInfoName;

    public String getProviderInfoName() {
        return providerInfoName;
    }

    public void setProviderInfoName(String providerInfoName) {
        this.providerInfoName = providerInfoName;
    }

    public AuthProviderId getId() {
        return id;
    }

    public void setId(AuthProviderId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

}