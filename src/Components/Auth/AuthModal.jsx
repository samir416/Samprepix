import React, { useEffect } from "react";
import "../../styles/authmodal.css";

const AuthModal = ({ isOpen, onClose }) => {

    useEffect(() => {

        const handleEsc = (e) => {

            if (e.key === "Escape") {
                onClose();
            }
        };

        document.addEventListener("keydown", handleEsc);

        return () => {
            document.removeEventListener("keydown", handleEsc);
        };

    }, [onClose]);

    if (!isOpen) return null;

    return (

        <div
            className="auth-overlay"
            onClick={onClose}
        >

            <div
                className="auth-modal"
                onClick={(e) => e.stopPropagation()}
            >

                {/* LOGO */}

                <div className="auth-logo">

                    <img
                        src="/Logo.png"
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

                <form className="auth-form">

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

                    <span>
                        Sign in
                    </span>

                </p>

                <p className="auth-security">

                    Secured with JWT • 256-bit encryption

                </p>

            </div>

        </div>
    );
};

export default AuthModal;