import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    FiGithub,
    FiSearch,
    FiCheckCircle,
    FiAlertTriangle,
    FiCopy,
    FiCheck,
    FiLock,
    FiExternalLink,
    FiStar,
    FiGitBranch,
    FiArrowRight,
    FiLayers,
    FiAward,
    FiUserCheck,
    FiUploadCloud,
    FiX
} from "react-icons/fi";
import { analyzeGithubProfile, getLatestGithubAnalysis, applyReadmeToGithub } from "../services/githubAnalyzerService";
import "../styles/githubAnalyzer.css";

export default function GithubAnalyzer() {
    const navigate = useNavigate();
    const [profileUrl, setProfileUrl] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [analysis, setAnalysis] = useState(null);
    const [copiedReadme, setCopiedReadme] = useState(false);
    const [readmeTab, setReadmeTab] = useState("recommended"); // 'recommended' | 'current'
    const [showApplyModal, setShowApplyModal] = useState(false);
    const [applying, setApplying] = useState(false);
    const [applySuccess, setApplySuccess] = useState("");
    const [applyError, setApplyError] = useState("");

    useEffect(() => {
        const fetchLatest = async () => {
            try {
                const latest = await getLatestGithubAnalysis();
                if (latest && latest.username) {
                    setAnalysis(latest);
                    setProfileUrl(latest.profileUrl || `https://github.com/${latest.username}`);
                }
            } catch (e) {
                // No prior analysis, normal state
            }
        };
        fetchLatest();
    }, []);

    const handleAnalyze = async (e) => {
        if (e) e.preventDefault();
        setError("");

        if (!profileUrl.trim()) {
            setError("Please enter a GitHub profile URL (e.g. https://github.com/username).");
            return;
        }

        setLoading(true);
        try {
            const data = await analyzeGithubProfile(profileUrl.trim());
            setAnalysis(data);
        } catch (err) {
            const msg = err?.response?.data?.error || err?.response?.data?.message || err?.message || "Failed to analyze GitHub profile.";
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    const handleCopyReadme = () => {
        if (!analysis?.recommendedReadme) return;
        navigator.clipboard.writeText(analysis.recommendedReadme);
        setCopiedReadme(true);
        setTimeout(() => setCopiedReadme(false), 2500);
    };

    const handleApplyReadme = async () => {
        if (!analysis || !analysis.recommendedReadme) return;
        setApplying(true);
        setApplyError("");
        setApplySuccess("");
        try {
            const res = await applyReadmeToGithub(analysis.username, analysis.recommendedReadme);
            setApplySuccess(res.message || "README successfully committed to your GitHub profile repository!");
            if (res.analysis) {
                setAnalysis(res.analysis);
            }
            setTimeout(() => {
                setShowApplyModal(false);
                setApplySuccess("");
            }, 2500);
        } catch (err) {
            const msg = err?.response?.data?.error || err?.response?.data?.message || err?.message || "Failed to commit README to GitHub.";
            setApplyError(msg);
        } finally {
            setApplying(false);
        }
    };

    return (
        <div className="github-analyzer-page">
            {/* HEADER */}
            <div className="ga-header">
                <div className="ga-header-badge">
                    <FiGithub /> Technical Recruiter Audit
                </div>
                <h1>
                    <FiGithub /> GitHub Profile Analyzer
                </h1>
                <p>
                    Recruiters evaluate candidate GitHub profiles within 30 seconds. Scan your public repositories,
                    profile README, and commit health to receive an objective 0–100 score and a tailored README.
                </p>
            </div>

            {/* URL INPUT CARD */}
            <div className="ga-input-card">
                <div className="ga-input-content">
                    <h2>Analyze Your GitHub Profile</h2>
                    <p>
                        Enter your public GitHub profile link. We inspect repository documentation, technical breadth,
                        and presentation signals using official public APIs.
                    </p>

                    <form className="ga-form" onSubmit={handleAnalyze}>
                        <div className="ga-input-wrapper">
                            <FiSearch className="ga-input-icon" size={18} />
                            <input
                                type="url"
                                className="ga-input"
                                placeholder="https://github.com/your-username"
                                value={profileUrl}
                                onChange={(e) => setProfileUrl(e.target.value)}
                                disabled={loading}
                            />
                        </div>

                        <button
                            type="submit"
                            className="ga-submit-btn"
                            disabled={loading}
                        >
                            {loading ? (
                                <>Analyzing Profile...</>
                            ) : (
                                <>
                                    <FiGithub size={18} /> Analyze Profile
                                </>
                            )}
                        </button>
                    </form>

                    {error && (
                        <div className="ga-error-banner">
                            <FiAlertTriangle /> {error}
                        </div>
                    )}
                </div>
            </div>

            {/* RESULTS VIEW */}
            {analysis && (
                <div className="ga-results">
                    {/* USER PROFILE CARD */}
                    <div className="ga-profile-banner">
                        <div className="ga-user-meta">
                            {analysis.avatarUrl ? (
                                <img src={analysis.avatarUrl} alt={analysis.username} className="ga-avatar" />
                            ) : (
                                <div className="ga-avatar" style={{ display: "flex", alignItems: "center", justifyContent: "center", background: "#6366f1", color: "#fff", fontSize: "24px", fontWeight: "bold" }}>
                                    {analysis.username?.charAt(0)?.toUpperCase()}
                                </div>
                            )}
                            <div className="ga-user-text">
                                <h2>
                                    {analysis.name || analysis.username}
                                    <a
                                        href={analysis.profileUrl}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="ga-gh-link"
                                    >
                                        @{analysis.username} <FiExternalLink size={14} />
                                    </a>
                                </h2>
                                <p className="ga-bio">{analysis.bio || "No public bio set on GitHub."}</p>
                            </div>
                        </div>

                        <div className="ga-stats-strip">
                            <div className="ga-stat-box">
                                <span className="ga-stat-num">{analysis.publicRepos}</span>
                                <span className="ga-stat-lbl">Public Repos</span>
                            </div>
                            <div className="ga-stat-box">
                                <span className="ga-stat-num">{analysis.followers}</span>
                                <span className="ga-stat-lbl">Followers</span>
                            </div>
                            <div className="ga-stat-box">
                                <span className="ga-stat-num">{analysis.following}</span>
                                <span className="ga-stat-lbl">Following</span>
                            </div>
                        </div>
                    </div>

                    {/* OVERALL SCORE & CATEGORIES */}
                    <div className="ga-score-card">
                        <div className="ga-score-gauge">
                            <div className="ga-score-circle">
                                <span className="ga-score-number">{analysis.overallScore}</span>
                                <span className="ga-score-max">/ 100</span>
                            </div>
                            <div className="ga-score-label">
                                {analysis.overallScore >= 80 ? "Excellent Profile" : analysis.overallScore >= 60 ? "Good Foundation" : "Needs Polish"}
                            </div>
                            <span className="ga-score-sub">Objective Technical Health</span>
                        </div>

                        <div className="ga-category-grid">
                            {analysis.categoryScores?.map((cat, i) => (
                                <div className="ga-cat-item" key={i}>
                                    <div className="ga-cat-top">
                                        <span>{cat.category}</span>
                                        <span className="ga-cat-score">{cat.score} / {cat.maxScore}</span>
                                    </div>
                                    <div className="ga-cat-bar-wrap">
                                        <div
                                            className="ga-cat-bar-fill"
                                            style={{ width: `${Math.round((cat.score / cat.maxScore) * 100)}%` }}
                                        />
                                    </div>
                                    <div className="ga-cat-feedback">{cat.feedback}</div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* AUDIT DETAILS: DEDUCTIONS & ACTIONABLE IMPROVEMENTS */}
                    <div className="ga-audit-grid">
                        <div className="ga-audit-card">
                            <h3 className="ga-audit-title danger">
                                <FiAlertTriangle /> What is reducing your score?
                            </h3>
                            <ul className="ga-list">
                                {analysis.deductions?.length > 0 ? (
                                    analysis.deductions.map((d, idx) => (
                                        <li className="ga-list-item deduction" key={idx}>
                                            <span>•</span> {d}
                                        </li>
                                    ))
                                ) : (
                                    <li className="ga-list-item" style={{ color: "#10b981" }}>
                                        No critical deductions found! Great job.
                                    </li>
                                )}
                            </ul>
                        </div>

                        <div className="ga-audit-card">
                            <h3 className="ga-audit-title success">
                                <FiCheckCircle /> How to improve
                            </h3>
                            <ul className="ga-list">
                                {analysis.improvements?.length > 0 ? (
                                    analysis.improvements.map((imp, idx) => (
                                        <li className="ga-list-item improvement" key={idx}>
                                            <FiArrowRight size={14} style={{ flexShrink: 0, marginTop: "3px" }} />
                                            <span>{imp}</span>
                                        </li>
                                    ))
                                ) : (
                                    <li className="ga-list-item">Your profile follows top recruiter practices.</li>
                                )}
                            </ul>
                        </div>
                    </div>

                    {/* RECRUITER VIEW SECTION */}
                    {analysis.premium ? (
                        <div className="ga-recruiter-card">
                            <div className="ga-recruiter-badge">
                                <FiUserCheck /> Recruiter Perspective
                            </div>
                            <h2>How Technical Recruiters Read Your Profile</h2>
                            <div className="ga-recruiter-verdict">
                                {analysis.recruiterView?.evaluationVerdict}
                            </div>
                            <p style={{ margin: "0 0 16px 0", color: "#64748b", lineHeight: 1.6 }}>
                                {analysis.recruiterView?.immediateImpressions}
                            </p>

                            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px", margin: "20px 0" }}>
                                <div style={{ padding: "14px", borderRadius: "12px", background: "rgba(16, 185, 129, 0.08)", border: "1px solid rgba(16, 185, 129, 0.2)" }}>
                                    <strong style={{ color: "#059669", display: "block", marginBottom: "8px", fontSize: "13px" }}>
                                        Demonstrated Skills
                                    </strong>
                                    <ul style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "#334155" }}>
                                        {analysis.recruiterView?.demonstratedSkills?.map((s, i) => (
                                            <li key={i}>{s}</li>
                                        ))}
                                    </ul>
                                </div>

                                <div style={{ padding: "14px", borderRadius: "12px", background: "rgba(239, 68, 68, 0.08)", border: "1px solid rgba(239, 68, 68, 0.2)" }}>
                                    <strong style={{ color: "#dc2626", display: "block", marginBottom: "8px", fontSize: "13px" }}>
                                        Missing Recruiter Signals
                                    </strong>
                                    <ul style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "#334155" }}>
                                        {analysis.recruiterView?.missingSignals?.map((s, i) => (
                                            <li key={i}>{s}</li>
                                        ))}
                                    </ul>
                                </div>
                            </div>

                            <div className="ga-recruiter-advice">
                                💡 <strong>Recruiter Takeaway:</strong> {analysis.recruiterView?.actionableAdvice}
                            </div>
                        </div>
                    ) : (
                        <div className="ga-locked-card">
                            <div className="ga-lock-badge">
                                <FiLock /> PREMIUM RECRUITER AUDIT
                            </div>
                            <h3>Unlock 30-Second Recruiter Perspective</h3>
                            <p>
                                See exactly how hiring managers evaluate your repositories, demonstrated skills, and
                                missing signals before applying.
                            </p>
                            <button className="ga-upgrade-btn" onClick={() => navigate("/pricing")}>
                                <FiAward /> Upgrade to Pro to Unlock
                            </button>
                        </div>
                    )}

                    {/* PROFILE README GENERATOR SECTION */}
                    {analysis.premium ? (
                        <div className="ga-readme-card">
                            <div className="ga-card-header-flex">
                                <div>
                                    <h3 style={{ margin: 0, fontSize: "20px", fontWeight: "700" }}>
                                        Tailored Profile README Generator
                                    </h3>
                                    <p style={{ margin: "4px 0 0 0", fontSize: "14px", color: "#64748b" }}>
                                        Based on your actual technical stack and verified Samprepix achievements.
                                    </p>
                                </div>

                                <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
                                    <div className="ga-readme-tab-track">
                                        <button
                                            className={`ga-readme-tab-btn ${readmeTab === "recommended" ? "active" : ""}`}
                                            onClick={() => setReadmeTab("recommended")}
                                        >
                                            Recommended
                                        </button>
                                        <button
                                            className={`ga-readme-tab-btn ${readmeTab === "current" ? "active" : ""}`}
                                            onClick={() => setReadmeTab("current")}
                                        >
                                            Current
                                        </button>
                                    </div>

                                    <button className="ga-copy-btn" onClick={handleCopyReadme}>
                                        {copiedReadme ? (
                                            <>
                                                <FiCheck style={{ color: "#10b981" }} /> Copied!
                                            </>
                                        ) : (
                                             <>
                                                 <FiCopy /> Copy Markdown
                                             </>
                                        )}
                                    </button>

                                    <button
                                        className="ga-apply-btn"
                                        onClick={() => {
                                            setApplyError("");
                                            setApplySuccess("");
                                            setShowApplyModal(true);
                                        }}
                                    >
                                        <FiUploadCloud /> Apply to GitHub Profile
                                    </button>
                                </div>
                            </div>

                            <pre className="ga-readme-box">
                                {readmeTab === "recommended"
                                    ? analysis.recommendedReadme
                                    : (analysis.currentReadme || "No custom profile README currently exists on GitHub.")}
                            </pre>
                        </div>
                    ) : (
                        <div className="ga-locked-card">
                            <div className="ga-lock-badge">
                                <FiLock /> PREMIUM FEATURE
                            </div>
                            <h3>Unlock Custom Profile README Generator</h3>
                            <p>
                                Get a copy-pasteable GitHub Profile README built strictly from your verified technical
                                stack, flagship repositories, and career targets.
                            </p>
                            <button className="ga-upgrade-btn" onClick={() => navigate("/pricing")}>
                                <FiAward /> Upgrade to Pro to Unlock
                            </button>
                        </div>
                    )}

                    {/* REPOSITORY IMPROVEMENT SUGGESTIONS */}
                    {analysis.repoAnalyses?.length > 0 && (
                        <div className="ga-readme-card">
                            <h3 style={{ margin: "0 0 16px 0", fontSize: "20px", fontWeight: "700" }}>
                                Repository Optimization Audit
                            </h3>
                            <div className="ga-repo-grid">
                                {analysis.repoAnalyses.map((repo, i) => (
                                    <div key={i} className="ga-repo-card">
                                        <div>
                                            <div className="ga-repo-card-header">
                                                <a
                                                    href={repo.htmlUrl}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="ga-repo-link"
                                                >
                                                    {repo.name} <FiExternalLink size={12} />
                                                </a>
                                                <span className="ga-repo-lang">
                                                    {repo.language}
                                                </span>
                                            </div>

                                            <p className="ga-repo-desc">
                                                {repo.description}
                                            </p>

                                            <div className="ga-repo-stats">
                                                <span><FiStar size={12} /> {repo.stars} stars</span>
                                                <span><FiGitBranch size={12} /> {repo.forks} forks</span>
                                            </div>
                                        </div>

                                        <div className="ga-repo-divider">
                                            <span className="ga-repo-recs-title">
                                                Recommendations:
                                            </span>
                                            <ul className="ga-repo-recs-list">
                                                {repo.recommendations?.map((r, ri) => (
                                                    <li key={ri}>{r}</li>
                                                ))}
                                            </ul>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* CONFIRMATION MODAL FOR APPLYING README */}
                    {showApplyModal && (
                        <div className="ga-modal-backdrop" onClick={() => !applying && setShowApplyModal(false)}>
                            <div className="ga-modal-card" onClick={(e) => e.stopPropagation()}>
                                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
                                    <h3 style={{ margin: 0, fontSize: "18px", fontWeight: "700", display: "flex", alignItems: "center", gap: "8px" }}>
                                        <FiGithub /> Apply README to GitHub Profile
                                    </h3>
                                    {!applying && (
                                        <button
                                            onClick={() => setShowApplyModal(false)}
                                            style={{ background: "none", border: "none", cursor: "pointer", color: "inherit", padding: "4px" }}
                                        >
                                            <FiX size={20} />
                                        </button>
                                    )}
                                </div>
                                <p style={{ fontSize: "14px", lineHeight: "1.6", color: "var(--text-secondary, #64748b)", margin: "0 0 16px 0" }}>
                                    This will automatically commit the generated README.md to your personal GitHub repository <strong>{analysis.username}/{analysis.username}</strong> on the main branch using your connected GitHub account.
                                </p>
                                {applyError && (
                                    <div style={{ padding: "12px", background: "rgba(239, 68, 68, 0.1)", border: "1px solid rgba(239, 68, 68, 0.3)", borderRadius: "8px", color: "#ef4444", fontSize: "13px", marginBottom: "16px" }}>
                                        {applyError}
                                    </div>
                                )}
                                {applySuccess && (
                                    <div style={{ padding: "12px", background: "rgba(16, 185, 129, 0.1)", border: "1px solid rgba(16, 185, 129, 0.3)", borderRadius: "8px", color: "#10b981", fontSize: "13px", marginBottom: "16px" }}>
                                        {applySuccess}
                                    </div>
                                )}
                                <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", marginTop: "20px" }}>
                                    <button
                                        onClick={() => setShowApplyModal(false)}
                                        disabled={applying}
                                        style={{ padding: "10px 18px", borderRadius: "10px", border: "1px solid #cbd5e1", background: "transparent", color: "inherit", cursor: "pointer", fontWeight: "600" }}
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        onClick={handleApplyReadme}
                                        disabled={applying}
                                        className="ga-apply-btn"
                                    >
                                        {applying ? "Committing..." : "Confirm & Commit"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
