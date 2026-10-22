
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";

// Load the global CSS reset before rendering the application.
import "./index.css";

// Mount the React application inside index.html's root element.
createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>
);
