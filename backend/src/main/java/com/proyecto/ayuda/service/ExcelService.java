package com.proyecto.ayuda.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    /**
     * Genera un archivo Excel a partir de los resultados de una consulta SPARQL.
     */
    public byte[] generarExcelDesdeResultados(List<Map<String, String>> resultados) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Resultados");

        if (resultados == null || resultados.isEmpty()) {
            workbook.close();
            throw new IllegalArgumentException("No hay resultados para exportar.");
        }

        // Cabecera
        Row header = sheet.createRow(0);
        List<String> columnas = new ArrayList<>(resultados.get(0).keySet());

        for (int i = 0; i < columnas.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columnas.get(i));
        }

        // Filas de datos
        for (int i = 0; i < resultados.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Map<String, String> fila = resultados.get(i);
            for (int j = 0; j < columnas.size(); j++) {
                row.createCell(j).setCellValue(fila.getOrDefault(columnas.get(j), ""));
            }
        }

        // Guardar en byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}

