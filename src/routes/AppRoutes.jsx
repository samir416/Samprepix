import { Routes, Route } from "react-router-dom";

import Home from "../Pages/Home";
import Login from "../Pages/Login";
import Register from "../Pages/Register";
import Dashboard from "../Pages/Dashboard";
import ResumeAnalyzer from "../Pages/ResumeAnalyzer";
import MockInterview from "../Pages/MockInterview";
import Performance from "../Pages/Performance";
import CodingArena from "../Pages/CodingArena";
import Features from "../Pages/Features";
import Pricing from "../Pages/Pricing";

import AuthModal from "../Components/Auth/AuthModal";

export default function AppRoutes() {

    return (

        <Routes>

            {/* HOME */}

            <Route
                path="/"
                element={<Home />}
            />

            {/* FEATURES */}

            <Route
                path="/features"
                element={<Features />}
            />

            {/* PRICING */}

            <Route
                path="/pricing"
                element={<Pricing />}
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

            {/* AUTH */}

            <Route
                path="/auth"
                element={<AuthModal />}
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
        </Routes>
    );
}