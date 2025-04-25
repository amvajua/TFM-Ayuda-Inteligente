# TFM - Ayuda Inteligente para Sistemas de Atención al Ciudadano
# 1. Descripción del proyecto
Proyecto desarrollado como Trabajo Fin de Máster en Inteligencia Artificial. 
El objetivo es mejorar los sistemas de atención al ciudadano mediante técnicas de IA, Web Semántica y consultas en lenguaje natural.

## 1.1 Objetivos principales
  - Implementar un sistema de **ayuda inteligente contextual** basado en una ontología del dominio.
  - Desarrollar un módulo para realizar **consultas personalizadas en lenguaje natural** con generación de listados desde Oracle.
  - Integrar una solución semántica para la **detección automática de errores técnicos** en la aplicación web.

## 1.2 Tecnologías utilizadas
- **Java + Spring Boot** (Backend)
- **React + TypeScript** (Frontend)
- **Ontologías OWL + SPARQL** (Web Semántica)
- **Ontop + Oracle DB** (Mapeo OBDA)
- **OpenAI API** (Procesamiento del lenguaje natural)
- **Docker** (Entorno de despliegue)
- **Elasticsearch (opcional)** para la parte de logs y errores

## 1.3 Estructura del Proyecto
```
TFM-Ayuda-Inteligente/
├── backend/     # Backend en Spring Boot + Ontop + conexión a Oracle
├── frontend/    # Frontend en React + Tailwind
└── README.md    # Este archivo
```
## 1.4 Ejecución general
1. Clona el repositorio:
```bash
git clone https://github.com/amvajua/TFM-Ayuda-Inteligente.git
cd TFM-Ayuda-Inteligente
```
2. Inicia el backend:

```bash
./mvnw spring-boot:run
```
3. Lanza el frontend (desde la carpeta `/frontend`):

```bash
npm install
npm start
```
> Asegúrate de tener configurada la base de datos Oracle y el endpoint de Ontop accesible.

## 1.5 Estado del proyecto
- [x] Ontología del dominio implementada
- [x] Backend conectado a Oracle mediante Ontop
- [x] Botón de ayuda contextual operativo
- [x] Consultas en lenguaje natural con OpenAI y SPARQL
- [ ] Detección semántica de errores en fase de integración
- [ ] Validación completa con métricas (en progreso)

--------------------------------------------------------------------------------------------
## 1.6 Backend (`/backend`)
- **Tecnología:** Java 8 + Spring Boot
- **Ontología:** OWL + OBDA con Ontop
- **Base de datos:** Oracle (consultas SPARQL mapeadas a SQL)
- **Servicios expuestos:**
  - `POST /api/ayuda/consulta` → Consultas personalizadas en lenguaje natural
  - `GET /api/ayuda/exportar` → Exportación de resultados a Excel
  
### 1.6.1 Estructura básica
```
backend/
├── src/
│   ├── main/java/       # Código fuente Java
│   ├── main/resources/  # Configuración y recursos
├── pom.xml              # Configuración Maven

```
### 1.6.2 Configuración
El archivo `application.properties` gestiona:

- Conexión con Oracle
- Ruta del archivo OWL
- Endpoint de Ontop
- Configuración de CORS

### 1.6.3 Ejecución
1. Entrar en la carpeta `backend`
2. Ejecutar la aplicación Spring Boot (`Main.java`)
3. Acceso por defecto en `http://localhost:8081`

-----------------------------------------------------------------------------------------------------
## 1.7 Frontend (`/frontend`)
- **Tecnología:** React + Vite + TailwindCSS
- **Funcionalidad:**
  - Integración con backend vía REST
  - Modal de ayuda inteligente contextual
  - Formulario de consultas en lenguaje natural
  - Exportación de resultados
 
> Asegúrate de que Ontop y Oracle están activos.
 
### 1.7.1 Estructura básica 
```
frontend/
├── src/              → Código fuente (componentes, páginas, etc.)
├── public/           → Recursos estáticos
├── vite.config.js    → Configuración de Vite
├── package.json      → Dependencias y scripts
```

### 1.7.2 Configuración
Asegúrate de que el backend esté ejecutándose en `http://localhost:8081`.

### 1.7.3 Ejecución
```bash
cd frontend
npm install
npm run dev
```
> Acceso por defecto: `http://localhost:5173`
### 1.7.4 Requisitos
- React 18+
- Vite
- (Opcional) TailwindCSS
- Axios para llamadas al backend
- Comunicación con servicio semántico vía REST
-----------------------------------------------------------------------------
## 2. Autora
**Amparo VJ**  
[@amvajua](https://github.com/amvajua)  
100007769@alumnos.uimp.es

Trabajo Fin de Máster en Inteligencia Artificial  
Universidad de Madrid · 2025

------------------------------------------------------------------------
## 3. Licencia
Este proyecto es privado. Su uso está restringido al ámbito académico.
