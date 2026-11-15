package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MedicoRequestDTO {
    //Este lo usaremos en el panel de administración cuando el admin cree un nuevo médico

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 30, message = "El usuario debe tener entre 4 y 30 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String password;

    // --- GETTERS Y SETTERS ---
    public String getUsername() { 
        return username; 
        }
    public void setUsername(String username) { 
        this.username = username; 
        }
    
    public String getPassword() { 
        return password; 
        }
    public void setPassword(String password) { 
        this.password = password; 
        }
}