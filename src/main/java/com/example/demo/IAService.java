package com.example.demo;

import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.HashMap;
import java.util.Map;
@Service
public class IAService {

    // Ahora Spring Boot buscará esta propiedad en el archivo de configuración
    @Value("${app.ia.url}")
    private String colabApiUrl;

    public String generarRespuesta(String promptMedico) {
        
        // Verificación de seguridad por si se olvida configurar
        if (colabApiUrl == null || colabApiUrl.contains("xxxx")) {
            return "ERROR CONFIGURACIÓN: Debes actualizar la propiedad app.ia.url en el archivo de configuración.";
        }

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000); 
            factory.setReadTimeout(600000); // 10 minutos

            RestTemplate restTemplate = new RestTemplate(factory); //
                   
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); //

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("prompt", promptMedico); //

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers); //

            // Usamos la variable dinámica leída del properties externo
            ResponseEntity<Map> response = restTemplate.postForEntity(
                colabApiUrl, 
                requestEntity, 
                Map.class
            ); //

            if (response.getBody() != null && response.getBody().containsKey("respuesta")) {
                return response.getBody().get("respuesta").toString();
            } else {
                return "La IA no devolvió una respuesta válida.";
            } //

        } catch (Exception e) {
            e.printStackTrace();
            return "Error de conexión con la IA (Colab): " + e.getMessage();
        }
    }
}