package com.rushtix.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.jwt-expiration-ms:864000000}")
    private long jwtExpirationMs;

    public String generateToken(UUID userId, String email, String role){
        Date now=new Date();
        Date expiryDate=new Date(now.getTime()+jwtExpirationMs);
        Key key= Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email",email)
                .claim("role","ROLE_"+role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String authtoken)
    {
        try{
            Key key= Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authtoken);
            return true;
        }
        catch (JwtException| IllegalArgumentException e){
            // Log the exception
            return false;
        }
    }

    public Claims getClaimsFromToken(String token){
        Key key= Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
