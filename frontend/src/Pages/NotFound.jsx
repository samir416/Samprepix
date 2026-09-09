import { useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { FaHome, FaCompass } from "react-icons/fa";
import "../styles/contentPages.css";

export default function NotFound() {
    useEffect(() => {
        updatePageSEO({
            title: "404 - Page Not Found | Samprepix",
            description: "The requested page does not exist or has been relocated.",
            noIndex: true
        });
    }, []);

    return (
        <div className="content-page">
            <Navbar />

            <main className="content-page-body" style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", textAlign: "center", minHeight: "60vh", padding: "60px 24px" }}>
                <span className="content-page-badge" style={{ color: "#ef4444", background: "rgba(239, 68, 68, 0.08)", borderColor: "rgba(239, 68, 68, 0.2)" }}>
                    Error 404
                </span>

                <h1 style={{ fontSize: "clamp(2.5rem, 6vw, 4.5rem)", fontWeight: "800", letterSpacing: "-0.04em", margin: "12px 0 16px 0" }}>
                    Page Not Found
                </h1>

                <p style={{ maxWidth: "540px", color: "var(--text-muted, #64748b)", fontSize: "1.05rem", lineHeight: "1.7", margin: "0 0 32px 0" }}>
                    The page you are looking for might have been moved, renamed, or is temporarily unavailable. Let&apos;s get you back on track.
                </p>

                <div style={{ display: "flex", gap: "16px", flexWrap: "wrap", justifyContent: "center" }}>
                    <Link
                        to="/"
                        style={{
                            display: "inline-flex",
                            alignItems: "center",
                            gap: "8px",
                            padding: "12px 24px",
                            borderRadius: "10px",
                            background: "#4f46e5",
                            color: "#ffffff",
                            textDecoration: "none",
                            fontWeight: "600",
                            fontSize: "0.95rem"
                        }}
                    >
                        <FaHome /> Return Home
                    </Link>

                    <Link
                        to="/features"
                        style={{
                            display: "inline-flex",
                            alignItems: "center",
                            gap: "8px",
                            padding: "12px 24px",
                            borderRadius: "10px",
                            background: "rgba(99, 102, 241, 0.08)",
                            color: "#4f46e5",
                            textDecoration: "none",
                            fontWeight: "600",
                            fontSize: "0.95rem",
                            border: "1px solid rgba(99, 102, 241, 0.2)"
                        }}
                    >
                        <FaCompass /> Explore Features
                    </Link>
                </div>
            </main>

            <Footer />
        </div>
    );
}
