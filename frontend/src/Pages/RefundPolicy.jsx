import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import {
    FaUndoAlt,
    FaClock,
    FaShieldAlt,
    FaCreditCard,
    FaFileInvoiceDollar,
    FaHeadset,
    FaArrowLeft
} from "react-icons/fa";
import "../styles/contentPages.css";

export default function RefundPolicy() {
    const navigate = useNavigate();

    useEffect(() => {
        updatePageSEO({
            title: "Refund & Cancellation Policy | SamPrepIX",
            description: "Read the official Refund & Cancellation Policy for SamPrepIX (Operated by SAMIR KUMAR). Clear policies on subscription duration, cancellation terms, and refund processing in INR.",
            canonicalPath: "/refund-policy"
        });
        trackPageView("/refund-policy", "Refund & Cancellation Policy | SamPrepIX");
    }, []);

    const policyCards = [
        {
            icon: <FaFileInvoiceDollar style={{ color: "#4f46e5", fontSize: "1.4rem" }} />,
            title: "1. Digital Services & Subscriptions",
            description: "SamPrepIX provides cloud-based digital placement preparation software, including AI mock interviews, ATS resume analysis, coding compilers, and aptitude question banks. We offer monthly paid plans — Pro (₹399/Month) and Elite (₹799/Month) — with all charges billed transparently in Indian Rupees (INR)."
        },
        {
            icon: <FaUndoAlt style={{ color: "#2563eb", fontSize: "1.4rem" }} />,
            title: "2. Cancellation Policy",
            description: "Paid subscriptions are one-time payments valid for a full 30-day billing cycle. Automatic recurring card debits are not active. You have full freedom to discontinue or not renew at the end of your term. If you request voluntary plan cancellation midway through a cycle, your premium benefits remain available until your active 30-day term concludes."
        },
        {
            icon: <FaShieldAlt style={{ color: "#7c3aed", fontSize: "1.4rem" }} />,
            title: "3. Refund Eligibility Criteria",
            description: "Due to the immediate provisioning of AI computing resources, compiler sandboxes, and premium curriculum upon payment, regular utilized plans are non-refundable. However, full refunds are issued under the following verified conditions:\n• Duplicate Charges: If you were charged more than once for a single order due to a payment gateway latency or technical glitch.\n• Unresolved Platform Defect: If a technical system fault on our platform completely prevents access to your paid tier features and cannot be resolved within 48 hours of reporting."
        },
        {
            icon: <FaClock style={{ color: "#059669", fontSize: "1.4rem" }} />,
            title: "4. Refund Processing Timeframe",
            description: "All authorized refunds are processed through our licensed payment gateway partner (Cashfree Payments). Once initiated, standard banking refunds typically settle within 5 to 7 business days directly to your original payment instrument (UPI ID, Debit Card, Credit Card, or Net Banking account)."
        },
        {
            icon: <FaCreditCard style={{ color: "#e11d48", fontSize: "1.4rem" }} />,
            title: "5. Chargebacks & Disputes",
            description: "If you notice an unexpected transaction or billing discrepancy, we strongly encourage contacting our support desk before initiating a bank chargeback. We resolve verified billing errors directly and promptly to ensure your account standing remains in good order."
        },
        {
            icon: <FaHeadset style={{ color: "#d97706", fontSize: "1.4rem" }} />,
            title: "6. How to Submit a Refund Request",
            description: "To request a refund or raise a cancellation inquiry, email our support team at samirprajapat5@gmail.com with your registered email, Cashfree Order ID, payment date, and a brief description of the issue. Requests are acknowledged within 24 business hours."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Compliance & Transparency</span>
                <h1>Refund & Cancellation Policy</h1>
                <p>
                    Clear, honest, and verifiable terms governing subscriptions, cancellations, and payment refunds for SamPrepIX (Operated by SAMIR KUMAR).
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
                        to="/contact"
                        className="content-page-nav-btn"
                    >
                        Contact Support
                    </Link>
                </div>
            </section>

            <main className="content-page-body" style={{ maxWidth: "920px", margin: "0 auto", padding: "0 24px 60px" }}>
                {/* BUSINESS ENTITY NOTICE */}
                <div
                    className="content-card"
                    style={{
                        marginBottom: "32px",
                        padding: "24px 28px",
                        background: "rgba(99, 102, 241, 0.05)",
                        border: "1px solid rgba(99, 102, 241, 0.18)",
                        borderRadius: "14px"
                    }}
                >
                    <h3 style={{ margin: "0 0 8px 0", fontSize: "1.05rem", fontWeight: "700", color: "#4f46e5" }}>
                        Merchant & Operator Identification
                    </h3>
                    <p style={{ margin: 0, fontSize: "0.92rem", lineHeight: "1.7", color: "var(--text-muted, #475569)" }}>
                        This website and platform (<strong>SamPrepIX</strong>) is operated by <strong>SAMIR KUMAR</strong>. All commercial payment transactions, digital subscription checkouts, and customer refunds are processed under this registered merchant entity in compliance with Cashfree Payment Gateway guidelines.
                    </p>
                </div>

                {/* POLICY GRID */}
                <div className="content-grid-2">
                    {policyCards.map((card, idx) => (
                        <div key={idx} className="content-card" style={{ padding: "28px" }}>
                            <div style={{ marginBottom: "14px" }}>{card.icon}</div>
                            <h2 style={{ fontSize: "1.1rem", fontWeight: "700", margin: "0 0 10px 0" }}>
                                {card.title}
                            </h2>
                            <p style={{ margin: 0, lineHeight: 1.65, fontSize: "0.92rem", color: "var(--text-muted, #64748b)", whiteSpace: "pre-line" }}>
                                {card.description}
                            </p>
                        </div>
                    ))}
                </div>

                {/* CONTACT HELPDESK BANNER */}
                <div
                    className="content-card"
                    style={{
                        marginTop: "32px",
                        textAlign: "center",
                        padding: "32px 24px",
                        border: "1px solid var(--border-color, #e2e8f0)",
                        borderRadius: "16px"
                    }}
                >
                    <h3 style={{ margin: "0 0 8px 0", fontSize: "1.2rem", fontWeight: 700 }}>
                        Need Assistance With a Payment or Refund?
                    </h3>
                    <p style={{ maxWidth: "580px", margin: "0 auto 20px", fontSize: "0.93rem", lineHeight: 1.65, color: "var(--text-muted, #64748b)" }}>
                        For any queries regarding transaction status, bank clearance timelines, or refund verification, reach out directly to our helpdesk:
                    </p>
                    <div style={{ display: "flex", gap: "14px", justifyContent: "center", flexWrap: "wrap", alignItems: "center" }}>
                        <a
                            href="mailto:samirprajapat5@gmail.com"
                            style={{
                                padding: "10px 24px",
                                borderRadius: "10px",
                                background: "#4f46e5",
                                color: "#ffffff",
                                textDecoration: "none",
                                fontWeight: "600",
                                fontSize: "0.92rem"
                            }}
                        >
                            Email: samirprajapat5@gmail.com
                        </a>
                        <Link
                            to="/contact"
                            className="content-page-nav-btn"
                            style={{ padding: "10px 20px" }}
                        >
                            Visit Contact Page
                        </Link>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
}
