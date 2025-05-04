package com.proyecto.ayuda.controller;

import com.proyecto.ayuda.service.ExcelService;
import com.proyecto.ayuda.service.OpenAiService;
import com.proyecto.ayuda.service.SparqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ayuda")
public class AyudaController {

    private final OpenAiService openAiService;
    private final SparqlService sparqlService;
    private final ExcelService excelService;
    @Autowired

    public AyudaController(OpenAiService openAiService, SparqlService sparqlService, ExcelService excelService) {
        this.openAiService = openAiService;
        this.sparqlService = sparqlService;
        this.excelService = excelService;
    }

    /**
     * Endpoint principal para procesar consultas en lenguaje natural.
     * Genera la consulta SPARQL con ayuda de OpenAI y la ejecuta sobre Ontop (base de datos relacional).
     * @param pregunta Pregunta en lenguaje natural formulada por el usuario.
     * @return Respuesta estructurada con la consulta SPARQL generada y los resultados obtenidos desde Oracle.
     */
    @GetMapping("/consulta")
    public Map<String, Object> obtenerConsulta(@RequestParam String pregunta) {
        Map<String, Object> respuesta = new HashMap<>();

        try {
            System.out.println("Pregunta recibida del usuario: " + pregunta);

            // 1. OpenAI genera la consulta SPARQL
            String consultaSPARQL = openAiService.generarConsultaSPARQL(pregunta);
            String consultaLimpia = openAiService.limpiarConsulta(consultaSPARQL);

            System.out.println("Consulta SPARQL generada por OpenAI:\n" + consultaLimpia);

            // 2. Validación: comprueba si la respuesta realmente es una consulta SELECT
            if (!consultaLimpia.toUpperCase().contains("SELECT")) {
                System.err.println("Error: la respuesta de OpenAI no contiene una consulta SELECT válida.");
                respuesta.put("error", "OpenAI no generó una consulta SPARQL válida.");
                return respuesta;
            }

            // 3. Ejecuta la consulta SPARQL en Ontop
            System.out.println("Ejecutando consulta en Ontop (Oracle + Ontología)");
            Map<String, Object> resultado = sparqlService.ejecutarConsultaOntop(consultaLimpia);

            // 4. Devuelve respuesta estructurada
            respuesta.put("consultaSPARQL", consultaLimpia);
            respuesta.put("resultado", resultado.get("resultado"));

            System.out.println("Consulta ejecutada correctamente y datos devueltos.");
            return respuesta;

        } catch (IOException e) {
            System.err.println("Error durante el procesamiento de la consulta: " + e.getMessage());
            respuesta.put("error", "Error al procesar la consulta: " + e.getMessage());
            return respuesta;
        }
    }
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam String pregunta) {
        try {
            // 1. Generar consulta SPARQL con OpenAI
            String consultaSPARQL = openAiService.generarConsultaSPARQL(pregunta);
            //String consultaLimpia = openAiService.limpiarConsulta(consultaSPARQL);
            String consultaSinLimite = openAiService.eliminarLimit(consultaSPARQL);

            if (!consultaSinLimite.toUpperCase().contains("SELECT")) {
                return ResponseEntity.badRequest().body("Consulta inválida".getBytes());
            }

            // 2. Ejecutar en Ontop (ya que todo va a Ontop ahora)
            Map<String, Object> resultado = sparqlService.ejecutarConsultaOntop(consultaSinLimite);
            List<Map<String, String>> datos = (List<Map<String, String>>) resultado.get("resultado");

            // 3. Generar Excel
            byte[] excelBytes = excelService.generarExcelDesdeResultados(datos);

            // 4. Preparar respuesta con cabeceras
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "resultado_sparql.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok().headers(headers).body(excelBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error generando Excel: " + e.getMessage()).getBytes());
        }
    }

}
