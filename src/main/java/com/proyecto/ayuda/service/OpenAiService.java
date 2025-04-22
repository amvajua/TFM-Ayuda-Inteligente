package com.proyecto.ayuda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String modelo;

    @Value("${openai.temperature:0.2}")
    private double temperature;

    @Value("${openai.max_tokens:100}")
    private int maxTokens;

    @Value("${openai.cache.size:50}")
    private int cacheSize;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> cacheSPARQL = Collections.synchronizedMap(new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > cacheSize;
        }
    });

    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10))
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(10))
            .build();

    public String generarConsultaSPARQL(String pregunta) throws IOException {
        if (cacheSPARQL.containsKey(pregunta)) {
            System.out.println("\u26a1 Usando caché para la pregunta: " + pregunta);
            return cacheSPARQL.get(pregunta);
        }

        String prompt = construirPrompt(pregunta);
        String jsonBody = generarJsonRequestDesdePrompt(prompt);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la respuesta de OpenAI: " + response);
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) throw new IOException("Respuesta vacía de OpenAI.");

                JsonNode jsonResponse = objectMapper.readTree(responseBody.string());
                if (jsonResponse.get("choices") == null || jsonResponse.get("choices").isEmpty()) {
                    throw new IOException("OpenAI no generó una consulta SPARQL válida.");
                }

                String consultaSPARQL = jsonResponse.get("choices").get(0).get("message").get("content").asText();

                if (!consultaSPARQL.trim().toUpperCase().contains("SELECT")) {
                    System.out.println("\u26a0 OpenAI generó una respuesta inválida: " + consultaSPARQL);
                    return "{\"error\": \"OpenAI no generó una consulta SPARQL válida.\"}";
                }

                String consultaLimpia = limpiarConsulta(consultaSPARQL);
                cacheSPARQL.put(pregunta, consultaLimpia);
                return consultaLimpia;
            }
        }
    }

    private String generarJsonRequestDesdePrompt(String prompt) throws IOException {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelo);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", prompt);

        messages.add(systemMessage);
        requestBody.put("messages", messages);

        return objectMapper.writeValueAsString(requestBody);
    }

    public String limpiarConsulta(String consulta) {
        String limpia = consulta
                .replaceFirst("(?i)^sparql", "")
                .replaceAll("[`]", "")  // Elimina comillas invertidas
                .replaceAll("“|”", "\"") // Comillas tipográficas a estándar
                .trim();

        Set<String> palabrasClave = new HashSet<>(Arrays.asList(
                "WHERE", "FILTER", "OPTIONAL", "UNION", "GRAPH", "SELECT"
        ));

        String[] lineas = limpia.split("\n");
        StringBuilder corregida = new StringBuilder();

        for (String linea : lineas) {
            linea = linea.trim();

            if (linea.isEmpty() || linea.startsWith("#")) {
                corregida.append(linea).append("\n");
                continue;
            }

            String[] tokens = linea.split("\\s+");

            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i];
                boolean esUltimoDeTriple = linea.endsWith(".") && i == tokens.length - 2;

                // 🔍 LOG de depuración por token
                System.out.println("→ Token detectado: " + token + " | Pos: " + i + " | UltimoTriple: " + esUltimoDeTriple);

                if (esUltimoDeTriple &&
                        !token.startsWith("?") &&
                        !token.startsWith("\"") &&
                        !token.contains(":") &&
                        !token.startsWith("<") &&
                        !palabrasClave.contains(token.toUpperCase()) &&
                        !esNumero(token)) {
                    token = "\"" + token + "\"";
                }

                corregida.append(token).append(" ");
            }

            corregida.append("\n");
        }

        String resultado = corregida.toString().trim();
        System.out.println("✅ Consulta tras limpieza final:\n" + resultado);
        return resultado;
    }

    private boolean esNumero(String valor) {
        try {
            Double.parseDouble(valor);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String cargarPromptDatos() throws IOException {
        ClassPathResource resource = new ClassPathResource("prompts/contexto-general.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public String construirPrompt(String preguntaUsuario) throws IOException {
        String contexto;
        contexto = cargarPromptDatos();
        return contexto + "\n\nPregunta: \"" + preguntaUsuario + "\"\nRespuesta:";
    }
}