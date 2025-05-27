package com.proyecto.ayuda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.*;

@Service
public class SparqlService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Ejecuta una consulta SPARQL dinámica contra el endpoint de Ontop.
     *
     * @param sparql Consulta SPARQL generada a partir del lenguaje natural.
     * @return Mapa con los resultados obtenidos desde Oracle, estructurados por variable.
     * @throws IOException si falla la conexión o el parseo de respuesta.
     */

    // Ejecución consultas Ontop dinámicas
    public Map<String, Object> ejecutarConsultaOntop(String sparql) throws IOException {
        System.out.println("CONSULTA ENVIADA A ONTOP:\n" + sparql); // LOG

        String endpointUrl = "http://localhost:8080/sparql";
        /*URL url = new URL(endpointUrl + "?query=" + URLEncoder.encode(sparql, "UTF-8"));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");*/
        // Cuerpo de la petición
        String encodedQuery = "query=" + URLEncoder.encode(sparql, "UTF-8");

        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/sparql-results+json");
        connection.setDoOutput(true);

       // Enviar el cuerpo
        connection.getOutputStream().write(encodedQuery.getBytes("UTF-8"));

        //
        //connection.setRequestProperty("Accept", "application/sparql-results+json");

        InputStream inputStream = connection.getInputStream();
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        //System.out.println("RESPUESTA CRUDA DE ONTOP:\n" + response); // LOG

        Map<String, Object> resultado = new HashMap<>();

        // Parsear la respuesta SPARQL JSON (string) a objeto JSON
        JsonNode json = objectMapper.readTree(response.toString());
        List<Map<String, String>> listaResultados = new ArrayList<>();

        JsonNode bindings = json.path("results").path("bindings");

        for (int i = 0; i < bindings.size(); i++) {
            JsonNode binding = bindings.get(i);
            Map<String, String> fila = new HashMap<>();

            Iterator<String> campos = binding.fieldNames();
            while (campos.hasNext()) {
                String campo = campos.next();
                JsonNode valorNode = binding.get(campo).get("value");
                if (valorNode != null) {
                    fila.put(campo, valorNode.asText());
                }
            }
            listaResultados.add(fila);
        }

        resultado.put("resultado", listaResultados);
        return resultado;
    }

}