package com.usermanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    private static final int EXPIRATION_MINUTES = 60; // Token valid for 60 minutes

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    public PasswordResetToken() {
        this.token = UUID.randomUUID().toString();
        this.expiryDate = calculateExpiryDate(EXPIRATION_MINUTES);
    }

    public PasswordResetToken(User user) {
        this();
        this.user = user;
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Date now = new Date();
        long expiryTimeInMilliseconds = now.getTime() + ((long) expiryTimeInMinutes * 60 * 1000);
        return new Date(expiryTimeInMilliseconds);
    }

    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }

}
