import React, { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { MessageCircle, X } from "lucide-react";
import Draggable from "react-draggable";
import axios from "axios";

const consultarAyuda = async (pregunta) => {
  try {
    const response = await axios.get(
      `http://localhost:8081/api/ayuda/consulta?pregunta=${encodeURIComponent(pregunta)}`
    );
    const datos = response.data.resultado;

    console.log("Datos recibidos:", datos);

    if (Array.isArray(datos) && datos.length > 0) {
      return datos;
    } else {
      return "No se encontró información relevante.";
    }
  } catch (error) {
    console.error("ERROR:", error.response ? error.response.data : error.message);
    return "Error al consultar la información.";
  }
};

const BotonAyuda = () => {
  const [mensaje, setMensaje] = useState("");
  const [mensajes, setMensajes] = useState(() => {
    const mensajesGuardados = localStorage.getItem("chatMensajes");
    return mensajesGuardados ? JSON.parse(mensajesGuardados) : [];
  });

  const [escribiendo, setEscribiendo] = useState(false);
  const [mostrarChat, setMostrarChat] = useState(false);

  const dragRef = useRef(null);
  const textAreaRef = useRef(null);

  useEffect(() => {
    if (textAreaRef.current) {
      textAreaRef.current.style.height = "auto";
      textAreaRef.current.style.height = `${textAreaRef.current.scrollHeight}px`;
    }
  }, [mensaje]);

  const [mostrarBotonDescarga, setMostrarBotonDescarga] = useState(false);
  const [ultimaPregunta, setUltimaPregunta] = useState("");
  const [tiempoRespuesta, setTiempoRespuesta] = useState(null);


  const enviarMensaje = async () => {
    if (mensaje.trim() === "") return;

    const nuevoMensaje = { texto: mensaje, tipo: "usuario" };
    setMensajes((prevMensajes) => [...prevMensajes, nuevoMensaje]);
    setUltimaPregunta(mensaje);
    setMensaje("");
    setEscribiendo(true);

    const start = performance.now();

    try {
      const respuestaTexto = await consultarAyuda(mensaje);
      console.log("Respuesta obtenida en texto:", respuestaTexto);

      const end = performance.now(); //  Mide el tiempo al recibir respuesta
      const duracion = ((end - start) / 1000).toFixed(2); // En segundos
      console.log(`Tiempo de respuesta: ${duracion}s`);
      setTiempoRespuesta(duracion); // Guarda duración en el estado


      setMensajes((prevMensajes) => [
        ...prevMensajes,
        { texto: respuestaTexto, tipo: "ia" },
      ]);

      setMostrarBotonDescarga(Array.isArray(respuestaTexto));
    } catch (error) {
      console.error("Error al obtener respuesta:", error);
      setMensajes((prevMensajes) => [
        ...prevMensajes,
        { texto: "Hubo un error al obtener la respuesta.", tipo: "ia" },
      ]);
    } finally {
      setEscribiendo(false);
    }
  };

  const descargarExcel = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8081/api/ayuda/exportar?pregunta=${encodeURIComponent(ultimaPregunta)}`,
        { responseType: "blob" }
      );

      const blob = new Blob([response.data], { type: "application/vnd.ms-excel" });
      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = "resultado_sparql.xlsx";
      link.click();

      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Error al descargar el Excel:", error);
    }
  };

  return (
    <Draggable nodeRef={dragRef} cancel=".no-drag">
      <div ref={dragRef} className="fixed bottom-6 right-6 flex flex-col items-end">
        <motion.button
          onDoubleClick={() => setMostrarChat(!mostrarChat)}
          className="bg-blue-600 text-white p-4 rounded-full shadow-lg hover:bg-blue-700 transition-all duration-300 flex items-center justify-center active:cursor-grabbing outline-none border-none"
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
        >
          <MessageCircle size={30} />
        </motion.button>

        <AnimatePresence>
          {mostrarChat && (
            <motion.div
              initial={{ opacity: 0, y: 20, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 20, scale: 0.95 }}
              transition={{ duration: 0.3 }}
              className="bg-white p-6 shadow-2xl rounded-xl w-[500px] h-auto border border-gray-300 mt-2 flex flex-col no-drag"
            >
              <div className="flex justify-between items-center border-b pb-2 mb-2">
                <h2 className="text-lg font-bold text-gray-800">Asistente de Ayuda</h2>
                <button
                  onClick={() => setMostrarChat(false)}
                  className="text-gray-500 hover:text-gray-800"
                >
                  <X size={20} />
                </button>
              </div>

              <div className="h-64 overflow-y-auto space-y-2 p-2 bg-gray-50 rounded">
                {tiempoRespuesta && (
                  <p className="text-sm text-gray-600 mt-1 italic">
                    ⏱ Tiempo de respuesta: {tiempoRespuesta} segundos
                  </p>
                )}

                {mensajes.map((msg, index) => (
                  <motion.div
                    key={index}
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.3 }}
                    className={`p-2 rounded-lg text-sm max-w-xs ${
                      msg.tipo === "usuario"
                        ? "bg-blue-600 text-white self-end"
                        : "bg-gray-300 text-gray-800 self-start shadow-md"
                    }`}
                  >
                    {Array.isArray(msg.texto) && msg.texto.length > 0 ? (
                      <div className="overflow-x-auto mt-1">
                          <p className="text-gray-600 text-xs italic mb-1">
                              Mostrando los primeros 20 resultados.
                          </p>
                        <table className="table-auto border-collapse border border-gray-400 text-sm">
                          <thead>
                            <tr>
                              {Object.keys(msg.texto[0]).map((col, idx) => (
                                <th key={idx} className="border px-2 py-1 font-bold bg-gray-200">{col}</th>
                              ))}
                            </tr>
                          </thead>
                          <tbody>
                            {msg.texto.map((fila, idx) => (
                              <tr key={idx}>
                                {Object.values(fila).map((valor, i) => (
                                  <td key={i} className="border px-2 py-1">{valor || "—"}</td>
                                ))}
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div>{msg.texto}</div>
                    )}
                  </motion.div>
                ))}
              </div>

              <div className="mt-3">
                <motion.textarea
                  ref={textAreaRef}
                  placeholder="Escribe tu pregunta..."
                  className="w-full p-4 border rounded-xl focus:ring-2 focus:ring-blue-400 text-lg resize-none bg-gray-100 transition-all min-h-[50px] max-h-[200px] no-drag"
                  value={mensaje}
                  onChange={(e) => setMensaje(e.target.value)}
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.2 }}
                />
              </div>

              {mostrarBotonDescarga && (
                <motion.button
                  className="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600 transition mt-2"
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={descargarExcel}
                >
                  Descargar Excel
                </motion.button>
              )}

              <div className="flex justify-end space-x-2 mt-3">
                <motion.button
                  className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600 transition"
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={() => {
                    setMensajes([]);
                    setMensaje("");
                    localStorage.removeItem("chatMensajes");
                  }}
                >
                  Borrar Chat
                </motion.button>

                <motion.button
                  className="bg-blue-500 text-white px-3 py-1 rounded text-sm hover:bg-blue-600 transition"
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                  onClick={enviarMensaje}
                >
                  Enviar
                </motion.button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </Draggable>
  );
};

export default BotonAyuda;
