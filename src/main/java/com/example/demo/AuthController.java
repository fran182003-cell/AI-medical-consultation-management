package com.example.demo;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ¡NUEVOS IMPORTS CLAVE!
import com.example.demo.dto.LoginRequestDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private MedicoRepository medicoRepo;
    @Autowired private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
       
        Medico medico = medicoRepo.findByUsername(loginRequest.getUsername()).orElse(null);
        
        // Usuario administrador de DEMOSTRACIÓN (admin/1234). No usar en un entorno real.
        if ("admin".equals(loginRequest.getUsername()) && "1234".equals(loginRequest.getPassword())) {
            String token = jwtUtils.generateToken(loginRequest.getUsername(), "ROLE_ADMIN");
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        }
        
        if (medico != null && medico.getPassword().equals(loginRequest.getPassword())) {
            String token = jwtUtils.generateToken(medico.getUsername(), "ROLE_USER");
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }
}
