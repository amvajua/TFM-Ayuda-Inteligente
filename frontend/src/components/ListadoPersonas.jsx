import React, { useEffect, useState } from "react";
import axios from "axios";

const ListadoPersonas = () => {
  const [personas, setPersonas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const obtenerPersonas = async () => {
      try {
        const response = await axios.get("http://localhost:8081/api/personas/listar");
        const raw = response.data.resultado;

        // Parsea el JSON SPARQL dentro del campo "resultado"
        const bindings = JSON.parse(raw).results.bindings;

        // Extrae nombre y género
        const datos = bindings.map((item) => ({
          nombre: item.nombre?.value || "Sin nombre",
          genero: item.genero?.value || "Sin género",
        }));

        setPersonas(datos);
        setCargando(false);
      } catch (err) {
        console.error("Error al obtener personas:", err);
        setError("No se pudo obtener el listado de personas.");
        setCargando(false);
      }
    };

    obtenerPersonas();
  }, []);

  if (cargando) return <p>Cargando personas...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Listado de Personas</h2>
      <table className="table-auto border-collapse border border-gray-400">
        <thead>
          <tr>
            <th className="border px-4 py-2">Nombre</th>
            <th className="border px-4 py-2">Género</th>
          </tr>
        </thead>
        <tbody>
          {personas.map((p, index) => (
            <tr key={index}>
              <td className="border px-4 py-2">{p.nombre}</td>
              <td className="border px-4 py-2">{p.genero}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ListadoPersonas;
