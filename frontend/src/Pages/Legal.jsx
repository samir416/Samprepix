import { useState, useEffect } from "react";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import "../styles/contentPages.css";

export default function Legal() {
    const [activeTab, setActiveTab] = useState("terms");

    useEffect(() => {
        updatePageSEO({
            title: "Legal Terms & Privacy Policy | Samprepix",
            description: "Review the Samprepix Terms of Service, Privacy Policy, candidate data protection standards, and acceptable use guidelines.",
            canonicalPath: "/legal"
        });
        trackPageView("/legal", "Legal & Privacy | Samprepix");
    }, []);

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Compliance & Trust</span>
                <h1>Terms & Privacy Policy</h1>
                <p>
                    Clear, transparent commitments detailing how Samprepix safeguards candidate data, maintains platform integrity, and respects user ownership.
                </p>

                <div
                    style={{
                        display: "inline-flex",
                        gap: "8px",
                        marginTop: "24px",
                        padding: "4px",
                        background: "rgba(99, 102, 241, 0.08)",
                        borderRadius: "999px",
                        border: "1px solid rgba(99, 102, 241, 0.18)"
                    }}
                    role="tablist"
                    aria-label="Legal Sections"
                >
                    <button
                        type="button"
                        onClick={() => setActiveTab("terms")}
                        style={{
                            padding: "8px 18px",
                            borderRadius: "999px",
                            border: "none",
                            cursor: "pointer",
                            fontWeight: "700",
                            fontSize: "0.88rem",
                            transition: "all 0.2s ease",
                            background: activeTab === "terms" ? "#4f46e5" : "transparent",
                            color: activeTab === "terms" ? "#ffffff" : "var(--text-muted, #64748b)"
                        }}
                        role="tab"
                        aria-selected={activeTab === "terms"}
                    >
                        Terms of Service
                    </button>
                    <button
                        type="button"
                        onClick={() => setActiveTab("privacy")}
                        style={{
                            padding: "8px 18px",
                            borderRadius: "999px",
                            border: "none",
                            cursor: "pointer",
                            fontWeight: "700",
                            fontSize: "0.88rem",
                            transition: "all 0.2s ease",
                            background: activeTab === "privacy" ? "#4f46e5" : "transparent",
                            color: activeTab === "privacy" ? "#ffffff" : "var(--text-muted, #64748b)"
                        }}
                        role="tab"
                        aria-selected={activeTab === "privacy"}
                    >
                        Privacy Policy
                    </button>
                    <button
                        type="button"
                        onClick={() => setActiveTab("acceptable-use")}
                        style={{
                            padding: "8px 18px",
                            borderRadius: "999px",
                            border: "none",
                            cursor: "pointer",
                            fontWeight: "700",
                            fontSize: "0.88rem",
                            transition: "all 0.2s ease",
                            background: activeTab === "acceptable-use" ? "#4f46e5" : "transparent",
                            color: activeTab === "acceptable-use" ? "#ffffff" : "var(--text-muted, #64748b)"
                        }}
                        role="tab"
                        aria-selected={activeTab === "acceptable-use"}
                    >
                        Acceptable Use
                    </button>
                </div>
            </section>

            <main className="content-page-body" style={{ maxWidth: "880px" }}>
                {activeTab === "terms" && (
                    <article className="content-card" style={{ padding: "36px" }}>
                        <div className="legal-section-block">
                            <h2>1. Acceptance of Terms</h2>
                            <p>
                                By accessing or using the Samprepix platform, you agree to comply with and be bound by these Terms of Service. If you do not agree with these terms, you must discontinue platform use immediately.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>2. User Accounts & Security</h2>
                            <p>
                                Candidates must provide accurate profile details upon registration. You are responsible for maintaining the confidentiality of your authentication credentials. Samprepix reserves the right to suspend accounts engaged in abusive automated traffic, scraping, or credential sharing.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>3. Intellectual Property & Code Ownership</h2>
                            <p>
                                You retain all rights and intellectual property ownership over the original source code, algorithms, and resume text you write or submit on Samprepix. Samprepix retains ownership over problem descriptions, editorial test cases, platform software, and rubric criteria.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>4. Subscription & Pricing Tiers</h2>
                            <p>
                                Platform tier features, quotas, and pricing are displayed on our public Pricing page. Paid plan capabilities are subject to active account status and fair resource utilization limits.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>5. Limitation of Liability</h2>
                            <p>
                                Samprepix provides algorithmic evaluation and interview simulations for educational and placement preparation purposes. We do not guarantee employment, offer letters, or hiring outcomes with any third-party employer.
                            </p>
                        </div>
                    </article>
                )}

                {activeTab === "privacy" && (
                    <article className="content-card" style={{ padding: "36px" }}>
                        <div className="legal-section-block">
                            <h2>1. Information We Collect</h2>
                            <p>
                                We collect information you provide directly upon registration (name, email), profile details (education, preferred programming languages, skills), and platform progress metrics (solved problems, aptitude attempts, interview performance ratings).
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>2. Candidate Resume & Audio Privacy</h2>
                            <p>
                                Resumes submitted to our ATS Resume Analyzer are processed strictly to extract relevant skills, structural keywords, and generate candidate feedback. We do NOT sell, license, or publish your resume or interview audio transcripts to recruiters or advertisers without your explicit authorization.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>3. Third-Party GitHub Integration</h2>
                            <p>
                                When you choose to connect GitHub to sync your coding solutions, Samprepix uses authorized OAuth scopes strictly to push your passing problem solutions to your designated repository. We never read or modify any other repositories in your GitHub account.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>4. Data Retention & Deletion</h2>
                            <p>
                                Candidates may request complete account data deletion at any time by contacting our support team at samirprajapat5@gmail.com.
                            </p>
                        </div>
                    </article>
                )}

                {activeTab === "acceptable-use" && (
                    <article className="content-card" style={{ padding: "36px" }}>
                        <div className="legal-section-block">
                            <h2>1. Sandbox Integrity & Safety</h2>
                            <p>
                                The Coding Arena provides live compiler and runtime environments. Code submitted to the sandbox must be intended solely for algorithmic problem-solving. Any attempts to escape the runtime sandbox, consume excessive memory/CPU maliciously, perform network calls, or attack platform infrastructure will result in permanent ban and potential legal action.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>2. Academic Honesty in College Placement Tests</h2>
                            <p>
                                When participating in campus placement drives or timed assessments, candidates agree to submit their own work without unauthorized third-party collaboration or automated script assistance.
                            </p>
                        </div>

                        <div className="legal-section-block">
                            <h2>3. Rate Limits & Automation</h2>
                            <p>
                                Automated scraping or reverse-engineering of the 5,050 problem dataset, test cases, or aptitude questions is strictly prohibited.
                            </p>
                        </div>
                    </article>
                )}
            </main>

            <Footer />
        </div>
    );
}
