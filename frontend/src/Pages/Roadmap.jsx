import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaCheckCircle, FaServer, FaBrain, FaGraduationCap } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Roadmap() {
    useEffect(() => {
        updatePageSEO({
            title: "Product Evolution & Milestones | Samprepix",
            description: "Explore the architectural milestones and completed capabilities powering the Samprepix engineering placement platform.",
            canonicalPath: "/roadmap"
        });
        trackPageView("/roadmap", "Roadmap & Milestones | Samprepix");
    }, []);

    const milestones = [
        {
            title: "Core Execution Engine",
            badge: "Production Milestone",
            badgeClass: "released",
            icon: <FaServer style={{ color: "#4f46e5", fontSize: "1.3rem" }} />,
            items: [
                {
                    name: "Isolated Multi-Language Sandboxes",
                    description: "Secure, low-latency execution environment supporting 50+ compiled and interpreted programming language runtimes with memory bounds and execution timeouts."
                },
                {
                    name: "Monaco IDE Integration",
                    description: "Browser-based algorithmic workbench featuring syntax highlighting, IntelliSense, customizable font sizing, and real-time execution consoles."
                },
                {
                    name: "Deterministic Language Resolution",
                    description: "4-step language hierarchy guaranteeing database problem isolation, problem-level session overrides, and saved user preferences."
                }
            ]
        },
        {
            title: "Curriculum & Assessment Scale",
            badge: "Completed Milestone",
            badgeClass: "released",
            icon: <FaGraduationCap style={{ color: "#059669", fontSize: "1.3rem" }} />,
            items: [
                {
                    name: "5,050+ Algorithmic Challenges",
                    description: "Curated problem library spanning Easy to Hard difficulty ratings across Dynamic Programming, Graphs, Trees, Arrays, and Strings."
                },
                {
                    name: "1,200+ Relational SQL Sandboxes",
                    description: "Automated schema provisioning running genuine MySQL 8.0 instances with tabular output comparison against expected datasets."
                },
                {
                    name: "22,060 Aptitude Practice Modules",
                    description: "Standardized testing engine covering Quantitative, Logical, Verbal, and Technical placement assessment domains."
                }
            ]
        },
        {
            title: "AI Simulation & Career Intelligence",
            badge: "Active Capabilities",
            badgeClass: "released",
            icon: <FaBrain style={{ color: "#7c3aed", fontSize: "1.3rem" }} />,
            items: [
                {
                    name: "Voice AI Mock Interviews",
                    description: "Interactive real-time speech interviews providing objective feedback on technical clarity, communication pacing, and algorithmic justification."
                },
                {
                    name: "ATS Resume Analyzer",
                    description: "Document parsing engine that scores structural readability, keyword density, and technical competence against target job profiles."
                },
                {
                    name: "Automated GitHub Synchronization",
                    description: "Automatic commit synchronization of passing algorithmic solutions directly to personal candidate GitHub repositories."
                }
            ]
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Product Evolution</span>
                <h1>Architecture & Milestones</h1>
                <p>
                    A transparent review of the platform milestones, engineering foundations, and production capabilities delivered across the Samprepix ecosystem.
                </p>
            </section>

            <main className="content-page-body">
                <div className="content-grid-3">
                    {milestones.map((col, idx) => (
                        <div key={idx} className="content-card" style={{ display: "flex", flexDirection: "column" }}>
                            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "16px" }}>
                                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                    {col.icon}
                                    <h2 style={{ fontSize: "1.15rem", fontWeight: "700", margin: 0 }}>{col.title}</h2>
                                </div>
                                <span className={`timeline-tag ${col.badgeClass}`}>{col.badge}</span>
                            </div>
                            <div style={{ display: "flex", flexDirection: "column", gap: "16px", flex: 1 }}>
                                {col.items.map((item, i) => (
                                    <div
                                        key={i}
                                        style={{
                                            padding: "16px",
                                            borderRadius: "10px",
                                            background: "rgba(99, 102, 241, 0.04)",
                                            border: "1px solid rgba(99, 102, 241, 0.1)"
                                        }}
                                    >
                                        <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "6px" }}>
                                            <FaCheckCircle style={{ color: "#059669", fontSize: "0.85rem", flexShrink: 0 }} />
                                            <h3 style={{ fontSize: "0.95rem", fontWeight: "700", margin: 0, color: "inherit" }}>
                                                {item.name}
                                            </h3>
                                        </div>
                                        <p style={{ fontSize: "0.86rem", lineHeight: "1.6", margin: 0, color: "var(--text-muted, #64748b)" }}>
                                            {item.description}
                                        </p>
                                    </div>
                                ))}
                            </div>
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
                    <h3 style={{ fontSize: "1.2rem", fontWeight: "700", marginBottom: "8px" }}>
                        Production Maturity Standards
                    </h3>
                    <p style={{ maxWidth: "720px", margin: "auto", fontSize: "0.92rem", color: "var(--text-muted, #64748b)", lineHeight: "1.7" }}>
                        Samprepix is engineered for high stability and authentic simulation. All features, compiler sandboxes, aptitude banks, and evaluation models shown above are active, fully tested, and verified in production.
                    </p>
                </div>
            </main>

            <Footer />
        </div>
    );
}
