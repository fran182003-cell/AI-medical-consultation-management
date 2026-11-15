package com.example.demo;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {

	
	@Value("${jwt.secret.path}")
    private String secretKeyPath;

    @Value("${jwt.expiration}")
    private long expiration;
    
    private Key signingKey;
    
    @PostConstruct
    public void init() {
        try {
            // 2. Buscamos el archivo en la ruta especificada
            Path rutaArchivo = Paths.get(secretKeyPath);
            
            // 3. Leemos los bytes crudos generados por tu script anterior
            byte[] keyBytes = Files.readAllBytes(rutaArchivo);
            
            // 4. Reconstruimos la clave matemática
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            
            System.out.println("🔐 Clave JWT cargada exitosamente desde el archivo local: " + secretKeyPath);
        } catch (Exception e) {
            // Si el archivo no existe o la ruta está mal, abortamos el arranque por seguridad
            throw new RuntimeException("CRÍTICO: No se pudo leer el archivo de la clave JWT en la ruta: " + secretKeyPath, e);
        }
    }



    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)   
                .getBody()
                .get("role", String.class);
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}
