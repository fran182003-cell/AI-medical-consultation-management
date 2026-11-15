package com.example.demo;

import com.example.demo.JwtAuthenticationFilter; // Asegúrate de que la ruta sea correcta
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // <--- Necesario para activar CORS
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource; // <--- ESTE ES EL IMPORT CORRECTO (Sin 'reactive')

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
           
            .cors(Customizer.withDefaults()) 
            
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            	    // 1. APIs Públicas (la consola H2 se deja abierta SOLO para desarrollo/demostración)
            	    .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
            	    
            	    // 2. APIs Protegidas (Tus datos)
            	    .requestMatchers("/api/admin/**").hasRole("ADMIN") 
            	    .requestMatchers("/api/citas/**").hasAnyRole("USER", "ADMIN") 
            	    
            	    // 3. Rutas del Frontend (La carcasa visual, React ya se encarga de protegerlas)
            	    .requestMatchers("/", "/index.html", "/static/**", "/*.json", "/*.png", "/*.ico", "/manifest.json", "/dashboard", "/nueva-cita", "/admin/**").permitAll()
            	    
            	    // 4. Todo lo demás bloqueado
            	    .anyRequest().authenticated()
            	)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        // Ahora esto funcionará porque el tipo de retorno coincide con el import correcto
        return source; 
    }
}