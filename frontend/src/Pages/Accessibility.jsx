import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaUniversalAccess, FaKeyboard, FaAdjust, FaEye, FaEnvelope } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Accessibility() {
    useEffect(() => {
        updatePageSEO({
            title: "Accessibility Standards | Samprepix",
            description: "Learn about accessibility features, keyboard navigation, high contrast themes, and reduced motion controls in Samprepix.",
            canonicalPath: "/accessibility"
        });
        trackPageView("/accessibility", "Accessibility | Samprepix");
    }, []);

    const accessibilityFeatures = [
        {
            icon: <FaKeyboard style={{ color: "#4f46e5", fontSize: "1.4rem" }} />,
            title: "Full Keyboard Navigation",
            description: "Core interactive surfaces including global navigation, coding problem drawers, Monaco code editor shortcuts, and dialog modals can be navigated and operated using standard keyboard tab keys and keyboard shortcuts."
        },
        {
            icon: <FaAdjust style={{ color: "#059669", fontSize: "1.4rem" }} />,
            title: "High-Contrast Light & Dark Themes",
            description: "The platform provides tailored light and dark appearances designed with readable typography, defined text-to-background contrast ratios, and comfortable code editor color schemes to reduce eye fatigue during extended study."
        },
        {
            icon: <FaEye style={{ color: "#7c3aed", fontSize: "1.4rem" }} />,
            title: "Reduced Motion Support",
            description: "Samprepix respects your operating system 'prefers-reduced-motion' setting. When enabled, non-essential interface animations, floating transitions, and hover motion effects are disabled to provide a stable, non-distracting reading experience."
        },
        {
            icon: <FaUniversalAccess style={{ color: "#2563eb", fontSize: "1.4rem" }} />,
            title: "Semantic HTML & Screen Reader Labels",
            description: "All views use standard semantic HTML5 elements (nav, main, aside, footer, article) and descriptive ARIA attributes to ensure assistive screen-reader technologies can identify content structure and state changes accurately."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Inclusive Design</span>
                <h1>Accessibility Commitment</h1>
                <p>
                    Ensuring that algorithmic preparation, coding sandboxes, and assessment tools are usable, comfortable, and accessible to every candidate.
                </p>
            </section>

            <main className="content-page-body">
                <div className="content-grid-2">
                    {accessibilityFeatures.map((f, idx) => (
                        <div key={idx} className="content-card">
                            <div style={{ marginBottom: "12px" }}>{f.icon}</div>
                            <h2 style={{ fontSize: "1.15rem", fontWeight: "700", margin: "0 0 8px 0" }}>
                                {f.title}
                            </h2>
                            <p style={{ fontSize: "0.9rem", lineHeight: "1.7", color: "var(--text-muted, #64748b)", margin: 0 }}>
                                {f.description}
                            </p>
                        </div>
                    ))}
                </div>

                <div
                    style={{
                        marginTop: "48px",
                        padding: "32px",
                        borderRadius: "16px",
                        background: "rgba(99, 102, 241, 0.04)",
                        border: "1px solid rgba(99, 102, 241, 0.12)",
                        textAlign: "center"
                    }}
                >
                    <FaEnvelope style={{ fontSize: "2rem", color: "#4f46e5", marginBottom: "12px" }} />
                    <h3 style={{ fontSize: "1.3rem", fontWeight: "700", marginBottom: "8px" }}>
                        Accessibility Feedback & Assistance
                    </h3>
                    <p style={{ maxWidth: "640px", margin: "auto", fontSize: "0.92rem", color: "var(--text-muted, #64748b)", lineHeight: "1.7", marginBottom: "20px" }}>
                        We continuously test and improve our interface ergonomics. If you encounter an accessibility barrier or have suggestions for screen reader or keyboard optimization, please let us know directly.
                    </p>
                    <a
                        href="mailto:samirprajapat5@gmail.com?subject=Samprepix%20Accessibility%20Feedback"
                        className="contact-submit-btn"
                        style={{ display: "inline-block", textDecoration: "none", padding: "10px 24px" }}
                    >
                        Share Feedback: samirprajapat5@gmail.com →
                    </a>
                </div>
            </main>

            <Footer />
        </div>
    );
}
