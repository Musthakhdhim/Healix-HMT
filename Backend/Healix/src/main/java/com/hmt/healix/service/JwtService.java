package com.hmt.healix.service;

import com.hmt.healix.config.JwtConfig;
import com.hmt.healix.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@AllArgsConstructor
@Slf4j
public class JwtService {

    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;

    public String extractEmail(String token) {
        String email = extractClaim(token, Claims::getSubject);
        log.debug("Extracted email from token: {}", email);
        return email;
    }

    public String extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        log.debug("Extracted role from token: {}", role);
        return role;
    }

    private Claims extractAllClaims(String token) {
        log.trace("Extracting all claims from token");
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.trace("Extracting specific claim from token");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.info("Generating JWT for user: {}", userDetails.getUsername());
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpiration()))
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                .compact();
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Generating JWT with role for user: {}", userDetails.getUsername());
        Map<String, Object> extraClaims = new HashMap<>();
        userRepository.findByEmail(userDetails.getUsername())
                .ifPresent(user -> {
                    log.debug("Adding role claim: {}", user.getRole().name());
                    extraClaims.put("role", user.getRole().name());
                });
        return generateToken(extraClaims, userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        boolean valid = email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        log.debug("Token validity for user {}: {}", userDetails.getUsername(), valid);
        return valid;
    }

    public boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        if (expired) {
            log.warn("Token has expired");
        }
        return expired;
    }

    private Date extractExpiration(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        log.debug("Token expiration date: {}", expiration);
        return expiration;
    }

    public String getTokenFromAuthorization(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Token not found in request header");
            throw new RuntimeException("token not found in the request");
        }
        String token = authHeader.replace("Bearer ", "");
        log.debug("Extracted token from Authorization header");
        return token;
    }
}
