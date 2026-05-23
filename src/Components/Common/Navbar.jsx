import { Link } from "react-router-dom";

import {
    Moon,
    Sun
} from "lucide-react";

import logo from "../../assets/Logo.png";

import "../../styles/home.css";

import {
    useLocation
} from "react-router-dom";

import {
    useEffect,
    useState
} from "react";

function Navbar() {

    const location = useLocation();

    const [darkMode, setDarkMode] = useState(false);

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
                        className="signin-btn"
                        to="/login"
                    >

                        Sign in

                    </Link>

                    <Link
                        className="getstarted-btn"
                        to="/auth"
                    >

                        Get started

                    </Link>

                </div>

            </nav>

        </div>
    )
}

export default Navbar;