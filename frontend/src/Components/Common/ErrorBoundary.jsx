import React from "react";

export class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.error("ErrorBoundary caught an unhandled error:", error, errorInfo);
    }

    handleReset = () => {
        this.setState({ hasError: false, error: null });
        if (this.props.onReset) {
            this.props.onReset();
        }
    };

    handleGoHome = () => {
        this.setState({ hasError: false, error: null });
        window.location.href = "/dashboard";
    };

    render() {
        if (this.state.hasError) {
            if (this.props.fallback) {
                return this.props.fallback;
            }

            return (
                <div style={{
                    minHeight: "60vh",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    padding: "32px 16px",
                    textAlign: "center",
                    color: "var(--text-primary, #f1f5f9)",
                    background: "transparent"
                }}>
                    <div style={{
                        maxWidth: "480px",
                        width: "100%",
                        padding: "32px",
                        borderRadius: "16px",
                        background: "rgba(30, 41, 59, 0.7)",
                        border: "1px solid rgba(255, 255, 255, 0.1)",
                        backdropFilter: "blur(12px)",
                        boxShadow: "0 20px 40px rgba(0,0,0,0.3)"
                    }}>
                        <div style={{
                            width: "56px",
                            height: "56px",
                            borderRadius: "50%",
                            background: "rgba(239, 68, 68, 0.15)",
                            border: "1px solid rgba(239, 68, 68, 0.3)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            margin: "0 auto 16px auto",
                            fontSize: "24px",
                            color: "#ef4444"
                        }}>
                            ⚠️
                        </div>
                        <h2 style={{
                            fontSize: "1.35rem",
                            fontWeight: "600",
                            marginBottom: "8px",
                            color: "#fff"
                        }}>
                            Something went wrong
                        </h2>
                        <p style={{
                            fontSize: "0.9rem",
                            color: "var(--text-secondary, #94a3b8)",
                            marginBottom: "24px",
                            lineHeight: "1.5"
                        }}>
                            An unexpected error occurred while displaying this section. Please try again or return to your dashboard.
                        </p>
                        <div style={{
                            display: "flex",
                            gap: "12px",
                            justifyContent: "center",
                            flexWrap: "wrap"
                        }}>
                            <button
                                onClick={this.handleReset}
                                style={{
                                    padding: "10px 20px",
                                    borderRadius: "8px",
                                    background: "linear-gradient(135deg, #6366f1, #4f46e5)",
                                    color: "#fff",
                                    border: "none",
                                    cursor: "pointer",
                                    fontSize: "0.9rem",
                                    fontWeight: "500",
                                    transition: "opacity 0.2s"
                                }}
                            >
                                Try Again
                            </button>
                            <button
                                onClick={this.handleGoHome}
                                style={{
                                    padding: "10px 20px",
                                    borderRadius: "8px",
                                    background: "rgba(255, 255, 255, 0.08)",
                                    border: "1px solid rgba(255, 255, 255, 0.15)",
                                    color: "#fff",
                                    cursor: "pointer",
                                    fontSize: "0.9rem",
                                    fontWeight: "500"
                                }}
                            >
                                Go to Dashboard
                            </button>
                        </div>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
