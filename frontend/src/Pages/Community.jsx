import { useEffect } from "react";
import { Link } from "react-router-dom";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaComments, FaBug, FaGraduationCap, FaEnvelope } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Community() {
    useEffect(() => {
        updatePageSEO({
            title: "Community & Feedback | Samprepix",
            description: "Connect with the Samprepix engineering team, submit platform feedback, report problem discrepancies, and request curriculum enhancements.",
            canonicalPath: "/community"
        });
        trackPageView("/community", "Community & Feedback | Samprepix");
    }, []);

    const feedbackChannels = [
        {
            icon: <FaComments style={{ fontSize: "1.8rem", color: "#4f46e5" }} />,
            title: "Feature Requests & Product Feedback",
            description: "Have ideas for interview simulation scenarios, coding arena features, or UI improvements? We review candidate suggestions directly via samirprajapat5@gmail.com.",
            actionText: "Email Feature Ideas",
            link: "mailto:samirprajapat5@gmail.com?subject=Samprepix%20Feature%20Feedback"
        },
        {
            icon: <FaBug style={{ fontSize: "1.8rem", color: "#e11d48" }} />,
            title: "Issue & Test Case Reporting",
            description: "Spotted an ambiguous test case, compiler sandbox discrepancy, or broken problem description? Report it directly to samirprajapat5@gmail.com for prompt resolution.",
            actionText: "Report Issue via Email",
            link: "mailto:samirprajapat5@gmail.com?subject=Samprepix%20Issue%20Report"
        },
        {
            icon: <FaGraduationCap style={{ fontSize: "1.8rem", color: "#059669" }} />,
            title: "Campus & Training Cell Inquiries",
            description: "University placement officers, student training coordinators, and college coding clubs can reach out directly to samirprajapat5@gmail.com for institutional discussions.",
            actionText: "Institutional Inquiry Email",
            link: "mailto:samirprajapat5@gmail.com?subject=Samprepix%20Campus%20Inquiry"
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Candidate Feedback</span>
                <h1>Community & Feedback</h1>
                <p>
                    We build Samprepix directly in collaboration with students, educators, and hiring teams. Share your feedback, report issues, and help shape our engineering preparation platform.
                </p>
            </section>

            <main className="content-page-body">
                <div className="content-grid-3">
                    {feedbackChannels.map((ch, idx) => (
                        <div key={idx} className="content-card" style={{ display: "flex", flexDirection: "column" }}>
                            <div style={{ marginBottom: "14px" }}>{ch.icon}</div>
                            <h2 style={{ fontSize: "1.15rem", fontWeight: "700", margin: "0 0 8px 0" }}>
                                {ch.title}
                            </h2>
                            <p style={{ fontSize: "0.9rem", lineHeight: "1.6", color: "var(--text-muted, #64748b)", flex: 1, margin: "0 0 16px 0" }}>
                                {ch.description}
                            </p>
                            <a
                                href={ch.link}
                                style={{
                                    color: "#4f46e5",
                                    fontWeight: "600",
                                    fontSize: "0.88rem",
                                    textDecoration: "none"
                                }}
                            >
                                {ch.actionText} →
                            </a>
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
                        Need Direct Support?
                    </h3>
                    <p style={{ maxWidth: "620px", margin: "auto", fontSize: "0.92rem", color: "var(--text-muted, #64748b)", lineHeight: "1.7", marginBottom: "20px" }}>
                        Our engineering support team monitors candidate inquiries daily. For account assistance, syllabus questions, or feedback, you can reach us via our dedicated contact portal.
                    </p>
                    <Link
                        to="/contact"
                        className="contact-submit-btn"
                        style={{ display: "inline-block", textDecoration: "none", padding: "10px 24px" }}
                    >
                        Visit Contact & Support Page →
                    </Link>
                </div>
            </main>

            <Footer />
        </div>
    );
}
