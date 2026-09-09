import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaShieldAlt, FaLock, FaUserSecret, FaServer, FaEnvelope } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Security() {
    useEffect(() => {
        updatePageSEO({
            title: "Security & Trust | Samprepix",
            description: "Learn about the security practices, code sandbox isolation, and data privacy protections implemented on Samprepix.",
            canonicalPath: "/security"
        });
        trackPageView("/security", "Security & Trust | Samprepix");
    }, []);

    const securityPillars = [
        {
            icon: <FaServer style={{ color: "#4f46e5", fontSize: "1.4rem" }} />,
            title: "Isolated Execution Sandboxes",
            description: "All code submitted in our Coding Arena runs within ephemeral, unprivileged runtime sandboxes. Runtimes have strictly enforced CPU quotas, memory boundaries (256MB to 512MB), wall-clock timeouts (2 to 5 seconds), and blocked outbound network access to prevent unauthorized network activity."
        },
        {
            icon: <FaUserSecret style={{ color: "#059669", fontSize: "1.4rem" }} />,
            title: "Candidate Data Privacy",
            description: "Your preparation data belongs exclusively to you. Resumes parsed in the ATS analyzer, mock interview audio recordings, and coding submissions remain private to your account. We never sell candidate data, license it to third parties, or use personal submissions for commercial model training."
        },
        {
            icon: <FaLock style={{ color: "#7c3aed", fontSize: "1.4rem" }} />,
            title: "Authentication & Credential Safety",
            description: "User passwords are cryptographically hashed before persistence using standard industry-grade salted one-way hashing algorithms. Session authentication uses signed, time-limited tokens with automatic expiration. Plaintext passwords and sensitive credentials are never stored or logged."
        },
        {
            icon: <FaShieldAlt style={{ color: "#2563eb", fontSize: "1.4rem" }} />,
            title: "Third-Party Authorization Scopes",
            description: "When connecting your GitHub account for solution synchronization, we request only the minimal necessary OAuth scopes required to push passing algorithmic solutions to your designated repository. We never read or modify other personal repositories or private account configurations."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">System Protection</span>
                <h1>Security & Trust</h1>
                <p>
                    How we safeguard candidate code execution, protect candidate privacy, and maintain platform integrity.
                </p>
            </section>

            <main className="content-page-body">
                <div className="content-grid-2">
                    {securityPillars.map((p, idx) => (
                        <div key={idx} className="content-card">
                            <div style={{ marginBottom: "12px" }}>{p.icon}</div>
                            <h2 style={{ fontSize: "1.15rem", fontWeight: "700", margin: "0 0 8px 0" }}>
                                {p.title}
                            </h2>
                            <p style={{ fontSize: "0.9rem", lineHeight: "1.7", color: "var(--text-muted, #64748b)", margin: 0 }}>
                                {p.description}
                            </p>
                        </div>
                    ))}
                </div>

                <div
                    style={{
                        marginTop: "48px",
                        padding: "36px",
                        borderRadius: "16px",
                        background: "rgba(99, 102, 241, 0.04)",
                        border: "1px solid rgba(99, 102, 241, 0.12)",
                        textAlign: "center"
                    }}
                >
                    <FaEnvelope style={{ fontSize: "2rem", color: "#4f46e5", marginBottom: "12px" }} />
                    <h3 style={{ fontSize: "1.3rem", fontWeight: "700", marginBottom: "8px" }}>
                        Responsible Disclosure & Security Inquiries
                    </h3>
                    <p style={{ maxWidth: "640px", margin: "auto", fontSize: "0.92rem", color: "var(--text-muted, #64748b)", lineHeight: "1.7", marginBottom: "20px" }}>
                        If you believe you have found a security defect, sandbox discrepancy, or vulnerability on the platform, please report it directly to our team so we can address it promptly.
                    </p>
                    <a
                        href="mailto:samirprajapat5@gmail.com?subject=Samprepix%20Security%20Inquiry"
                        className="contact-submit-btn"
                        style={{ display: "inline-block", textDecoration: "none", padding: "10px 24px" }}
                    >
                        Contact Security: samirprajapat5@gmail.com →
                    </a>
                </div>
            </main>

            <Footer />
        </div>
    );
}
