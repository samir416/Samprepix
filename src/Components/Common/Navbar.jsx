import { Link } from "react-router-dom";
import { Moon } from "lucide-react";
import logo from "../../assets/Logo.png";
import "../../styles/home.css";
import { useNavigate } from "react-router-dom";

function Navbar() {

    const navigate = useNavigate();

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

                    <div className="theme-icon">
                        <Moon size={18} />
                    </div>

                    <Link className="signin-btn" to="/login">
                        Sign in
                    </Link>

                    <Link className="getstarted-btn" to="/auth">
                        Get started
                    </Link>
                </div>

            </nav>

        </div>

    )
}

export default Navbar;