package com.example.demo;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class TinkConfig {

    // Leemos la propiedad que definimos en el paso 1
	@Value("${app.tfg.seguridad.ruta-claves:./claves/tink_keyset.json}")
    private String rutaClave;

    @Bean
    public Aead aead() throws GeneralSecurityException, IOException {
        // 1. Registrar configuración de Tink
        AeadConfig.register();

        // 2. Verificar que el archivo existe
        File archivo = new File(rutaClave);
        if (!archivo.exists()) {
            throw new RuntimeException("ERROR FATAL: No encuentro el archivo de claves en: " + rutaClave);
        }

        // 3. Cargar las claves del JSON
        KeysetHandle keysetHandle = CleartextKeysetHandle.read(
                JsonKeysetReader.withFile(archivo)
        );

        // 4. Devolver el objeto listo para encriptar
        return keysetHandle.getPrimitive(Aead.class);
    }
}


