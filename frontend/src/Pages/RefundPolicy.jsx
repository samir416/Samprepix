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
            description: "Read the official Refund & Cancellation Policy for SamPrepIX (Operated by SAMIR KUMAR). All payments are final and non-refundable. Learn about subscription terms, AutoPay management, and upgrade rules in INR.",
            canonicalPath: "/refund-policy"
        });
        trackPageView("/refund-policy", "Refund & Cancellation Policy | SamPrepIX");
    }, []);

    const policyCards = [
        {
            icon: <FaFileInvoiceDollar style={{ color: "#4f46e5", fontSize: "1.4rem" }} />,
            title: "1. Digital Services & Transparent INR Pricing",
            description: "SamPrepIX provides cloud-based digital placement preparation software, including AI mock interviews, ATS resume analysis, coding compilers, and aptitude question banks. We offer monthly paid plans — Pro (₹399/Month) and Elite (₹799/Month) — with all charges billed transparently in Indian Rupees (INR) through authorized payment aggregator Cashfree Payments."
        },
        {
            icon: <FaShieldAlt style={{ color: "#e11d48", fontSize: "1.4rem" }} />,
            title: "2. Strict Non-Refundable Policy",
            description: "All payments for subscriptions, renewals, and tier upgrades are final and strictly non-refundable upon successful completion. Due to immediate allocation of dedicated AI compute resources, compiler execution sandboxes, and premium curriculum access, refunds are not provided for change of mind, non-usage, partial usage, or unused days."
        },
        {
            icon: <FaUndoAlt style={{ color: "#2563eb", fontSize: "1.4rem" }} />,
            title: "3. No Mid-Cycle Cancellation",
            description: "Once a subscription payment is completed and activated, mid-term or mid-cycle plan cancellations are not supported. Your paid Pro or Elite benefits remain 100% active and accessible for the entire duration of your 30-day billing period until its scheduled expiration date."
        },
        {
            icon: <FaClock style={{ color: "#059669", fontSize: "1.4rem" }} />,
            title: "4. AutoPay & Future Renewal Management",
            description: "If AutoPay / recurring renewal is enabled on your account, you have full freedom to turn off AutoPay at any time prior to your next renewal date from your Billing settings. Turning off AutoPay ensures that you will not be billed for subsequent cycles. Disabling AutoPay does not cancel your current paid period, does not terminate active access early, and does not generate a refund."
        },
        {
            icon: <FaCreditCard style={{ color: "#7c3aed", fontSize: "1.4rem" }} />,
            title: "5. Plan Upgrades (Pro to Elite)",
            description: "Eligible Pro users may upgrade to Elite mid-cycle by paying only the backend-calculated difference (₹799 - ₹399 = ₹400). Elite access activates immediately upon verified payment and remains active for the remaining duration of your current billing period. Upgrading preserves your existing renewal/expiry date."
        },
        {
            icon: <FaHeadset style={{ color: "#d97706", fontSize: "1.4rem" }} />,
            title: "6. Duplicate Payment & Technical Inquiries",
            description: "If an unexpected technical anomaly or network timeout results in a duplicate transaction deduction for a single order, contact our support team at samirprajapat5@gmail.com with your registered email and Cashfree Order ID. Inquiries are verified against payment gateway records and resolved within 24 business hours."
        }
    ];

    return (
        <div className="content-page">
            <Navbar />

            <section className="content-page-hero">
                <span className="content-page-badge">Compliance & Transparency</span>
                <h1>Refund & Cancellation Policy</h1>
                <p>
                    Clear, transparent, and binding terms governing subscriptions, cancellations, and payments for SamPrepIX (Operated by SAMIR KUMAR).
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
                        This website and platform (<strong>SamPrepIX</strong>) is operated by <strong>SAMIR KUMAR</strong>. All commercial payment transactions, digital subscription checkouts, and billing inquiries are processed under this registered merchant entity in compliance with Cashfree Payment Gateway guidelines.
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
                        Need Assistance With a Payment or Billing Inquiry?
                    </h3>
                    <p style={{ maxWidth: "580px", margin: "0 auto 20px", fontSize: "0.93rem", lineHeight: 1.65, color: "var(--text-muted, #64748b)" }}>
                        For any queries regarding transaction status, invoice downloads, or payment verification, reach out directly to our support desk:
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
