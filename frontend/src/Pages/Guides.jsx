import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaCompass, FaCheckSquare, FaLayerGroup } from "react-icons/fa";
import "../styles/contentPages.css";

export default function Guides() {
    useEffect(() => {
        updatePageSEO({
            title: "Placement Preparation Guides | Samprepix",
            description: "Actionable, multi-week study guides, curriculum blueprints, and preparation checklists for software engineering interviews.",
            canonicalPath: "/guides"
        });
        trackPageView("/guides", "Guides | Samprepix");
    }, []);

    const guides = [
        {
            title: "The 90-Day Campus Placement Sprint",
            level: "Foundational to Advanced",
            duration: "12 Weeks",
            summary: "A rigorous, structured day-by-day blueprint covering Arrays, Strings, Trees, Graphs, Dynamic Programming, and SQL.",
            steps: [
                "Weeks 1–3: Core Linear Data Structures (Arrays, Linked Lists, Stacks, Queues, Two Pointers).",
                "Weeks 4–6: Non-Linear Structures & Recursion (Binary Trees, BST, Graphs, BFS/DFS traversal).",
                "Weeks 7–9: Optimization & Dynamic Programming (1D/2D DP, Greedy, Binary Search variations).",
                "Weeks 10–12: Database SQL Sandboxes, Full-Length Aptitude Mocks, and AI Voice Interview Simulations."
            ]
        },
        {
            title: "Aptitude Assessment Mastery Guide",
            level: "Screening Round Preparation",
            duration: "4 Weeks",
            summary: "Targeted curriculum designed to crack the initial high-elimination aptitude rounds of campus hiring.",
            steps: [
                "Module 1: Quantitative Essentials — Ratios, Percentages, Profit/Loss, Time & Work formulas.",
                "Module 2: Logical Reasoning — Syllogisms, Blood Relations, Coding-Decoding, Seating Arrangements.",
                "Module 3: Verbal Ability — Sentence Correction, Reading Comprehension, Contextual Vocabulary.",
                "Module 4: Full-Scale Speed Tests — Practicing under 60-second-per-question constraints."
            ]
        },
        {
            title: "Engineering Portfolio & GitHub Sync Setup",
            level: "Practical Verification",
            duration: "1 Week",
            summary: "How to showcase genuine problem-solving ability to recruiters through authenticated automated commits.",
            steps: [
                "Step 1: Connect your public GitHub account via OAuth in Samprepix settings.",
                "Step 2: Designate a dedicated solutions repository (e.g. `Samprepix-Coding-Practice`).",
                "Step 3: Solve problems in the Coding Arena; passing solutions are auto-committed with full test verification.",
                "Step 4: Highlight your active problem-solving streak and categorized solutions directly on your technical resume."
            ]
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Structured Curricula</span>
                <h1>Placement Preparation Guides</h1>
                <p>
                    Comprehensive, battle-tested preparation roadmaps and checklists designed to take you from foundational syntax to interview confidence.
                </p>
            </section>

            <main className="content-page-body">
                <div style={{ display: "flex", flexDirection: "column", gap: "28px" }}>
                    {guides.map((g, idx) => (
                        <div key={idx} className="content-card">
                            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "12px", flexWrap: "wrap", gap: "8px" }}>
                                <span style={{ display: "inline-flex", alignItems: "center", gap: "6px", color: "#4f46e5", fontWeight: "700", fontSize: "0.85rem" }}>
                                    <FaCompass /> {g.level}
                                </span>
                                <span style={{ padding: "3px 10px", borderRadius: "999px", background: "rgba(99, 102, 241, 0.08)", color: "#4f46e5", fontWeight: "700", fontSize: "0.8rem" }}>
                                    {g.duration}
                                </span>
                            </div>

                            <h2 style={{ fontSize: "1.3rem", fontWeight: "700", margin: "0 0 10px 0" }}>{g.title}</h2>
                            <p style={{ color: "var(--text-muted, #64748b)", lineHeight: "1.6", marginBottom: "16px" }}>{g.summary}</p>

                            <div style={{ background: "rgba(99, 102, 241, 0.03)", padding: "16px 20px", borderRadius: "12px", border: "1px solid rgba(99, 102, 241, 0.1)" }}>
                                <h3 style={{ fontSize: "0.95rem", fontWeight: "700", margin: "0 0 10px 0", display: "flex", alignItems: "center", gap: "6px" }}>
                                    <FaCheckSquare style={{ color: "#4f46e5" }} /> Core Curriculum Breakdown
                                </h3>
                                <ul style={{ margin: 0, paddingLeft: "20px", fontSize: "0.9rem", lineHeight: "1.7", color: "var(--text-muted, #64748b)" }}>
                                    {g.steps.map((s, i) => (
                                        <li key={i}>{s}</li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    ))}
                </div>
            </main>

            <Footer />
        </div>
    );
}
