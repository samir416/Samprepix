import React from "react";
import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "../Components/Dashboard/Sidebar";
import Topbar from "../Components/Dashboard/Topbar";
import ErrorBoundary from "../Components/Common/ErrorBoundary";
import AIHelpBot from "../Components/Support/AIHelpBot";
import "../styles/dashboard.css";

export default function AppLayout() {
    const location = useLocation();
    const isCodingArena = location.pathname.startsWith("/coding-arena");

    return (
        <div className="dashboard-layout">
            {/* GLOBAL PERSISTENT SIDEBAR */}
            <Sidebar />

            {/* MAIN APPLICATION CONTAINER */}
            <div className="dashboard-main">
                {/* GLOBAL PERSISTENT TOPBAR */}
                <Topbar />

                {/* MAIN CONTENT AREA */}
                <main className={`dashboard-content ${isCodingArena ? "coding-arena-shell" : ""}`}>
                    <ErrorBoundary>
                        <div key={location.pathname} className="app-page-transition">
                            <Outlet />
                        </div>
                    </ErrorBoundary>
                </main>
            </div>

            {/* AUTHENTICATED WORKSPACE AI HELP BOT */}
            <AIHelpBot />
        </div>
    );
}
