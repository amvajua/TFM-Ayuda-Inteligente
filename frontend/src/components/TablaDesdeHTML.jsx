import React from "react";

const TablaDesdeHTML = ({ html }) => {
  const parser = new DOMParser();
  const doc = parser.parseFromString(html, "text/html");
  const table = doc.querySelector("table");

  if (!table) return <p>Error al procesar la tabla</p>;

  const headers = Array.from(table.querySelectorAll("thead th")).map((th, i) => (
    <th key={i} className="border px-4 py-2 bg-gray-100 text-sm">{th.textContent}</th>
  ));

  const rows = Array.from(table.querySelectorAll("tbody tr")).map((tr, rowIndex) => {
    const cells = Array.from(tr.querySelectorAll("td")).map((td, cellIndex) => (
      <td key={cellIndex} className="border px-4 py-2 text-sm">{td.textContent}</td>
    ));
    return <tr key={rowIndex}>{cells}</tr>;
  });

  return (
    <div className="overflow-x-auto mt-1">
      <table className="table-auto border-collapse border border-gray-400 w-full text-left text-sm">
        <thead>
          <tr>{headers}</tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    </div>
  );
};

export default TablaDesdeHTML;
