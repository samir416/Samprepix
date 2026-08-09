import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import "bootstrap/dist/css/bootstrap.min.css";
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import "./styles/theme.css";
import "./styles/global.css";
import "./styles/toastify.css";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <App />
    <ToastContainer
    position="bottom-center"
    autoClose={7000}
    hideProgressBar={false}
    newestOnTop
    closeOnClick
    pauseOnHover
    draggable
/>
    
  </BrowserRouter>
)
