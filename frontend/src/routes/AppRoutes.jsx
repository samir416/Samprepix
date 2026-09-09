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
import Changelog from "../Pages/Changelog";
import Roadmap from "../Pages/Roadmap";
import Docs from "../Pages/Docs";
import Blog from "../Pages/Blog";
import Guides from "../Pages/Guides";
import Community from "../Pages/Community";
import About from "../Pages/About";
import Security from "../Pages/Security";
import Accessibility from "../Pages/Accessibility";
import Contact from "../Pages/Contact";
import Legal from "../Pages/Legal";
import NotFound from "../Pages/NotFound";
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
            {/* PUBLIC MARKETING & CONTENT ROUTES */}
            <Route path="/" element={<Home />} />
            <Route path="/features" element={<Features />} />
            <Route path="/pricing" element={<Pricing />} />
            <Route path="/changelog" element={<Changelog />} />
            <Route path="/roadmap" element={<Roadmap />} />
            <Route path="/docs" element={<Docs />} />
            <Route path="/blog" element={<Blog />} />
            <Route path="/guides" element={<Guides />} />
            <Route path="/community" element={<Community />} />
            <Route path="/about" element={<About />} />
            <Route path="/security" element={<Security />} />
            <Route path="/accessibility" element={<Accessibility />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/legal" element={<Legal />} />
            <Route path="/terms" element={<Legal />} />
            <Route path="/privacy" element={<Legal />} />

            {/* AUTHENTICATION ROUTES */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/auth" element={<AuthModal />} />

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
                <Route path="/analytics" element={<Performance />} />
                <Route path="/billing" element={<Pricing />} />
                <Route path="/profile" element={<Profile />} />
            </Route>

            {/* 404 NOT FOUND CATCH-ALL */}
            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}
