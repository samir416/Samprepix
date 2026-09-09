import { useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import "../styles/contentPages.css";

export default function Changelog() {
    useEffect(() => {
        updatePageSEO({
            title: "Changelog & Product Releases | Samprepix",
            description: "Track official product releases, feature updates, improvements, and fixes across the Samprepix engineering platform.",
            canonicalPath: "/changelog"
        });
        trackPageView("/changelog", "Changelog | Samprepix");
    }, []);

    const releases = [
        {
            version: "v2.4.0",
            date: "September 2026",
            tag: "Released",
            tagClass: "released",
            summary: "Enhanced Coding Arena Multi-Language Engine & Settings Persistence",
            highlights: [
                "Deterministic language preference hierarchy: saved user preferences strictly govern default editor language across sessions.",
                "Automatic SQL/MySQL isolation when navigating between Algorithms (DSA) and Database problems.",
                "Piston multi-language execution engine supporting 50+ programming runtimes and MySQL sandboxes.",
                "Transactional Settings modal with decoupled draft states and instant UI synchronization."
            ]
        },
        {
            version: "v2.3.0",
            date: "August 2026",
            tag: "Released",
            tagClass: "released",
            summary: "Comprehensive Aptitude Assessment Bank (22,060 Questions)",
            highlights: [
                "Full syllabus coverage across Quantitative Aptitude, Logical Reasoning, Verbal Ability, and Technical Aptitude.",
                "Timed assessment engine with question navigation palette, review modes, and detailed solution explanations.",
                "Historical attempt tracking with speed metrics, accuracy percentages, and topic-wise breakdown."
            ]
        },
        {
            version: "v2.2.0",
            date: "July 2026",
            tag: "Released",
            tagClass: "released",
            summary: "Coding Arena Problem Bank Expansion (5,050 Problems)",
            highlights: [
                "Integrated massive problem catalog spanning Easy, Medium, and Hard algorithmic challenges.",
                "Structured test cases with public sample checks and secure hidden test validation.",
                "Multi-tiered AI hints (Concept, Approach, Solution) with anti-cheat cooldowns."
            ]
        },
        {
            version: "v2.1.0",
            date: "June 2026",
            tag: "Released",
            tagClass: "released",
            summary: "Automated GitHub Solution Synchronization",
            highlights: [
                "Direct GitHub repository integration for automated solution syncing upon test pass.",
                "Folder-organized solution files with standard extension mappings and duplicate commit prevention.",
                "One-click sync retry and status verification directly within the Coding Arena topbar."
            ]
        },
        {
            version: "v2.0.0",
            date: "May 2026",
            tag: "Released",
            tagClass: "released",
            summary: "AI Mock Interviews & ATS Resume Analyzer Launch",
            highlights: [
                "Speech-enabled mock technical and HR interviews with speech recognition and natural voice synthesis.",
                "Automated rubric evaluation measuring technical depth, behavioral pacing, and structured problem-solving.",
                "ATS resume parser extracting keywords, structural density, and role-fit scores."
            ]
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Product Updates</span>
                <h1>Product Changelog</h1>
                <p>
                    Continuous improvements, new features, runtime expansions, and platform updates delivered to engineering candidates.
                </p>
            </section>

            <main className="content-page-body">
                <div className="timeline-list">
                    {releases.map((rel) => (
                        <article key={rel.version} className="timeline-item">
                            <div className="timeline-header">
                                <span className="timeline-version">{rel.version}</span>
                                <span className="timeline-date">{rel.date}</span>
                                <span className={`timeline-tag ${rel.tagClass}`}>{rel.tag}</span>
                            </div>
                            <div className="timeline-content">
                                <h3 style={{ margin: "0 0 8px 0", fontSize: "1.05rem", fontWeight: "700" }}>
                                    {rel.summary}
                                </h3>
                                <ul>
                                    {rel.highlights.map((item, idx) => (
                                        <li key={idx}>{item}</li>
                                    ))}
                                </ul>
                            </div>
                        </article>
                    ))}
                </div>
            </main>

            <Footer />
        </div>
    );
}
