package com.xuxd.kafka.console.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String ISSUER = "kafka-console-ui";
    private static final long EXPIRE_TIME = 5 * 24 * 60 * 60 * 1000;
    private static final String PRIVATE_KEY = "~hello!kafka=console^ui";

    public static String sign(String username){
        Map<String,Object> header = new HashMap<>();
        header.put("typ","JWT");
        header.put("alg","HS256");
        Map<String,Object> claims = new HashMap<>();
        claims.put("username", username);
        return Jwts.builder()
                .setIssuer(ISSUER)
                .setHeader(header)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, PRIVATE_KEY)
                .compact();
    }

    public static String parse(String token){
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(PRIVATE_KEY)
                    .parseClaimsJws(token).getBody();
            return (String) claims.get("username");
        }catch (Exception e){
            return null;
        }
    }
}
