package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Ruta del archivo de log (puedes cambiarla)
    private static final String LOG_FILE = "log_seguridad.txt";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        // 1. Obtener la IP del cliente
    	
        String ipCliente = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ipCliente)) {
        	ipCliente = "127.0.0.1";
        }
        
        // 2. Obtener los detalles de los errores de @Valid
        String detallesErrores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        // 3. Escribir en el archivo .txt
        escribirLog(ipCliente, detallesErrores);

        // 4. Devolver ÚNICAMENTE el código de error 400, sin cuerpo (JSON vacío)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private void escribirLog(String ip, String errores) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println("[" + timestamp + "] IP: " + ip + " - FALLO VALIDACIÓN: " + errores);
            
        } catch (IOException e) {
            System.err.println("Error escribiendo en el log: " + e.getMessage());
        }
    }
    
 
    
    
    
    
}