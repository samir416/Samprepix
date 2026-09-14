import Navbar from "../Common/Navbar";
import "../../styles/authmodal.css";
import Logo from "../../assets/Logo.png";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { registerUser, getCurrentUser } from "../../services/authService";
export default function AuthModal() {

    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

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

                        <button
                            type="button"
                            onClick={() => {
                                window.location.href =
                                    "http://localhost:8080/oauth2/register/github";
                            }}>

                            <span>
                                ⌘
                            </span>

                            GitHub

                        </button>

                        <button
                            type="button"
                            onClick={() => {
                                window.location.href =
                                    "http://localhost:8080/oauth2/register/google";
                            }}

                        >

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

                                setLoading(true);

                                const response = await registerUser(
                                    name,
                                    username,
                                    email,
                                    password
                                );

                                if (response?.token) {
                                    localStorage.setItem("token", response.token);
                                    const user = await getCurrentUser();
                                    localStorage.setItem("user", JSON.stringify(user));
                                    if (user?.profileCompleted) {
                                        localStorage.setItem("onboardingCompleted", "true");
                                        navigate("/dashboard", { replace: true });
                                    } else {
                                        localStorage.removeItem("onboardingCompleted");
                                        navigate("/onboarding", { replace: true });
                                    }
                                } else {
                                    setError("Account created, but authentication token was missing. Please log in.");
                                    navigate("/login", { replace: true });
                                }

                            } catch (err) {

                                setError(
                                    err?.response?.data?.message ||
                                    err?.response?.data ||
                                    "Please review your details and try again."
                                );

                            } finally {

                                setLoading(false);

                            }
                        }}
                    >
                        <div className="auth-input-group">

    <label>
        Full Name
    </label>

    <input
        type="text"
        placeholder="John Doe"
        value={name}
        onChange={(e) =>
            setName(e.target.value)
        }
    />

</div>

<div className="auth-input-group">

    <label>
        Username
    </label>

    <input
        type="text"
        placeholder="john416"
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
                                placeholder="john@example.com"
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
                            disabled={loading}
                        >

                            {
                                loading
                                    ? "Creating..."
                                    : "Create account"
                            }

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