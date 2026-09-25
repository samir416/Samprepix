import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaCode, FaBrain, FaChartLine, FaShieldAlt } from "react-icons/fa";
import "../styles/contentPages.css";

export default function About() {
    useEffect(() => {
        updatePageSEO({
            title: "About Samprepix | Engineering Career Acceleration",
            description: "Learn about the mission, engineering philosophy, and architecture powering the Samprepix AI placement platform.",
            canonicalPath: "/about"
        });
        trackPageView("/about", "About Us | Samprepix");
    }, []);

    const pillars = [
        {
            icon: <FaCode style={{ color: "#4f46e5", fontSize: "1.5rem" }} />,
            title: "Authentic Code Sandboxes",
            description: "No mocked execution. Every code submission in our Coding Arena runs against real language compilers and interpreters across 50+ programming runtimes and SQL sandboxes with strict time and memory sandboxing."
        },
        {
            icon: <FaBrain style={{ color: "#4f46e5", fontSize: "1.5rem" }} />,
            title: "Objective AI Evaluation",
            description: "AI mock interviews are graded using calibrated scoring rubrics covering communication clarity, technical accuracy, algorithm justification, and structured problem-solving."
        },
        {
            icon: <FaChartLine style={{ color: "#4f46e5", fontSize: "1.5rem" }} />,
            title: "Deep Curriculum Breadth",
            description: "With 5,050 curated coding problems, 1,200 SQL challenges, and 22,060 aptitude assessments, candidates practice the exact difficulty patterns demanded by Tier-1 product and IT companies."
        },
        {
            icon: <FaBrain style={{ color: "#4f46e5", fontSize: "1.5rem" }} />,
            title: "GitHub Intelligence & Career Roadmaps",
            description: "Deep public repository analysis, deterministic 0–100 code scoring, and milestone-driven AI career roadmaps across 8 software specializations."
        },
        {
            icon: <FaShieldAlt style={{ color: "#4f46e5", fontSize: "1.5rem" }} />,
            title: "Privacy & Data Ownership",
            description: "Candidate resumes, code submissions, and interview audio recordings remain private. We do not sell candidate data or use proprietary user submissions for third-party model training."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Our Mission</span>
                <h1>Built for Engineering Excellence</h1>
                <p>
                    Samprepix is built to bridge the gap between academic computer science education and the rigorous technical standards of modern software engineering hiring.
                </p>
            </section>

            <main className="content-page-body">
                <section style={{ maxWidth: "800px", margin: "0 auto 48px auto", textAlign: "center" }}>
                    <h2 style={{ fontSize: "1.6rem", fontWeight: "700", marginBottom: "16px" }}>
                        Democratizing High-Caliber Interview Preparation
                    </h2>
                    <p style={{ fontSize: "1.05rem", lineHeight: "1.8", color: "var(--text-muted, #64748b)" }}>
                        Securing a top software engineering role demands more than memorizing syntax. It requires rapid algorithmic synthesis, structured verbal articulation under pressure, sound database query design, and resume alignment with applicant tracking systems.
                    </p>
                    <p style={{ fontSize: "1.05rem", lineHeight: "1.8", color: "var(--text-muted, #64748b)" }}>
                        We created Samprepix to provide every engineering student and self-taught developer with an institutional-grade platform to practice, benchmark, and succeed.
                    </p>
                </section>

                <section>
                    <h2 style={{ fontSize: "1.4rem", fontWeight: "700", marginBottom: "20px", textAlign: "center" }}>
                        Our Core Engineering Principles
                    </h2>
                    <div className="content-grid-2">
                        {pillars.map((p, idx) => (
                            <div key={idx} className="content-card">
                                <div style={{ marginBottom: "12px" }}>{p.icon}</div>
                                <h3>{p.title}</h3>
                                <p>{p.description}</p>
                            </div>
                        ))}
                    </div>
                </section>
            </main>

            <Footer />
        </div>
    );
}
