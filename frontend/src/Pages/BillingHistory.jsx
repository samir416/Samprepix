import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
    FaReceipt,
    FaArrowLeft,
    FaDownload,
    FaSpinner,
    FaCheckCircle,
    FaClock,
    FaTimesCircle,
    FaLock,
    FaCreditCard,
    FaRedo,
    FaCrown
} from "react-icons/fa";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { API_BASE_URL } from "../config";
import { getCleanToken } from "../services/profileService";
import { cancelSubscription } from "../services/subscriptionService";
import { toast } from "react-toastify";
import "../styles/billingHistory.css";

export default function BillingHistory() {
    const navigate = useNavigate();
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [downloadingId, setDownloadingId] = useState(null);
    const [activePlan, setActivePlan] = useState("STARTER");
    const [billingStatus, setBillingStatus] = useState("Active");
    const [nextRenewal, setNextRenewal] = useState(null);
    const [currentSub, setCurrentSub] = useState(null);
    const [cancelling, setCancelling] = useState(false);
    const [cancelModalOpen, setCancelModalOpen] = useState(false);
    const [cancelSuccessMsg, setCancelSuccessMsg] = useState("");
    const [cancelErrorMsg, setCancelErrorMsg] = useState("");

    const token = getCleanToken();

    const fetchInvoices = async () => {
        const cleanToken = getCleanToken();
        if (!cleanToken) {
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const res = await axios.get(`${API_BASE_URL}/api/invoices`, {
                headers: { Authorization: `Bearer ${cleanToken}` }
            });
            const data = Array.isArray(res.data) ? res.data : [];
            setInvoices(data);

            // Fetch current subscription status & plan
            try {
                const subRes = await axios.get(`${API_BASE_URL}/api/subscription/current`, {
                    headers: { Authorization: `Bearer ${cleanToken}` }
                });
                if (subRes.data) {
                    setCurrentSub(subRes.data);
                    const plan = subRes.data.effectivePlan || subRes.data.plan || subRes.data.planName || "STARTER";
                    setActivePlan(plan);
                    setBillingStatus(subRes.data.subscriptionStatus || subRes.data.status || (plan === "STARTER" ? "Free Tier" : "Active"));
                    if (subRes.data.expiresAt) {
                        setNextRenewal(subRes.data.expiresAt);
                    }
                }
            } catch (_) {
                // Ignore sub fetch fallback
            }
        } catch (err) {
            console.error("Failed to fetch billing history:", err);
            if (err?.response?.status === 401) {
                // Token expired or invalid: remove stale token cleanly
                localStorage.removeItem("token");
                setInvoices([]);
                setError(null);
            } else {
                setError(err?.response?.data?.error || err?.response?.data?.message || "Unable to retrieve your invoice history. Please check your connection.");
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchInvoices();
    }, [token]);

    const handleDownloadInvoice = async (invoiceId, invoiceNumber) => {
        const cleanToken = getCleanToken();
        if (!cleanToken) return;
        setDownloadingId(invoiceId);
        try {
            const response = await axios.get(
                `${API_BASE_URL}/api/invoices/${invoiceId}/pdf`,
                {
                    headers: { Authorization: `Bearer ${cleanToken}` },
                    responseType: "blob"
                }
            );

            const blob = new Blob([response.data], { type: "application/pdf" });
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = `Samprepix-Invoice-${invoiceNumber || invoiceId}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error("Failed to download invoice PDF:", err);
            toast.error("Unable to download the invoice. Please try again.");
        } finally {
            setDownloadingId(null);
        }
    };

    const handleCancelSubscription = async () => {
        if (!currentSub?.subscriptionId) return;
        setCancelling(true);
        setCancelErrorMsg("");
        setCancelSuccessMsg("");
        try {
            await cancelSubscription(currentSub.subscriptionId, "User requested cancellation via billing");
            setCancelModalOpen(false);
            setCancelSuccessMsg(
                "Your subscription has been cancelled. An automated refund has been initiated to your original payment method (typically 5–7 business days)."
            );
            await fetchInvoices();
        } catch (err) {
            setCancelErrorMsg(
                err?.response?.data?.error || err?.response?.data?.message || err?.message || "Failed to cancel subscription."
            );
        } finally {
            setCancelling(false);
        }
    };

    const formatCurrency = (amount, curr) => {
        const val = Number(amount || 0);
        if (curr === "USD") {
            return `$${val.toFixed(2)}`;
        }
        return `₹${val.toFixed(2)}`;
    };

    const renderStatusBadge = (status) => {
        const s = (status || "PAID").toUpperCase();
        if (s === "PAID" || s === "SUCCESS") {
            return (
                <span className="bh-badge paid">
                    <FaCheckCircle style={{ fontSize: "10px" }} /> Paid
                </span>
            );
        }
        if (s === "PENDING" || s === "PROCESSING") {
            return (
                <span className="bh-badge pending">
                    <FaClock style={{ fontSize: "10px" }} /> Pending
                </span>
            );
        }
        return (
            <span className="bh-badge failed">
                <FaTimesCircle style={{ fontSize: "10px" }} /> {s}
            </span>
        );
    };

    return (
        <div className="billing-history-page">
            <Navbar />

            <main className="billing-history-container">
                {/* BACK NAVIGATION */}
                <button
                    type="button"
                    className="bh-back-link"
                    onClick={() => navigate("/pricing")}
                >
                    <FaArrowLeft /> Back to Pricing Plans
                </button>

                {/* HEADER */}
                <div className="bh-header">
                    <div className="bh-header-badge">
                        <FaReceipt /> Verified Invoices & Billing
                    </div>
                    <h1>
                        <FaReceipt /> Billing History & Invoices
                    </h1>
                    <p>
                        Review your verified subscription transactions, inspect payment details, and download
                        official GST/Tax invoice receipts in PDF format.
                    </p>
                </div>

                {/* STATS OVERVIEW */}
                <div className="bh-stats-grid">
                    <div className="bh-stat-card">
                        <div className="bh-stat-icon">
                            <FaCrown />
                        </div>
                        <div className="bh-stat-info">
                            <h3>Current Tier</h3>
                            <div className="bh-stat-val">
                                {activePlan.toUpperCase()} Plan
                            </div>
                        </div>
                    </div>

                    <div className="bh-stat-card">
                        <div className="bh-stat-icon green">
                            <FaReceipt />
                        </div>
                        <div className="bh-stat-info">
                            <h3>Total Invoices</h3>
                            <div className="bh-stat-val">
                                {token ? invoices.length : 0} Records
                            </div>
                        </div>
                    </div>

                    <div className="bh-stat-card">
                        <div className="bh-stat-icon blue">
                            <FaCreditCard />
                        </div>
                        <div className="bh-stat-info">
                            <h3>Billing Status</h3>
                            <div className="bh-stat-val">
                                {activePlan.toUpperCase() === "STARTER" ? "Free Tier" : (billingStatus || "Active")}
                            </div>
                        </div>
                    </div>
                </div>

                {cancelSuccessMsg && (
                    <div style={{
                        padding: "14px 18px",
                        marginBottom: "20px",
                        borderRadius: "12px",
                        background: "rgba(16,185,129,0.08)",
                        border: "1px solid rgba(16,185,129,0.25)",
                        color: "#10b981",
                        fontSize: "13.5px",
                        fontWeight: "600",
                        display: "flex",
                        alignItems: "center",
                        gap: "10px"
                    }}>
                        <FaCheckCircle /> {cancelSuccessMsg}
                    </div>
                )}

                {cancelErrorMsg && (
                    <div style={{
                        padding: "14px 18px",
                        marginBottom: "20px",
                        borderRadius: "12px",
                        background: "rgba(239,68,68,0.08)",
                        border: "1px solid rgba(239,68,68,0.25)",
                        color: "#ef4444",
                        fontSize: "13.5px",
                        fontWeight: "600",
                        display: "flex",
                        alignItems: "center",
                        gap: "10px"
                    }}>
                        <FaTimesCircle /> {cancelErrorMsg}
                    </div>
                )}

                {token && activePlan.toUpperCase() !== "STARTER" && (
                    <div className="bh-card" style={{ marginBottom: "24px" }}>
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "16px" }}>
                            <div>
                                <div style={{ fontSize: "12px", fontWeight: "700", textTransform: "uppercase", letterSpacing: "1px", color: "#6366f1" }}>
                                    Current Plan Lifecycle
                                </div>
                                <h3 style={{ margin: "4px 0 6px", fontSize: "1.2rem", fontWeight: "800" }}>
                                    {activePlan.toUpperCase()} Membership ({billingStatus})
                                </h3>
                                <p style={{ margin: 0, fontSize: "13px", color: "var(--text-muted, #64748b)" }}>
                                    Renewal Mode: <strong>Manual Renewal</strong> (Cashfree PG one-time payment) · Valid until: <strong>{nextRenewal ? new Date(nextRenewal).toLocaleDateString() : "Active Cycle"}</strong>
                                </p>
                            </div>

                            <div style={{ display: "flex", gap: "10px", alignItems: "center", flexWrap: "wrap" }}>
                                <button
                                    type="button"
                                    onClick={() => navigate("/subscription/policy")}
                                    style={{
                                        padding: "8px 14px",
                                        borderRadius: "9px",
                                        background: "transparent",
                                        border: "1px solid rgba(99,102,241,0.25)",
                                        color: "#6366f1",
                                        fontSize: "12.5px",
                                        fontWeight: "600",
                                        cursor: "pointer"
                                    }}
                                >
                                    Policy Details
                                </button>

                            </div>
                        </div>
                    </div>
                )}

                {/* MAIN INVOICE LIST CARD */}
                <div className="bh-card">
                    <div className="bh-card-header">
                        <h2 className="bh-card-title">All Invoices & Receipts</h2>
                        {token && (
                            <button
                                type="button"
                                className="bh-refresh-btn"
                                onClick={fetchInvoices}
                                disabled={loading}
                                title="Refresh invoices"
                            >
                                <FaRedo style={{ animation: loading ? "spin 1s linear infinite" : "none" }} />
                                Refresh
                            </button>
                        )}
                    </div>

                    {/* UNAUTHENTICATED STATE */}
                    {!token ? (
                        <div className="bh-empty-state">
                            <FaLock className="bh-empty-icon" style={{ color: "#6366f1" }} />
                            <h3>Sign In to View Invoices</h3>
                            <p>
                                Your subscription invoices and tax receipts are safely linked to your personal
                                account. Sign in to view and download your documents.
                            </p>
                            <button
                                type="button"
                                className="bh-btn-primary"
                                onClick={() => navigate("/login")}
                            >
                                Sign In to Your Account
                            </button>
                        </div>
                    ) : loading ? (
                        /* LOADING STATE */
                        <div className="bh-empty-state">
                            <FaSpinner className="bh-empty-icon" style={{ animation: "spin 1s linear infinite", color: "#6366f1" }} />
                            <h3>Loading Invoices...</h3>
                            <p>Retrieving your billing records from the server.</p>
                        </div>
                    ) : error ? (
                        /* ERROR STATE */
                        <div className="bh-empty-state">
                            <FaTimesCircle className="bh-empty-icon" style={{ color: "#ef4444" }} />
                            <h3>Unable to Load Invoices</h3>
                            <p>{error}</p>
                            <button
                                type="button"
                                className="bh-btn-primary"
                                onClick={fetchInvoices}
                            >
                                <FaRedo /> Try Again
                            </button>
                        </div>
                    ) : invoices.length === 0 ? (
                        /* EMPTY STATE */
                        <div className="bh-empty-state">
                            <FaReceipt className="bh-empty-icon" />
                            <h3>No Invoices Yet</h3>
                            <p>
                                You don't have any billing records yet. When you upgrade to Pro (₹1) or Elite (₹2),
                                all verified tax receipts and downloadable PDF invoices will appear here.
                            </p>
                            <button
                                type="button"
                                className="bh-btn-primary"
                                onClick={() => navigate("/pricing")}
                            >
                                Explore Pricing Plans
                            </button>
                        </div>
                    ) : (
                        /* INVOICES TABLE */
                        <div className="bh-table-wrapper">
                            <table className="bh-table">
                                <thead>
                                    <tr>
                                        <th>Invoice #</th>
                                        <th>Plan</th>
                                        <th>Issued Date</th>
                                        <th>Amount</th>
                                        <th>Status</th>
                                        <th style={{ textAlign: "right" }}>Receipt</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {invoices.map((inv) => (
                                        <tr key={inv.id || inv.invoiceNumber}>
                                            <td style={{ fontWeight: "700" }}>
                                                {inv.invoiceNumber || `INV-${inv.id}`}
                                            </td>
                                            <td>
                                                <span style={{ fontWeight: "600" }}>
                                                    {inv.plan || inv.planName || "Subscription"}
                                                </span>
                                            </td>
                                            <td style={{ color: "var(--text-muted, #64748b)" }}>
                                                {inv.issuedAt || inv.createdAt || "Recent"}
                                            </td>
                                            <td style={{ fontWeight: "800", color: "#6366f1" }}>
                                                {formatCurrency(inv.amount, inv.currency)}
                                            </td>
                                            <td>
                                                {renderStatusBadge(inv.status)}
                                            </td>
                                            <td style={{ textAlign: "right" }}>
                                                <button
                                                    type="button"
                                                    className="bh-download-btn"
                                                    onClick={() => handleDownloadInvoice(inv.id, inv.invoiceNumber)}
                                                    disabled={downloadingId === inv.id}
                                                    title="Download verified PDF tax receipt"
                                                >
                                                    {downloadingId === inv.id ? (
                                                        <FaSpinner style={{ animation: "spin 1s linear infinite" }} />
                                                    ) : (
                                                        <FaDownload />
                                                    )}
                                                    PDF
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </main>

            {cancelModalOpen && (
                <div style={{
                    position: "fixed",
                    inset: 0,
                    zIndex: 9999,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    background: "rgba(0,0,0,0.7)",
                    backdropFilter: "blur(6px)",
                    padding: "20px"
                }}>
                    <div style={{
                        maxWidth: "460px",
                        width: "100%",
                        background: "var(--card-bg, #1a2236)",
                        border: "1px solid rgba(255,255,255,0.1)",
                        borderRadius: "20px",
                        padding: "28px",
                        textAlign: "center",
                        boxShadow: "0 25px 60px rgba(0,0,0,0.5)"
                    }}>
                        <FaTimesCircle style={{ fontSize: "2.5rem", color: "#ef4444", marginBottom: "14px" }} />
                        <h3 style={{ margin: "0 0 10px", fontSize: "1.25rem", fontWeight: 800 }}>
                            Cancel Subscription & Request Refund?
                        </h3>
                        <p style={{ fontSize: "13.5px", color: "var(--text-muted, #94a3b8)", lineHeight: 1.6, margin: "0 0 20px" }}>
                            An automated refund for your eligible payment will be initiated through Cashfree Payments. Banking settlements typically process within <strong>5–7 business days</strong> back to your original payment method. Your account will revert to the Starter tier.
                        </p>
                        <div style={{ display: "flex", gap: "12px", justifyContent: "center" }}>
                            <button
                                type="button"
                                onClick={() => setCancelModalOpen(false)}
                                disabled={cancelling}
                                style={{
                                    padding: "10px 20px",
                                    borderRadius: "10px",
                                    background: "rgba(255,255,255,0.08)",
                                    border: "1px solid rgba(255,255,255,0.15)",
                                    color: "inherit",
                                    fontWeight: 600,
                                    cursor: "pointer"
                                }}
                            >
                                Keep Subscription
                            </button>
                            <button
                                type="button"
                                onClick={handleCancelSubscription}
                                disabled={cancelling}
                                style={{
                                    padding: "10px 20px",
                                    borderRadius: "10px",
                                    background: "#dc2626",
                                    border: "none",
                                    color: "#fff",
                                    fontWeight: 700,
                                    cursor: "pointer",
                                    display: "flex",
                                    alignItems: "center",
                                    gap: "8px"
                                }}
                            >
                                {cancelling ? <FaSpinner style={{ animation: "spin 1s linear infinite" }} /> : null}
                                {cancelling ? "Processing..." : "Confirm & Refund"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            <Footer />

            <style>{`
                @keyframes spin {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
                }
            `}</style>
        </div>
    );
}
