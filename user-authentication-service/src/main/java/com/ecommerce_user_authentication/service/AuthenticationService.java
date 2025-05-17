package com.ecommerce_user_authentication.service;

import com.ecommerce_user_authentication.dto.response.UserInfoResponse;
import com.ecommerce_user_authentication.exception.InvalidRoleException;
import com.ecommerce_user_authentication.exception.UserAlreadyExistsException;
import com.ecommerce_user_authentication.model.RoleEntity;
import com.ecommerce_user_authentication.model.RoleEnum;
import com.ecommerce_user_authentication.model.SessionEntity;
import com.ecommerce_user_authentication.model.SessionStatus;
import com.ecommerce_user_authentication.model.UserEntity;
import com.ecommerce_user_authentication.repository.RoleRepository;
import com.ecommerce_user_authentication.repository.SessionRepository;
import com.ecommerce_user_authentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository, SessionRepository sessionRepository, RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public String login(String email, String password) {
        final Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        var userEntity = userRepository.findOneByEmail(email).orElseThrow();
        String token = jwtService.generateToken(auth);
        Date expiration = jwtService.extractExpiration(token);
        SessionEntity session = new SessionEntity(SessionStatus.ACTIVE, token, expiration, userEntity);
        sessionRepository.save(session);
        return token;
    }

    public void logout(String token, String email) {
        Optional<SessionEntity> sessionOptional = sessionRepository.findByTokenAndUser_Email(token, email);
        if (sessionOptional.isEmpty()) {
            log.info("No session found.");
            return;
        }
        SessionEntity session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);
        log.info("Session for token closed in database.");
    }

    @Transactional
    public Optional<UserInfoResponse> signUp(String email, String password) {
        if (userRepository.findOneByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered.");
        }

        // Default role set to USER
        RoleEntity roleEntity = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new InvalidRoleException("Role does not exist."));

        var user = new UserEntity(email, passwordEncoder.encode(password), Set.of(roleEntity));
        var savedUser = userRepository.save(user);

        return Optional.of(UserInfoResponse.from(savedUser));
    }

    public void changePassword(String email, String password, String newPassword){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        var userEntity = userRepository.findOneByEmail(email).orElseThrow();
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
    }

    public SessionStatus validate(final String token, final String email) {
        jwtService.validJWT(token);
        Date expiryAt = jwtService.extractExpiration(token);
        Optional<SessionEntity> sessionOptional = sessionRepository.findByTokenAndUser_Email(token, email);
        if (sessionOptional.isPresent()) {
            SessionEntity session = sessionOptional.get();
            Date now = new Date();
            if (session.getExpiration().after(now) && expiryAt.after(now)) {
                return session.getSessionStatus();
            }
        }
        return SessionStatus.INVALID;
    }


}
