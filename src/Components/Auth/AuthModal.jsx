import Navbar from "../Common/Navbar";
import "../../styles/authmodal.css";
import Logo from "../../assets/Logo.png";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";

export default function AuthModal() {

    const navigate = useNavigate();

    return (

        <>

            <Navbar />

            <section className="auth-page">

                <div className="auth-modal">

                    {/* LOGO */}

                    <div className="auth-logo">

                        <img
                            src={Logo}
                            alt="logo"
                        />

                    </div>

                    {/* TITLE */}

                    <h2>
                        Create your account
                    </h2>

                    <p className="auth-subtitle">
                        Start your free 14-day pro trial
                    </p>

                    {/* SOCIAL BUTTONS */}

                    <div className="auth-socials">

                        <button>

                            <span>
                                ⌘
                            </span>

                            GitHub

                        </button>

                        <button>

                            <span>
                                ✉
                            </span>

                            Google

                        </button>

                    </div>

                    {/* DIVIDER */}

                    <div className="auth-divider">

                        <span></span>

                        <p>
                            or continue with email
                        </p>

                        <span></span>

                    </div>

                    {/* FORM */}

                    <form
                        className="auth-form"
                        onSubmit={(e) => {

                            e.preventDefault();

                            navigate("/dashboard");
                        }}
                    >
                        <div className="auth-input-group">

                            <label>
                                Full name
                            </label>

                            <input
                                type="text"
                                placeholder="Aarav Mehta"
                            />

                        </div>

                        <div className="auth-input-group">

                            <label>
                                Email
                            </label>

                            <input
                                type="email"
                                placeholder="you@university.edu"
                            />

                        </div>

                        <div className="auth-input-group">

                            <label>
                                Password
                            </label>

                            <input
                                type="password"
                                placeholder="••••••••"
                            />

                        </div>

                        <button
                            type="submit"
                            className="auth-submit-btn"
                        >

                            Create account

                        </button>

                    </form>

                    {/* FOOTER */}

                    <p className="auth-bottom-text">

                        Already have an account?

                        <Link to="/login" className="auth-login-link">
                            Sign in
                        </Link>

                    </p>

                    <p className="auth-security">

                        Secured with JWT • 256-bit encryption

                    </p>

                </div>

            </section>

        </>
    );
}