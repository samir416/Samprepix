import { useState, useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaEnvelope, FaPaperPlane, FaComments, FaGraduationCap, FaExternalLinkAlt, FaCopy, FaCheck } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Contact() {
    const [copied, setCopied] = useState(false);

    useEffect(() => {
        updatePageSEO({
            title: "Contact Us | Samprepix",
            description: "Get in touch with the Samprepix team. Reach out directly for platform questions, curriculum feedback, or university inquiries.",
            canonicalPath: "/contact"
        });
        trackPageView("/contact", "Contact Us | Samprepix");
    }, []);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText("samirprajapat5@gmail.com");
            setCopied(true);
            setTimeout(() => setCopied(false), 2500);
        } catch {
            // Clipboard fallback
            const textArea = document.createElement("textarea");
            textArea.value = "samirprajapat5@gmail.com";
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand("copy");
            document.body.removeChild(textArea);
            setCopied(true);
            setTimeout(() => setCopied(false), 2500);
        }
    };

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Direct Communication</span>
                <h1>Contact Samprepix</h1>
                <p>
                    Have a question, need help with the platform, or want to share feedback? Reach out directly and your message will be reviewed.
                </p>
            </section>

            <main className="content-page-body">
                <div style={{ maxWidth: "760px", margin: "0 auto" }}>
                    {/* PRIMARY CONTACT CARD */}
                    <div
                        className="content-card"
                        style={{
                            padding: "40px",
                            textAlign: "center",
                            background: "var(--card-bg, #ffffff)",
                            border: "1px solid var(--border-color, #e2e8f0)",
                            borderRadius: "16px",
                            boxShadow: "0 4px 20px rgba(0, 0, 0, 0.04)"
                        }}
                    >
                        <div
                            style={{
                                width: "64px",
                                height: "64px",
                                borderRadius: "50%",
                                background: "rgba(99, 102, 241, 0.1)",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                margin: "0 auto 20px auto"
                            }}
                        >
                            <FaEnvelope style={{ color: "#4f46e5", fontSize: "1.8rem" }} />
                        </div>

                        <h2 style={{ fontSize: "1.5rem", fontWeight: "700", marginBottom: "12px" }}>
                            Send Us a Message
                        </h2>

                        <p style={{ fontSize: "1rem", lineHeight: "1.7", color: "var(--text-muted, #64748b)", maxWidth: "560px", margin: "0 auto 24px auto" }}>
                            We actively read candidate queries, curriculum suggestions, and university collaboration inquiries via our primary contact address:
                        </p>

                        <div style={{ margin: "20px 0 28px 0" }}>
                            <a
                                href="mailto:samirprajapat5@gmail.com"
                                style={{
                                    fontSize: "1.25rem",
                                    fontWeight: "700",
                                    color: "#4f46e5",
                                    textDecoration: "underline",
                                    textUnderlineOffset: "4px"
                                }}
                            >
                                samirprajapat5@gmail.com
                            </a>
                        </div>

                        <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", gap: "12px" }}>
                            <a
                                href="mailto:samirprajapat5@gmail.com"
                                className="contact-submit-btn"
                                style={{
                                    display: "inline-flex",
                                    alignItems: "center",
                                    gap: "8px",
                                    textDecoration: "none",
                                    padding: "12px 26px",
                                    fontSize: "0.95rem",
                                    fontWeight: "600",
                                    borderRadius: "10px"
                                }}
                            >
                                <FaPaperPlane style={{ fontSize: "0.9rem" }} /> Open Email
                            </a>

                            <a
                                href="https://mail.google.com/mail/?view=cm&fs=1&to=samirprajapat5@gmail.com"
                                target="_blank"
                                rel="noopener noreferrer"
                                className="contact-gmail-btn"
                                style={{
                                    display: "inline-flex",
                                    alignItems: "center",
                                    gap: "8px",
                                    textDecoration: "none",
                                    padding: "12px 24px",
                                    fontSize: "0.95rem",
                                    fontWeight: "600",
                                    borderRadius: "10px",
                                    background: "rgba(99, 102, 241, 0.08)",
                                    color: "#4f46e5",
                                    border: "1px solid rgba(99, 102, 241, 0.25)"
                                }}
                            >
                                <FaExternalLinkAlt style={{ fontSize: "0.85rem" }} /> Open in Gmail (Web)
                            </a>

                            <button
                                type="button"
                                onClick={handleCopy}
                                className="contact-copy-btn"
                                style={{
                                    display: "inline-flex",
                                    alignItems: "center",
                                    gap: "8px",
                                    padding: "12px 20px",
                                    fontSize: "0.95rem",
                                    fontWeight: "600",
                                    borderRadius: "10px",
                                    background: copied ? "#ecfdf5" : "var(--card-bg, #f8fafc)",
                                    color: copied ? "#059669" : "var(--text-muted, #64748b)",
                                    border: `1px solid ${copied ? "#a7f3d0" : "var(--border-color, #e2e8f0)"}`,
                                    cursor: "pointer",
                                    transition: "all 0.2s ease"
                                }}
                            >
                                {copied ? <FaCheck style={{ fontSize: "0.85rem" }} /> : <FaCopy style={{ fontSize: "0.85rem" }} />}
                                {copied ? "Address Copied!" : "Copy Address"}
                            </button>
                        </div>
                    </div>

                    {/* TOPIC GUIDANCE */}
                    <div style={{ marginTop: "40px" }}>
                        <h3 style={{ fontSize: "1.15rem", fontWeight: "700", marginBottom: "16px", textAlign: "center" }}>
                            How We Can Help
                        </h3>

                        <div className="content-grid-2">
                            <div className="content-card">
                                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "8px" }}>
                                    <FaComments style={{ color: "#4f46e5", fontSize: "1.1rem" }} />
                                    <h4 style={{ margin: 0, fontSize: "1rem", fontWeight: "600" }}>Platform & Account Inquiries</h4>
                                </div>
                                <p style={{ fontSize: "0.88rem", lineHeight: "1.6", color: "var(--text-muted, #64748b)", margin: 0 }}>
                                    Assistance with coding arena compiler sandboxes, aptitude session history, or settings configuration.
                                </p>
                            </div>

                            <div className="content-card">
                                <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "8px" }}>
                                    <FaGraduationCap style={{ color: "#059669", fontSize: "1.1rem" }} />
                                    <h4 style={{ margin: 0, fontSize: "1rem", fontWeight: "600" }}>Curriculum & Campus Pilots</h4>
                                </div>
                                <p style={{ fontSize: "0.88rem", lineHeight: "1.6", color: "var(--text-muted, #64748b)", margin: 0 }}>
                                    Suggestions for problem sets, edge-case test case feedback, or university placement cell inquiries.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
}
