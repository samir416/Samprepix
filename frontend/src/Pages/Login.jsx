import Navbar from "../Components/Common/Navbar";
import "../styles/authmodal.css";
import Logo from "../assets/Logo.png";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { loginUser } from "../services/authService";

export default function Login() {

    const navigate = useNavigate();
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
                        Welcome back
                    </h2>

                    <p className="auth-subtitle">
                        Sign in to continue your prep journey
                    </p>

                    {/* SOCIALS */}

                    <div className="auth-socials">

                        <button>

                            <span>⌘</span>

                            GitHub

                        </button>

                        <button>

                            <span>✉</span>

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

                                const data =
                                    await loginUser(
                                        email,
                                        password
                                    );

                                localStorage.setItem(
                                    "token",
                                    data.token
                                );

                                console.log(data);

                                navigate("/dashboard");
                            } catch (err) {

                                console.log(err);

                                setError(
                                    "Invalid email or password"
                                );
                            }
                        }}
                    >

                        <div className="auth-input-group">

                            <label>
                                Email
                            </label>

                            <input
                                type="email"
                                placeholder="you@university.edu"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
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
                                <p>
                                    {error}
                                </p>
                            )
                        }

                        <button
                            type="submit"
                            className="auth-submit-btn"
                        >

                            Sign in

                        </button>

                    </form>

                    {/* FOOTER */}

                    <p className="auth-bottom-text">

                        New here?
                        <Link to="/auth" className="auth-login-link">
                            Create an account
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