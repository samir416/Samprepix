import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView } from "../utils/analytics";
import {
    FaShieldAlt,
    FaUndoAlt,
    FaCalendarCheck,
    FaCreditCard,
    FaUserCheck,
    FaExclamationCircle,
    FaArrowLeft,
    FaClock
} from "react-icons/fa";
import "../styles/contentPages.css";

export default function SubscriptionPolicy() {
    const navigate = useNavigate();

    useEffect(() => {
        updatePageSEO({
            title: "Subscription & Refund Policy | SamPrepIX",
            description: "Detailed policies on subscription lifecycle, user cancellations, platform revocations, refund processing timeframes, and renewal rules.",
            canonicalPath: "/subscription/policy"
        });
        trackPageView("/subscription/policy", "Subscription & Refund Policy | SamPrepIX");
    }, []);

    const policySections = [
        {
            icon: <FaUndoAlt style={{ color: "#4f46e5", fontSize: "1.4rem" }} />,
            title: "Cancellation & Refund Initiation",
            description: "When a premium subscription (Pro or Elite) is cancelled by either the user or by platform administration, an automated refund request is submitted through our payment gateway provider (Cashfree Payments). Your premium entitlements remain active only until cancellation confirmation, after which the account transitions back to the Starter plan."
        },
        {
            icon: <FaClock style={{ color: "#059669", fontSize: "1.4rem" }} />,
            title: "Refund Processing Timeframe",
            description: "Once initiated, standard banking refunds typically settle within 5 to 7 business days back to your original payment instrument (UPI, Debit Card, Credit Card, or Net Banking). The exact timing depends on your issuing bank and Cashfree's settlement window. We do not invent guaranteed instant turnaround times."
        },
        {
            icon: <FaShieldAlt style={{ color: "#7c3aed", fontSize: "1.4rem" }} />,
            title: "Platform Administration Revocation",
            description: "In the event platform administration cancels a premium tier for administrative, testing, or policy reconciliation reasons, an automatic refund is immediately dispatched for the eligible transaction. Affected users receive an immediate dashboard banner and email notification. Internal administration details are never exposed."
        },
        {
            icon: <FaCalendarCheck style={{ color: "#2563eb", fontSize: "1.4rem" }} />,
            title: "Auto-Renewal & Plan Duration",
            description: "All payments processed via Cashfree Payment Gateway are one-time charges valid for the full selected billing duration (30 days for monthly tiers). Automatic recurring debit or e-mandate billing is not active. When your plan cycle nears expiry, you can manually renew or choose another tier at your convenience."
        },
        {
            icon: <FaCreditCard style={{ color: "#e11d48", fontSize: "1.4rem" }} />,
            title: "Refund Status Tracking",
            description: "Refund progress follows transparent, verifiable states: Refund Initiated (dispatched to Cashfree), Refund Processing (bank clearing), and Refund Completed (funds credited). If provider processing encounters an issue, status is marked Refund Failed and our support team reviews the transaction."
        },
        {
            icon: <FaUserCheck style={{ color: "#d97706", fontSize: "1.4rem" }} />,
            title: "Purchasing Again After Cancellation",
            description: "Once your cancellation and refund have reached final reconciliation, you are always welcome to upgrade to Pro or Elite whenever you are ready. Your historical interview records, ATS resume scans, and coding problem milestones are safely preserved on your account."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Billing & Terms</span>
                <h1>Subscription & Refund Policy</h1>
                <p>
                    Clear, honest rules regarding your SamPrepIX membership, refund timelines, and account lifecycle.
                </p>
                <div style={{ marginTop: "18px", display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
                    <button
                        type="button"
                        onClick={() => navigate("/dashboard")}
                        className="content-page-nav-btn"
                    >
                        <FaArrowLeft /> Back to Dashboard
                    </button>
                    <button
                        type="button"
                        onClick={() => navigate("/pricing")}
                        className="content-page-nav-btn"
                    >
                        View Pricing Plans
                    </button>
                </div>
            </section>

            <main className="content-page-body">
                <div className="content-grid-2">
                    {policySections.map((sec, idx) => (
                        <div key={idx} className="content-card">
                            <div style={{ marginBottom: "12px" }}>{sec.icon}</div>
                            <h2 style={{ fontSize: "1.15rem", fontWeight: "700", margin: "0 0 8px 0" }}>
                                {sec.title}
                            </h2>
                            <p style={{ margin: 0, lineHeight: 1.65, fontSize: "0.93rem" }}>
                                {sec.description}
                            </p>
                        </div>
                    ))}
                </div>

                <div
                    className="content-card"
                    style={{
                        marginTop: "28px",
                        textAlign: "center",
                        padding: "32px 24px",
                        background: "rgba(99,102,241,0.05)",
                        border: "1px solid rgba(99,102,241,0.15)"
                    }}
                >
                    <FaExclamationCircle style={{ fontSize: "2rem", color: "#6366f1", marginBottom: "12px" }} />
                    <h3 style={{ margin: "0 0 8px 0", fontSize: "1.2rem", fontWeight: 700 }}>
                        Need Assistance With Your Payment?
                    </h3>
                    <p style={{ maxWidth: "560px", margin: "0 auto 18px", fontSize: "0.92rem", lineHeight: 1.6 }}>
                        Our payment infrastructure is secured by 256-bit encryption and reconciled server-side directly with Cashfree. For inquiries regarding receipts, transactions, or banking queries, visit your Billing history or contact support.
                    </p>
                    <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
                        <button
                            type="button"
                            className="content-btn-primary"
                            onClick={() => navigate("/billing-history")}
                            style={{
                                padding: "10px 22px",
                                borderRadius: "10px",
                                background: "#4f46e5",
                                color: "#fff",
                                border: "none",
                                fontWeight: 700,
                                cursor: "pointer"
                            }}
                        >
                            View Billing History & Invoices
                        </button>
                        <button
                            type="button"
                            className="content-page-nav-btn"
                            onClick={() => navigate("/pricing")}
                            style={{
                                padding: "10px 22px"
                            }}
                        >
                            View Pricing Plans
                        </button>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
}
