package com.example.demo;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.crypto.SecretKey;
/*
public class Generador_clave_token_firma {
    
    // Método MAIN: Sirve para ejecutar esta clase de forma independiente
    public static void main(String[] args) {
        try {
            // 1. Definimos la ruta donde se guardará el archivo en tu PC
            // Puedes cambiarla a "C:/TFG/claves/jwt_secret.key" si lo prefieres
            Path rutaArchivo = Paths.get("./claves/jwt_secret.key");

            // 2. Generamos una clave criptográficamente perfecta para el algoritmo HS256
            SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            
            // 3. Extraemos los bytes en bruto de esa clave
            byte[] keyBytes = key.getEncoded();

            // 4. Guardamos los bytes en el archivo
            Files.write(rutaArchivo, keyBytes);

            System.out.println("✅ ¡Éxito! Clave generada y guardada de forma segura en: " + rutaArchivo.toAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar o guardar la clave: " + e.getMessage());
        }
    }
}*/