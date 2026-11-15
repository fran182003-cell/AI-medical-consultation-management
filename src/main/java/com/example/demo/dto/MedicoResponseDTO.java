package com.example.demo.dto;

import java.util.UUID;

public class MedicoResponseDTO {
    //Cuando tu React pide la lista de médicos (GET /admin/medicos) para que no envie contraseñas
    
    private UUID id;
    private String username;

    // --- CONSTRUCTORES ---
    public MedicoResponseDTO() {}

    public MedicoResponseDTO(UUID id, String username) {
        this.id = id;
        this.username = username;
    }

    // --- GETTERS Y SETTERS ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}