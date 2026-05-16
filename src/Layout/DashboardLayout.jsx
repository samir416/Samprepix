import Sidebar from "../Components/Dashboard/Sidebar";
import Topbar from "../Components/Dashboard/Topbar";

import { Outlet } from "react-router-dom";

import "../styles/dashboard.css";

export default function DashboardLayout() {

    return (

        <section className="dashboard-layout">

            {/* SIDEBAR */}

            <Sidebar />

            {/* MAIN */}

            <div className="dashboard-main">

                {/* TOPBAR */}

                <Topbar />

                {/* PAGE CONTENT */}

                <div className="dashboard-content">

                    <Outlet />

                </div>

            </div>

        </section>
    );
}