import React from "react";
import Logo from "../../assets/Logo.png";

import {
    FiGrid,
    FiFileText,
    FiMic,
    FiCode,
    FiBookOpen,
    FiBarChart2
} from "react-icons/fi";

import {
    Link,
    useLocation,
    useNavigate
} from "react-router-dom";

export default function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();

    return (
        <aside className="dashboard-sidebar">
            <div className="sidebar-top">
                {/* LOGO */}
                <div className="dashboard-logo">
                    <img
                        src={Logo}
                        alt="Logo"
                    />
                    <h2>
                        Samprepix
                    </h2>
                </div>

                {/* MENU */}
                <nav className="dashboard-menu">
                    {/* DASHBOARD */}
                    <Link
                        to="/dashboard"
                        className="sidebar-link"
                    >
                        <button
                            className={
                                location.pathname === "/dashboard"
                                    ? "active"
                                    : ""
                            }
                        >
                            <FiGrid />
                            Dashboard
                        </button>
                    </Link>

                    {/* RESUME ANALYZER */}
                    <Link
                        to="/resume-analyzer"
                        className="sidebar-link"
                    >
                        <button
                            className={
                                location.pathname === "/resume-analyzer"
                                    ? "active"
                                    : ""
                            }
                        >
                            <FiFileText />
                            Resume Analyzer
                        </button>
                    </Link>

                    {/* MOCK INTERVIEW */}
                    <Link
                        to="/mock-interview"
                        className="sidebar-link"
                    >
                        <button
                            className={
                                location.pathname === "/mock-interview"
                                    ? "active"
                                    : ""
                            }
                        >
                            <FiMic />
                            Mock Interview
                        </button>
                    </Link>

                    {/* CODING ARENA */}
                    <Link
                        to="/coding-arena"
                        className="sidebar-link"
                    >
                        <button
                            className={
                                location.pathname === "/coding-arena"
                                    ? "active"
                                    : ""
                            }
                        >
                            <FiCode />
                            Coding Arena
                        </button>
                    </Link>

                    {/* APTITUDE */}
                    <Link
                        to="/aptitude"
                        className="sidebar-link"
                    >
                        <button
                            className={
                                location.pathname === "/aptitude"
                                    ? "active"
                                    : ""
                            }
                        >
                            <FiBookOpen />
                            Aptitude
                        </button>
                    </Link>

                    {/* PERFORMANCE */}
                    <Link
                        to="/performance"
                        className="sidebar-link"
                    >
                        <button
                            className={
                                location.pathname === "/performance"
                                    ? "active"
                                    : ""
                            }
                        >
                            <FiBarChart2 />
                            Performance
                        </button>
                    </Link>
                </nav>
            </div>

            {/* UPGRADE CARD */}
            <div className="upgrade-card">
                <h3>
                    Upgrade to Pro
                </h3>
                <p>
                    Unlimited mocks, all tracks.
                </p>
                <button
                    className="sidebar-upgrade-btn"
                    onClick={() => navigate("/pricing")}
                >
                    Upgrade
                </button>
            </div>
        </aside>
    );
}
