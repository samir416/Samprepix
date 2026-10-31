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
            title: "Subscription & Billing Policy | SamPrepIX",
            description: "Detailed policies on subscription lifecycle, AutoPay management, upgrade rules, non-refundable terms, and official invoicing.",
            canonicalPath: "/subscription/policy"
        });
        trackPageView("/subscription/policy", "Subscription & Billing Policy | SamPrepIX");
    }, []);

    const policySections = [
        {
            icon: <FaShieldAlt style={{ color: "#e11d48", fontSize: "1.4rem" }} />,
            title: "1. Non-Refundable Subscription Policy",
            description: "All subscription, renewal, and tier upgrade payments on SamPrepIX are strictly final and non-refundable upon successful completion. Due to immediate provisioning of AI computing models, compiler sandboxes, and premium placement curriculum, refunds are not provided for change of mind, non-usage, or partial usage."
        },
        {
            icon: <FaUndoAlt style={{ color: "#4f46e5", fontSize: "1.4rem" }} />,
            title: "2. No Mid-Cycle Cancellations",
            description: "Once activated, paid plans cannot be cancelled mid-cycle. Your premium access remains 100% active and available until the exact conclusion of your 30-day billing cycle. No early cancellation refunds or credits are issued."
        },
        {
            icon: <FaCalendarCheck style={{ color: "#2563eb", fontSize: "1.4rem" }} />,
            title: "3. AutoPay & Renewal Control",
            description: "You have full control over automatic renewals. You can turn off AutoPay at any time directly from your Billing settings. Turning off AutoPay ensures no future debits will occur, while your current paid plan remains fully active until its scheduled expiry date. Turning off AutoPay does not issue a refund."
        },
        {
            icon: <FaCreditCard style={{ color: "#7c3aed", fontSize: "1.4rem" }} />,
            title: "4. Plan Upgrades (Pro to Elite)",
            description: "Active Pro members can upgrade to Elite at any time by paying only the backend-calculated difference (₹799 - ₹399 = ₹400). Elite features unlock immediately upon payment verification and remain active for the remaining duration of your current billing period, preserving your original renewal date."
        },
        {
            icon: <FaClock style={{ color: "#059669", fontSize: "1.4rem" }} />,
            title: "5. Administrative Actions",
            description: "In the rare event that platform administration modifies or reconciles a subscription for policy or administrative reasons, any authorized administrative refund is handled directly through Cashfree Payments. The user receives immediate confirmation via their registered email."
        },
        {
            icon: <FaUserCheck style={{ color: "#d97706", fontSize: "1.4rem" }} />,
            title: "6. Official Tax Invoices & Records",
            description: "Every verified subscription, upgrade, or renewal transaction generates an official GST/Tax invoice receipt. You can inspect transaction records and download PDFs anytime from your Billing History."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Billing & Terms</span>
                <h1>Subscription & Billing Policy</h1>
                <p>
                    Clear, transparent rules regarding your SamPrepIX membership, AutoPay management, upgrade terms, and account lifecycle.
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
