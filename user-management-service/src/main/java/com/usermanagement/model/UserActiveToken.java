package com.usermanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "user_active_tokens")
@NoArgsConstructor
public class UserActiveToken {

    @Id
    @Column(nullable = false, unique = true)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiryTime; // Original expiry time of the token

    public UserActiveToken(String jti, User user, Date expiryTime) {
        this.jti = jti;
        this.user = user;
        this.expiryTime = expiryTime;
    }

}
