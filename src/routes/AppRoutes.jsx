import { Routes, Route } from "react-router-dom";

import Home from "../Pages/Home";
import Login from "../Pages/Login";
import Register from "../Pages/Register";
import Dashboard from "../Pages/Dashboard";
import AuthModal from "../Components/Auth/AuthModal";
import Performance from "../Pages/Performance";

export default function AppRoutes() {

    return (

        <Routes>

            {/* HOME */}

            <Route
                path="/"
                element={<Home />}
            />

            {/* LOGIN */}

            <Route
                path="/login"
                element={<Login />}
            />

            {/* REGISTER */}

            <Route
                path="/register"
                element={<Register />}
            />

            {/* DASHBOARD */}

            <Route
                path="/dashboard"
                element={<Dashboard />}
            />

            {/* RESUME ANALYZER */}

            <Route
                path="/resume-analyzer"
                element={<Dashboard />}
            />

            {/* MOCK INTERVIEW */}

            <Route
                path="/mock-interview"
                element={<Dashboard />}
            />

            {/* PERFORMANCE */}

            <Route
                path="/performance"
                element={<Dashboard />}
            />

            {/* CODING ARENA */}

            <Route
                path="/coding-arena"
                element={<Dashboard />}
            />

            {/* AUTH */}

            <Route
                path="/auth"
                element={<AuthModal />}
            />

        </Routes>
    );
}