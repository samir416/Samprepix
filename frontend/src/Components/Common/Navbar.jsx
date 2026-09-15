import { Link, useLocation } from "react-router-dom";
import { motion } from "framer-motion";

import {
    Moon,
    Sun
} from "lucide-react";

import logo from "../../assets/Logo.png";

import "../../styles/home.css";

import {
    useEffect,
    useState
} from "react";

function Navbar() {

    const location = useLocation();

    const [darkMode, setDarkMode] = useState(false);

    const isLogin = location.pathname === "/login";
    const isSignup = location.pathname === "/auth" || location.pathname === "/register";

    /* LOAD SAVED THEME */

    useEffect(() => {

        const savedTheme =
            localStorage.getItem("theme");

        if (savedTheme === "dark") {

            document.body.classList.add(
                "dark-theme"
            );

            setDarkMode(true);
        }

    }, []);

    /* TOGGLE */

    const toggleTheme = () => {

        document.body.classList.toggle(
            "dark-theme"
        );

        const isDark =
            document.body.classList.contains(
                "dark-theme"
            );

        setDarkMode(isDark);

        localStorage.setItem(
            "theme",
            isDark ? "dark" : "light"
        );
    };

    return (

        <div className="navbar-wrapper">

            <nav className="custom-navbar">

                {/* LEFT */}

                <div className="logo-section">

                    <div className="logo-box">

                        <img
                            src={logo}
                            alt="Samprepix Logo"
                            className="navbar-logo"
                        />

                    </div>

                    <h2 className="logo-text">
                        Samprepix
                    </h2>

                </div>

                {/* CENTER */}

                <div className="nav-links">

                    <Link
                        to="/"
                        className={
                            location.pathname === "/"
                                ? "active-nav"
                                : ""
                        }
                    >

                        Home

                    </Link>

                    <Link
                        to="/features"
                        className={
                            location.pathname === "/features"
                                ? "active-nav"
                                : ""
                        }
                    >

                        Features

                    </Link>

                    <Link
                        to="/pricing"
                        className={
                            location.pathname === "/pricing"
                                ? "active-nav"
                                : ""
                        }
                    >

                        Pricing

                    </Link>

                </div>

                {/* RIGHT */}

                <div className="nav-right">

                    {/* SIMPLE PREMIUM TOGGLE */}

                    <button
                        className="theme-toggle"
                        onClick={toggleTheme}
                        aria-label="Toggle Theme"
                    >

                        {
                            darkMode
                                ? <Sun size={22} />
                                : <Moon size={22} />
                        }

                    </button>

                    <Link
                        className={`nav-auth-btn ${isLogin ? "active-auth-btn" : ""}`}
                        to="/login"
                    >
                        {isLogin && (
                            <motion.div
                                layoutId="nav-auth-bg"
                                style={{
                                    position: "absolute",
                                    top: 0, left: 0, right: 0, bottom: 0,
                                    background: "linear-gradient(90deg, #6366f1, #06b6d4)",
                                    borderRadius: "14px",
                                    zIndex: 0
                                }}
                                transition={{ type: "spring", stiffness: 400, damping: 30 }}
                            />
                        )}
                        <span style={{ position: "relative", zIndex: 1 }}>Sign in</span>
                    </Link>

                    <Link
                        className={`nav-auth-btn ${isSignup ? "active-auth-btn" : ""}`}
                        to="/auth"
                    >
                        {isSignup && (
                            <motion.div
                                layoutId="nav-auth-bg"
                                style={{
                                    position: "absolute",
                                    top: 0, left: 0, right: 0, bottom: 0,
                                    background: "linear-gradient(90deg, #6366f1, #06b6d4)",
                                    borderRadius: "14px",
                                    zIndex: 0
                                }}
                                transition={{ type: "spring", stiffness: 400, damping: 30 }}
                            />
                        )}
                        <span style={{ position: "relative", zIndex: 1 }}>Get started</span>
                    </Link>

                </div>

            </nav>

        </div>
    )
}

export default Navbar;