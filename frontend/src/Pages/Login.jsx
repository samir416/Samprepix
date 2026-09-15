import { API_BASE_URL } from "../config";
import Navbar from "../Components/Common/Navbar";
import AppLoader from "../Components/Common/AppLoader";
import "../styles/authmodal.css";
import Logo from "../assets/Logo.png";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { loginUser, getCurrentUser, getCleanToken } from "../services/authService";
import { useSearchParams } from "react-router-dom";

export default function Login() {

    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [showSplash, setShowSplash] = useState(false);

    useEffect(() => {

        setEmail("");
        setPassword("");

        const token = searchParams.get("token");
        if (!token) {
            const existingToken = getCleanToken();
            if (existingToken) {
                try {
                    const rawUser = localStorage.getItem("user");
                    if (rawUser) {
                        const parsed = JSON.parse(rawUser);
                        if (parsed?.profileCompleted === true) {
                            navigate("/dashboard", { replace: true });
                            return;
                        } else if (parsed?.profileCompleted === false) {
                            navigate("/onboarding", { replace: true });
                            return;
                        }
                    }
                } catch (_) {}
            }
        }

    }, [navigate, searchParams]);

    useEffect(() => {

        const token = searchParams.get("token");
        const oauthError = searchParams.get("oauthError");

        const loadUser = async () => {

            localStorage.setItem("token", token);

            try {

                const user = await getCurrentUser();

                localStorage.setItem(
                    "user",
                    JSON.stringify(user)
                );

                if (user?.profileCompleted) {
                    localStorage.setItem("onboardingCompleted", "true");
                    setShowSplash(true);
                    setTimeout(() => {
                        navigate("/dashboard", { replace: true });
                    }, 2000);
                } else {
                    localStorage.removeItem("onboardingCompleted");
                    navigate("/onboarding", { replace: true });
                }

            } catch {

                localStorage.removeItem("token");

                setError("Unable to fetch your account details. Please try signing in again.");

            }

        };

        if (token) {

            loadUser();
            return;

        }

        if (oauthError) {
            const raw = decodeURIComponent(oauthError);
            let friendly = "Sign-in was interrupted or could not be completed. Please try again.";
            if (raw === "session_expired" || raw.includes("authorization_request_not_found")) {
                friendly = "Your sign-in session expired or was interrupted. Please try again.";
            } else if (raw === "access_denied" || raw.includes("access_denied")) {
                friendly = "Authentication was cancelled. You can try again whenever you are ready.";
            } else if (raw === "security_verification_failed") {
                friendly = "Authentication security check failed. Please try signing in again.";
            } else if (raw && !raw.startsWith("[")) {
                friendly = raw;
            }
            setError(friendly);

            window.history.replaceState({}, "", "/login");

        }

    }, [searchParams, navigate]);

    return (

        <>
            <AppLoader visible={showSplash} />

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
                                    API_BASE_URL + "/oauth2/authorize/github";
                            }}
                        >
                            <span>⌘</span>
                            GitHub
                        </button>

                        <button
                            type="button"
                            onClick={() => {
                                window.location.href =
                                    API_BASE_URL + "/oauth2/authorize/google";
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

                                setLoading(true);

                                const data = await loginUser(
                                    email,
                                    password
                                );

                                localStorage.setItem(
                                    "token",
                                    data.token
                                );

                                const user = await getCurrentUser();

                                localStorage.setItem(
                                    "user",
                                    JSON.stringify(user)
                                );

                                if (user?.profileCompleted) {
                                    localStorage.setItem("onboardingCompleted", "true");
                                    setShowSplash(true);
                                    setTimeout(() => {
                                        navigate("/dashboard", { replace: true });
                                    }, 2000);
                                } else {
                                    localStorage.removeItem("onboardingCompleted");
                                    navigate("/onboarding", { replace: true });
                                }

                            } catch (err) {

                                setError(

                                    err?.response?.data?.message ||

                                    err?.response?.data ||

                                    "Please check your credentials and try again."

                                );

                            } finally {

                                setLoading(false);

                            }
                        }}
                    >

                        <div className="auth-input-group">

                            <label>
                                Username or Email
                            </label>

                            <input
                                type="email"
                                placeholder="Enter your username or email"
                                value={email}
                                name="oauth-login-email"
                                autoComplete="new-password"
                                spellCheck={false}
                                autoCapitalize="off"
                                autoCorrect="off"
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
                                name="oauth-login-password"
                                autoComplete="new-password"
                                onChange={(e) => setPassword(e.target.value)}
                            />


                        </div>

                        <div className="auth-options">

                            <label className="auth-remember">

                                <input
                                    type="checkbox"
                                />

                                <span>Remember me</span>

                            </label>

                            <Link
                                to="/forgot-password"
                                className="auth-forgot"
                            >
                                Forgot Password?
                            </Link>

                        </div>
                        {
                            error && (
                                <div className="auth-error-alert">

                                    <div className="auth-error-icon">
                                        ⚠
                                    </div>

                                    <div className="auth-error-content">

                                        <h6>
                                            Unable to Sign In
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
                                    ? "Signing in..."
                                    : "Sign in"
                            }

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