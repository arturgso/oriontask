package br.com.oriontask.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.oriontask.backend.dto.AuthResponseDTO;
import br.com.oriontask.backend.dto.LoginRequestDTO;
import br.com.oriontask.backend.dto.SignupRequestDTO;
import br.com.oriontask.backend.model.Users;
import br.com.oriontask.backend.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCrypt;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersRepository usersRepository;

    @Value("${jwt.secret:change-me}")
    private String jwtSecret;

    @Value("${jwt.expMinutes:60}")
    private int expMinutes;

    // minimal disposable email domain list; configurable via property in future
    private static final Set<String> DISPOSABLE_DOMAINS = new HashSet<>(Arrays.asList(
        "mailinator.com",
        "10minutemail.com",
        "guerrillamail.com",
        "yopmail.com",
        "temp-mail.org",
        "fakemail.net",
        "trashmail.com"
    ));

    @Transactional
    public AuthResponseDTO signup(SignupRequestDTO req) {
        usersRepository.findByUsername(req.username()).ifPresent(u -> {
            throw new IllegalArgumentException("Username unavailable");
        });
        usersRepository.findByEmail(req.email()).ifPresent(u -> {
            throw new IllegalArgumentException("Email unavailable");
        });

        if (isDisposableEmail(req.email())) {
            throw new IllegalArgumentException("Disposable/temporary emails are not allowed");
        }

        String passwordHash = BCrypt.hashpw(req.password(), BCrypt.gensalt());

        Users user = Users.builder()
                .name(req.name())
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordHash)
                .build();

        user = usersRepository.save(user);

        String token = generateToken(user);

        return new AuthResponseDTO(token, user.getId(), user.getUsername(), user.getName());
    }

    public AuthResponseDTO login(LoginRequestDTO req) {
        Optional<Users> userOpt = isEmail(req.login())
            ? usersRepository.findByEmail(req.login())
            : usersRepository.findByUsername(req.login());

        Users user = userOpt.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!BCrypt.checkpw(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = generateToken(user);
        return new AuthResponseDTO(token, user.getId(), user.getUsername(), user.getName());
    }

    private String generateToken(Users user) {
        Algorithm alg = Algorithm.HMAC256(jwtSecret);
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("username", user.getUsername())
                .withIssuedAt(java.util.Date.from(now))
                .withExpiresAt(java.util.Date.from(now.plus(expMinutes, ChronoUnit.MINUTES)))
                .sign(alg);
    }

    private boolean isEmail(String value) {
        return value.contains("@");
    }

    private boolean isDisposableEmail(String email) {
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase();
        return DISPOSABLE_DOMAINS.contains(domain);
    }
}
