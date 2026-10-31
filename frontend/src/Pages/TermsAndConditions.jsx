import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import { FaArrowLeft } from "react-icons/fa";
import "../styles/contentPages.css";

export default function TermsAndConditions() {
    const navigate = useNavigate();

    useEffect(() => {
        updatePageSEO({
            title: "Terms & Conditions | SamPrepIX",
            description: "Review the official Terms & Conditions for SamPrepIX (Operated by SAMIR KUMAR). Platform rules, digital services, payment terms in INR, and intellectual property guidelines.",
            canonicalPath: "/terms-and-conditions"
        });
        trackPageView("/terms-and-conditions", "Terms & Conditions | SamPrepIX");
    }, []);

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Legal Terms</span>
                <h1>Terms & Conditions</h1>
                <p>
                    Please review these terms governing the access, paid digital services, and curriculum tools provided by SamPrepIX (Operated by SAMIR KUMAR).
                </p>
                <div style={{ marginTop: "18px", display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
                    <button
                        type="button"
                        onClick={() => navigate(-1)}
                        className="content-page-nav-btn"
                    >
                        <FaArrowLeft /> Back
                    </button>
                    <Link
                        to="/pricing"
                        className="content-page-nav-btn"
                    >
                        View Pricing Plans (INR)
                    </Link>
                    <Link
                        to="/refund-policy"
                        className="content-page-nav-btn"
                    >
                        Refund & Cancellation Policy
                    </Link>
                </div>
            </section>

            <main className="content-page-body" style={{ maxWidth: "880px", margin: "0 auto", padding: "0 24px 60px" }}>
                {/* OPERATOR NOTICE */}
                <div
                    className="content-card"
                    style={{
                        marginBottom: "28px",
                        padding: "24px 28px",
                        background: "rgba(99, 102, 241, 0.05)",
                        border: "1px solid rgba(99, 102, 241, 0.18)",
                        borderRadius: "14px"
                    }}
                >
                    <h3 style={{ margin: "0 0 8px 0", fontSize: "1.05rem", fontWeight: "700", color: "#4f46e5" }}>
                        Operator & Legal Identification
                    </h3>
                    <p style={{ margin: 0, fontSize: "0.92rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                        <strong>SamPrepIX</strong> is an AI-powered interview practice, algorithmic coding, and placement preparation platform operated by <strong>SAMIR KUMAR</strong>. By using this website, creating an account, or purchasing digital services, you enter into a legally binding agreement under these terms with the registered operator.
                    </p>
                </div>

                <article className="content-card" style={{ padding: "36px" }}>
                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>1. Acceptance of Terms</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            By registering an account, accessing educational modules, or initiating a transaction on SamPrepIX, you confirm that you are at least 18 years of age (or have parental/guardian consent) and agree to be bound by these Terms & Conditions and our associated policies. If you do not accept these terms, you must discontinue platform use.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>2. Products & Digital Services Description</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            SamPrepIX offers cloud-based placement preparation software tailored for engineering and computer science students. Services include:
                        </p>
                        <ul style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)", paddingLeft: "20px" }}>
                            <li>AI-driven technical and behavioral mock interview simulations with performance grading.</li>
                            <li>Interactive Coding Arena with 5,050+ DSA and SQL problems across 50+ execution environments.</li>
                            <li>Aptitude assessment bank containing 22,060+ practice questions and detailed explanations.</li>
                            <li>ATS Resume Analyzer providing scoring, keyword matching, and structural recommendations.</li>
                            <li>GitHub Profile Analyzer and personalized AI career roadmap tracking.</li>
                        </ul>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>3. Pricing & Payment Terms (INR)</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            We offer both free tier access and premium paid subscriptions:
                        </p>
                        <ul style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)", paddingLeft: "20px" }}>
                            <li><strong>Starter Plan</strong>: ₹0 (Free forever for essential preparation).</li>
                            <li><strong>Pro Plan</strong>: ₹399 / Month (Full access to AI interviews, resume analytics, and coding hints).</li>
                            <li><strong>Elite Plan</strong>: ₹799 / Month (Priority computing quotas, company tracks, and advanced tooling).</li>
                        </ul>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            All subscription fees are quoted and charged exclusively in <strong>Indian Rupees (INR)</strong>. Commercial checkout sessions are securely handled by authorized payment aggregator <strong>Cashfree Payments</strong>. We do not store or handle raw debit/credit card numbers on our servers.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>4. Cancellation & Refund Policy</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            All subscription payments are one-time charges for 30 days of digital access. Subscriptions can be cancelled at any time prior to cycle renewal. Details regarding refund eligibility, duplicate transaction reimbursements, and 5 to 7 day banking settlement turnaround times are outlined in our dedicated <Link to="/refund-policy" style={{ color: "#4f46e5", fontWeight: "600" }}>Refund & Cancellation Policy</Link>.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>5. User Accounts & Credentials</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            You are responsible for maintaining the confidentiality of your account password and security tokens. You agree not to share, license, or resell your account to third parties. We reserve the right to terminate accounts that engage in fraudulent activity or violate our acceptable use standards.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>6. Intellectual Property Rights</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            All software, user interface designs, logos, problem sets, editorial explanations, and platform content are the exclusive property of SamPrepIX / SAMIR KUMAR. You retain full ownership and intellectual rights to the original code, resume documents, and solutions you independently author and upload.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>7. Acceptable Use Policy</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            You agree not to attempt to breach compiler sandbox security, perform automated scraping of problem banks, introduce malicious software, disrupt platform servers, or exploit platform endpoints. Violations may result in immediate suspension and referral to appropriate legal authorities.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>8. Educational Disclaimer & Limitation of Liability</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            SamPrepIX is an interactive training and self-assessment utility. While our curriculum closely mirrors current technology hiring assessments, we do NOT guarantee job offers, campus selection, or specific interview outcomes with third-party employers. The platform and its contents are provided on an "as-is" and "as-available" basis without warranties of any kind.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>9. Governing Law & Dispute Resolution</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            These Terms & Conditions shall be governed by and interpreted under the laws of India. Any disputes arising out of or related to these terms shall be subject to the exclusive jurisdiction of the competent courts in India.
                        </p>
                    </div>

                    <div className="legal-section-block">
                        <h2 style={{ fontSize: "1.25rem", fontWeight: "700", marginBottom: "10px" }}>10. Contact Information</h2>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            For questions regarding these Terms & Conditions, please contact us:
                        </p>
                        <p style={{ fontSize: "0.93rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                            <strong>Platform</strong>: SamPrepIX<br />
                            <strong>Legal Operator</strong>: SAMIR KUMAR<br />
                            <strong>Official Email</strong>: <a href="mailto:samirprajapat5@gmail.com" style={{ color: "#4f46e5" }}>samirprajapat5@gmail.com</a>
                        </p>
                    </div>
                </article>
            </main>

            <Footer />
        </div>
    );
}
