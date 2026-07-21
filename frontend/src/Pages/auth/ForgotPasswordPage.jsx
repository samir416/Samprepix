import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { forgotPassword } from "../../services/authService";
import "../../styles/ForgotPasswordPage.css";

export default function ForgotPasswordPage() {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");

    const [success, setSuccess] = useState("");

    const handleSubmit = async (e) => {

        e.preventDefault();

        setError("");

        setSuccess("");

               try {

            setLoading(true);

            const response = await forgotPassword(email);

            setSuccess(
                response.message
            );

        } catch (err) {

            console.log("Forgot Password Error");
            console.log(err);
            console.log(err.response);
            console.log(err.response?.data);

            setError(
                err.response?.data?.message ||
                err.response?.data ||
                err.message ||
                "Unable to send reset link."
            );

        } finally {

            setLoading(false);

        }

    };

    return (

        <div className="forgot-password-page">

            <div className="forgot-password-card">

                <h2 className="forgot-password-title">

                    Forgot Password

                </h2>

                <p className="forgot-password-subtitle">

                    Enter your registered email to receive a password reset link.

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
                    className="forgot-password-form"
                    onSubmit={handleSubmit}
                >

                                        <div className="form-group">

                        <label>

                            Email Address

                        </label>

                        <input
                            type="email"
                            placeholder="Enter your registered email"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                            required
                        />

                    </div>

                    <button
                        type="submit"
                        className="forgot-password-btn"
                        disabled={loading}
                    >

                        {
                            loading
                                ? "Sending Reset Link..."
                                : "Send Reset Link"
                        }

                    </button>

                </form>

                <div className="forgot-password-footer">

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
