package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PromptRequestDTO {

    @NotBlank(message = "El texto de la consulta no puede estar vacío")
    @Size(max = 2000, message = "El texto es demasiado largo para ser procesado por la IA")
    private String prompt;

    // --- GETTERS Y SETTERS ---
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}
