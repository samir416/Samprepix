import { useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { resetPassword } from "../../services/authService";
import "../../styles/ResetPasswordPage.css";

export default function ResetPasswordPage() {

    const navigate = useNavigate();

    const [searchParams] = useSearchParams();

    const token = searchParams.get("token");

    const [password, setPassword] = useState("");

    const [confirmPassword, setConfirmPassword] = useState("");

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");

    const [success, setSuccess] = useState("");

    const passwordStrength = useMemo(() => {

        if (!password) {

            return {
                label: "",
                className: ""
            };

        }

        let score = 0;

        if (password.length >= 8) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[a-z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;

        if (score <= 2) {

            return {
                label: "Weak Password",
                className: "weak"
            };

        }

        if (score <= 4) {

            return {
                label: "Medium Password",
                className: "medium"
            };

        }

        return {
            label: "Strong Password",
            className: "strong"
        };

    }, [password]);


    const handleSubmit = async (e) => {

        e.preventDefault();

        setError("");

        setSuccess("");

        if (!token) {

            setError("Invalid or expired reset link.");

            return;

        }

        if (password.length < 6) {

            setError("Password must be at least 6 characters.");

            return;

        }

        if (password !== confirmPassword) {

            setError("Passwords do not match.");

            return;

        }

        try {

            setLoading(true);

            const response = await resetPassword(
                token,
                password
            );

            setSuccess(
                response.message
            );

            setTimeout(() => {

                navigate("/login");

            }, 2000);

        } catch (err) {

            console.log("Reset Password Error");
            console.log(err);
            console.log(err.response);
            console.log(err.response?.data);

            setError(
                err.response?.data?.message ||
                err.response?.data ||
                err.message ||
                "Something went wrong."
            );

        } finally {

            setLoading(false);

        }


    };

    return (

        <div className="reset-password-page">

            <div className="reset-password-card">

                <h2 className="reset-password-title">
                    Reset Password
                </h2>

                <p className="reset-password-subtitle">
                    Create a new password for your account.
                </p>

                {
                    success && (

                        <div className="success-message">

                            {success}

                        </div>

                    )
                }

                {
                    error && (

                        <div className="error-message">

                            {error}

                        </div>

                    )
                }

                <form
                    onSubmit={handleSubmit}
                    className="reset-password-form"
                >

                    <div className="form-group">

                        <label>
                            New Password
                        </label>

                        <input
                            type="password"
                            placeholder="Enter new password"
                            value={password}
                            onChange={(e) =>
                                setPassword(e.target.value)
                            }
                            required
                        />
                        {
                            password && (

                                <div className={`password-strength ${passwordStrength.className}`}>

                                    {passwordStrength.label}

                                </div>

                            )
                        }

                    </div>

                    <div className="form-group">

                        <label>
                            Confirm Password
                        </label>

                        <input
                            type="password"
                            placeholder="Confirm new password"
                            value={confirmPassword}
                            onChange={(e) =>
                                setConfirmPassword(e.target.value)
                            }
                            onPaste={(e) => e.preventDefault()}
                            onCopy={(e) => e.preventDefault()}
                            onCut={(e) => e.preventDefault()}
                            onDragStart={(e) => e.preventDefault()}
                            required
                        />

                        {
                            confirmPassword && (

                                <div
                                    className={
                                        password === confirmPassword
                                            ? "password-match success"
                                            : "password-match error"
                                    }
                                >

                                    {
                                        password === confirmPassword
                                            ? "✔ Passwords Match"
                                            : "✖ Passwords do not match"
                                    }

                                </div>

                            )
                        }


                    </div>

                    <button
                        type="submit"
                        className="reset-password-btn"
                        disabled={loading}
                    >

                        {
                            loading
                                ? "Updating Password..."
                                : "Reset Password"
                        }

                    </button>

                </form>

                <div className="reset-password-footer">

                    Remember your password?

                    <button
                        type="button"
                        className="login-link-btn"
                        onClick={() => navigate("/login")}
                    >

                        Back to Login

                    </button>

                </div>

            </div>

        </div>

    );

}
