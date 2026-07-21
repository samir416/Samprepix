import Navbar from "../Common/Navbar";
import "../../styles/authmodal.css";
import Logo from "../../assets/Logo.png";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { registerUser } from "../../services/authService";

export default function AuthModal() {

    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

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
                        Start your placement journey
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
                        onSubmit={async (e) => {

                            e.preventDefault();

                            try {

                                await registerUser(
                                    username,
                                    email,
                                    password
                                );
                                setUsername("");
                                setEmail("");
                                setPassword("");
                                navigate("/login");

                            } catch (err) {

                                console.log(err);

                                setError(
                                    "Please review your details and try again."
                                );
                            }
                        }}
                    >
                        <div className="auth-input-group">

                            <label>
                                Username
                            </label>

                            <input
                                type="text"
                                placeholder="samir416"
                                value={username}
                                onChange={(e) =>
                                    setUsername(e.target.value)
                                }
                            />
                        </div>

                        <div className="auth-input-group">

                            <label>
                                Email
                            </label>

                            <input
                                type="email"
                                placeholder="you@university.edu"
                                value={email}
                                onChange={(e) =>
                                    setEmail(e.target.value)
                                }
                            />

                        </div>

                        <div className="auth-input-group">

                            <label>
                                Password
                            </label>

                            <input
                                type="password"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) =>
                                    setPassword(e.target.value)
                                }
                            />

                            

                        </div>
                        {
                            error && (
                                <div className="auth-error-alert">

                                    <div className="auth-error-icon">
                                        !
                                    </div>

                                    <div className="auth-error-content">

                                        <h6>
                                            Unable to Create Account
                                        </h6>

                                        <p>
                                            {error}
                                        </p>

                                    </div>

                                </div>
                            )
                        }

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