import { createRoot } from "react-dom/client";
import "bootstrap/dist/css/bootstrap.min.css";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import "./styles/theme.css";
import "./styles/global.css";
import "react-toastify/dist/ReactToastify.css";
import "./styles/toastify.css";
import { ToastContainer, toast, Slide } from "react-toastify";

if (typeof window !== "undefined") {
  window.__toast = toast;
}

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <App />

    <ToastContainer
      position="bottom-center"
      autoClose={5000}
      hideProgressBar={false}
      newestOnTop={false}
      closeOnClick={false}
      pauseOnHover={true}
      pauseOnFocusLoss={false}
      draggable={false}
      transition={Slide}
      closeButton={false}
    />

  </BrowserRouter>
);