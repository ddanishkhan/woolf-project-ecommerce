package com.ecommerce_user_authentication.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Table(name = "sessions")
@Entity
@Getter
@NoArgsConstructor
public class SessionEntity extends BaseModel {

    private String token;

    private Date expiration;

    @CreationTimestamp
    private Date loginAt;

    @ManyToOne
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Setter
    private SessionStatus sessionStatus;

    public SessionEntity(SessionStatus sessionStatus, String token, Date expiration, UserEntity user) {
        this.sessionStatus = sessionStatus;
        this.token = token;
        this.expiration = expiration;
        this.user = user;
    }
}