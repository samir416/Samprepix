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

const ToastCloseBtn = ({ closeToast }) => (
  <button
    type="button"
    className="toast-close-btn"
    onClick={closeToast}
    aria-label="Close notification"
  >
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="18" y1="6" x2="6" y2="18"></line>
      <line x1="6" y1="6" x2="18" y2="18"></line>
    </svg>
  </button>
);

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
      closeButton={ToastCloseBtn}
    />

  </BrowserRouter>
);