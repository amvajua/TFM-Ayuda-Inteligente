import React from "react";
import BotonAyuda from "./components/BotonAyuda";
import ErrorBoundary from "./components/ErrorBoundary";

function App() {
  return (
    <ErrorBoundary>
      <div className="h-screen flex items-center justify-center bg-gray-100">
        <BotonAyuda />
      </div>
    </ErrorBoundary>
  );
}

export default App;
