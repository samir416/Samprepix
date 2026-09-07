import { Routes, Route } from "react-router-dom";

import Home from "../Pages/Home";
import Login from "../Pages/Login";
import Register from "../Pages/Register";
import Dashboard from "../Pages/Dashboard";
import ResumeAnalyzer from "../Pages/ResumeAnalyzer";
import MockInterview from "../Pages/MockInterview";
import Performance from "../Pages/Performance";
import InterviewResult from "../Pages/InterviewResult";
import CodingArena from "../Pages/CodingArena";
import Profile from "../Pages/Profile";
import Features from "../Pages/Features";
import Pricing from "../Pages/Pricing";
import AuthModal from "../Components/Auth/AuthModal";
import ProtectedRoute from "../Components/Auth/ProtectedRoute";
import ResetPasswordPage from "../pages/auth/ResetPasswordPage";
import ForgotPasswordPage from "../pages/auth/ForgotPasswordPage";
import Onboarding from "../Pages/Onboarding";
import Aptitude from "../Pages/Aptitude";
import AppLayout from "../Layout/AppLayout";


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

            {/* FORGOT PASSWORD */}
            <Route
                path="/forgot-password"
                element={<ForgotPasswordPage />}
            />
            {/* RESET PASSWORD */}
            <Route
                path="/reset-password"
                element={<ResetPasswordPage />}
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

            <Route
                path="/onboarding"
                element={
                    <ProtectedRoute>
                        <Onboarding />
                    </ProtectedRoute>
                }
            />

            {/* AUTHENTICATED GLOBAL APP SHELL (SIDEBAR + TOPBAR + CONTENT) */}
            <Route
                element={
                    <ProtectedRoute>
                        <AppLayout />
                    </ProtectedRoute>
                }
            >
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/resume-analyzer" element={<ResumeAnalyzer />} />
                <Route path="/mock-interview" element={<MockInterview />} />
                <Route path="/interview-result" element={<InterviewResult />} />
                <Route path="/coding-arena" element={<CodingArena />} />
                <Route path="/aptitude" element={<Aptitude />} />
                <Route path="/performance" element={<Performance />} />
                <Route path="/profile" element={<Profile />} />
            </Route>
        </Routes>
    );
}
