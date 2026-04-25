package com.example.accounts.Configurations;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String key;


    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username) ///pune username-ul in token
                .setIssuedAt(new Date()) ///data cand a fost emisa cererea
                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*10)) ///data de expirarea
                .signWith(SignatureAlgorithm.HS256,key) ///semneaza cu cheia
                .compact(); ///construieste token-ul
    }
    public String extractUsername(String token){
        return Jwts.parser()
                .setSigningKey(key) ///foloseste keya parsata
                .parseClaimsJws(token)///decodificare token
                .getBody() ///ia continutul
                .getSubject(); ///returneaza username-ul
    }
    public boolean validateToken(String token){
        try{
            extractUsername(token);
            return true;
        }catch(Exception e){
            return false;
        }
    }
}
