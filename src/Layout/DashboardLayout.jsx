import { useState } from "react";

import Sidebar from "../Components/Dashboard/Sidebar";
import Topbar from "../Components/Dashboard/Topbar";

import { Outlet } from "react-router-dom";

import "../styles/dashboard.css";

export default function DashboardLayout() {

    const [sidebarOpen, setSidebarOpen] = useState(false);

    return (

        <section className="dashboard-layout">

            {/* OVERLAY */}

            <div
                className={`sidebar-overlay ${sidebarOpen ? "show-overlay" : ""}`}
                onClick={() => setSidebarOpen(false)}
            ></div>

            {/* SIDEBAR */}

            <div
                className={`sidebar-mobile-wrapper ${sidebarOpen ? "show-sidebar" : ""}`}
            >

                <Sidebar />

            </div>

            {/* MAIN */}

            <div className="dashboard-main">

                {/* TOPBAR */}

                <Topbar
                    sidebarOpen={sidebarOpen}
                    setSidebarOpen={setSidebarOpen}
                />

                {/* PAGE CONTENT */}

                <div className="dashboard-content">

                    <Outlet />

                </div>

            </div>

        </section>
    );
}