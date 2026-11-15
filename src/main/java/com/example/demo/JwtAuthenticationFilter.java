package com.example.demo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
               
                
                if (jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    String role = jwtUtils.getRoleFromToken(token); 

              

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        
                      
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                        
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                Collections.singletonList(authority)
                        );
                        
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        System.out.println("--> Autenticación establecida en el Contexto de Seguridad");
                    }
                } else {
                     System.out.println("--> El token NO es válido según jwtUtils");
                }
            }
        } catch (Exception e) {
            System.out.println("Error en el filtro JWT: " + e.getMessage());
        }
        
        // IMPORTANTE: Esta línea debe ejecutarse SIEMPRE, haya token o no.
        filterChain.doFilter(request, response);
    }
}