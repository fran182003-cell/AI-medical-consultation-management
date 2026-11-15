package com.example.demo; // <--- OJO: Pon tu paquete correcto aquí

import com.google.crypto.tink.Aead;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, byte[]> {

    private static Aead aead;

    
    @Autowired
    public void setAead(Aead aead) {
        AttributeEncryptor.aead = aead;
    }

   
    public byte[] convertToDatabaseColumn(String atributo) {
        if (atributo == null) return null;
        try {
            // Encriptar: String -> Bytes Cifrados
            return aead.encrypt(atributo.getBytes(StandardCharsets.UTF_8), null);
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar datos médicos", e);
        }
    }

  
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length == 0) {
            return null;
        }
        try {
            return new String(aead.decrypt(dbData, null));
        } catch (GeneralSecurityException e) {
            // Logueamos el error pero NO lanzamos la excepción
            System.err.println("⚠️ No se pudo desencriptar un campo. Datos antiguos o corruptos detectados.");
            return "[Error: Dato no legible]"; 
        }
    }
}
