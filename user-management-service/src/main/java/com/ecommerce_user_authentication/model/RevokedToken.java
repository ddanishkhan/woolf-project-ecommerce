package com.ecommerce_user_authentication.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "revoked_tokens")
@NoArgsConstructor
public class RevokedToken {

    @Id
    @Column(nullable = false, unique = true)
    private String jti;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiryTime; // Original expiry time of the token


    public RevokedToken(String jti, Date expiryTime) {
        this.jti = jti;
        this.expiryTime = expiryTime;
    }

}
