package com.example.backend.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.backend.principal.Role;
import com.example.backend.talent.model.entity.Talent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.example.backend.jwt.JwtConstant.TOKEN_ISSUE;
import static com.example.backend.jwt.JwtConstant.*;

/**
 *  Jwt provider for working with jwt-token
 *
 * @version 1.0
 * @author Dmytro Teliukov
 *
 * */
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Generate JWT-token for authorization our talent
     * P.S.: In the future when we will have more than 1 role, we should change ROLE_CLAIM and add parameter role
     *
     * @param talent Talent
     *
     * @return jwt token
     * */
    public String generateJwtToken(Talent talent) {
        return JWT.create()
                .withIssuer(TOKEN_ISSUE)
                .withAudience()
                .withIssuedAt(new Date())
                .withSubject(talent.getEmail())
                .withClaim(TALENT_ID_CLAIM, talent.getId())
                .withClaim(ROLE_CLAIM, Role.TALENT.name())
                .withClaim(FIRSTNAME_CLAIM, talent.getFirstname())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(secret.getBytes()));
    }

    /**
     * Create authentication for filter authentication
     *
     * @param email Talent email
     * @param authority Talent authority
     * @param request Request for filter
     *
     * @return jwt token
     * */
    public Authentication getAuthentication(String email, GrantedAuthority authority, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(email, null, List.of(authority));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

    /**
     * Check if jwt-token is valid
     *
     * @param email Talent email
     * @param token JWT-Token
     *
     * @return is expired token
     * */
    public boolean isTokenValid(String email, String token) {
        JWTVerifier verifier = getVerifier();
        return StringUtils.isNotEmpty(email) &&
                !isTokenExpired(verifier, token) &&
                getSubject(token).equals(email);
    }

    /**
     * Get subject(email) from jwt-token
     *
     * @param token JWT-Token
     *
     * @return email
     * */
    public String getSubject(String token) {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token).getSubject();
    }

    /**
     * Check if jwt-token is expired
     *
     * @param verifier jwt-verifier
     * @param token JWT-Token
     *
     * @return is expired token
     * */
    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    /**
     * Get authority(role) from jwt-token
     *
     * @param token JWT-Token
     *
     * @return authority
     * */
    public GrantedAuthority getAuthority(String token) {
        return new SimpleGrantedAuthority(getRoleFromToken(token));
    }


    /**
     * Parse role from jwt-token
     *
     * @param token JWT-Token
     *
     * @return role
     * */
    private String getRoleFromToken(String token) {
        JWTVerifier verifier = getVerifier();
        return verifier.verify(token).getClaim(ROLE_CLAIM).asString();
    }

    /**
     * Get verifier for parsing jwt-token
     *
     * @return jwt-verifier
     * */
    private JWTVerifier getVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(secret);
            verifier = JWT.require(algorithm).withIssuer(TOKEN_ISSUE).build();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException(TOKEN_NOT_VERIFIED_MESSAGE);
        }
        return verifier;
    }
}

