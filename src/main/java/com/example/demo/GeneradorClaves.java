/*package com.example.demo; // <--- Asegúrate de que esto coincida con tu carpeta real

import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.PredefinedAeadParameters;
import java.io.File;

public class GeneradorClaves {
    public static void main(String[] args) throws Exception {
        
        // 1. IMPORTANTE: Registrar la configuración AEAD primero
        AeadConfig.register();

        // 2. Generar la clave 
        // En lugar de AeadKeyTemplates, usamos PredefinedAeadParameters
       KeysetHandle keysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM);

        // 3. Ruta del archivo (carpeta ./claves, ignorada por git)
        File archivo = new File("./claves/tink_keyset.json");

        // Crear carpetas si no existen
        archivo.getParentFile().mkdirs();

        // 4. Escribir el archivo
        // guardar claves en texto claro es peligroso, pero para generar tu archivo inicial está bien.
        CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(archivo));

        System.out.println("✅ Clave generada exitosamente en: " + archivo.getAbsolutePath());
    }
}*/