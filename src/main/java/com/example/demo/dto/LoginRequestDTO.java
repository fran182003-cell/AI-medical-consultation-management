package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @NotBlank(message = "El usuario no puede estar vacío")
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
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
