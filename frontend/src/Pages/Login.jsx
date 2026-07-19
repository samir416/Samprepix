import Navbar from "../Components/Common/Navbar";
import "../styles/authmodal.css";
import Logo from "../assets/Logo.png";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { loginUser } from "../services/authService";
import { useSearchParams } from "react-router-dom";

export default function Login() {

    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {

        setEmail("");
        setPassword("");

    }, []);

    useEffect(() => {

        const token = searchParams.get("token");

        if (token) {

            localStorage.setItem("token", token);

            navigate("/dashboard");
        }

    }, [searchParams, navigate]);


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
                        Continue Your Career Journey
                    </p>

                    {/* SOCIALS */}

                    <div className="auth-socials">

                        <button
                            type="button"
                            onClick={() => {
                                window.location.href =
                                    "http://localhost:8080/oauth2/authorization/github";
                            }}
                        >

                            <span>⌘</span>

                            GitHub

                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                window.location.href =
                                    "http://localhost:8080/oauth2/authorization/google";
                            }}
                        >

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
                        autoComplete="off"
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
                                autoComplete="off"
                                value={email}
                                name="login_email"
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
                                autoComplete="off"
                                value={password}
                                name="login_password"
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