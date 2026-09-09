import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaBookOpen, FaClock, FaTag } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Blog() {
    useEffect(() => {
        updatePageSEO({
            title: "Engineering Blog & Placement Insights | Samprepix",
            description: "Technical guides, algorithmic problem-solving strategies, ATS resume best practices, and interview preparation advice from engineers.",
            canonicalPath: "/blog"
        });
        trackPageView("/blog", "Blog | Samprepix");
    }, []);

    const articles = [
        {
            title: "How Applicant Tracking Systems (ATS) Parse Engineering Resumes in 2026",
            category: "Resume & Career",
            readTime: "6 min read",
            date: "September 2026",
            summary: "Understand how modern parsing engines extract skills, evaluate single-column layouts, score role keyword density, and compute technical fit before a human recruiter reads your PDF."
        },
        {
            title: "Mastering Sliding Window & Two Pointers in Algorithmic Interviews",
            category: "Algorithms & DSA",
            readTime: "9 min read",
            date: "August 2026",
            summary: "A systematic approach to identifying dynamic vs fixed-size sliding window problems with step-by-step state transition formulas and Python/Java implementations."
        },
        {
            title: "Quantitative Aptitude for Campus Placements: Probability & Permutations Shortcuts",
            category: "Aptitude Prep",
            readTime: "7 min read",
            date: "August 2026",
            summary: "High-yield mental math strategies and counting principles frequently tested in initial screening rounds of product and service-based software firms."
        },
        {
            title: "Verbal Articulation in Technical Interviews: The STAR-T Method",
            category: "Interview Skills",
            readTime: "5 min read",
            date: "July 2026",
            summary: "How to structure answers to difficult behavioral and technical questions under pressure using Situation, Task, Action, Result, and Technical Takeaway."
        },
        {
            title: "SQL Query Optimization: Avoiding Common Pitfalls in Database Rounds",
            category: "Database & SQL",
            readTime: "8 min read",
            date: "June 2026",
            summary: "Why window functions (ROW_NUMBER, DENSE_RANK), correlated subqueries, and improper indexing slow down queries, and how to write clean, canonical MySQL solutions."
        },
        {
            title: "From 0 to 500 Problems: Building an Unshakeable Daily Coding Habit",
            category: "Preparation Strategy",
            readTime: "5 min read",
            date: "May 2026",
            summary: "Actionable roadmap on structuring your preparation across foundational data structures, medium difficulty patterns, and timed contest environments."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Engineering Articles</span>
                <h1>Placement & Engineering Blog</h1>
                <p>
                    Technical deep-dives, algorithmic problem strategies, resume optimization guides, and placement interview breakdowns.
                </p>
            </section>

            <main className="content-page-body">
                <div className="content-grid-3">
                    {articles.map((art, idx) => (
                        <article key={idx} className="content-card" style={{ display: "flex", flexDirection: "column" }}>
                            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "12px" }}>
                                <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", fontSize: "0.78rem", fontWeight: "700", color: "#4f46e5" }}>
                                    <FaTag style={{ fontSize: "0.7rem" }} /> {art.category}
                                </span>
                                <span style={{ display: "inline-flex", alignItems: "center", gap: "4px", fontSize: "0.78rem", color: "var(--text-muted, #64748b)" }}>
                                    <FaClock style={{ fontSize: "0.7rem" }} /> {art.readTime}
                                </span>
                            </div>

                            <h2 style={{ fontSize: "1.1rem", fontWeight: "700", lineHeight: "1.4", margin: "0 0 10px 0" }}>
                                {art.title}
                            </h2>

                            <p style={{ fontSize: "0.9rem", lineHeight: "1.6", color: "var(--text-muted, #64748b)", flex: 1, margin: 0 }}>
                                {art.summary}
                            </p>

                            <div style={{ marginTop: "18px", paddingTop: "14px", borderTop: "1px solid var(--border-color, #e2e8f0)", fontSize: "0.82rem", color: "var(--text-muted, #64748b)" }}>
                                Published: {art.date}
                            </div>
                        </article>
                    ))}
                </div>
            </main>

            <Footer />
        </div>
    );
}
