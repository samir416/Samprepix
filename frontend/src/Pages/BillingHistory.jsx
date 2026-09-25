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
import "../styles/billingHistory.css";

export default function BillingHistory() {
    const navigate = useNavigate();
    const [invoices, setInvoices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [downloadingId, setDownloadingId] = useState(null);
    const [activePlan, setActivePlan] = useState("STARTER");

    const token = localStorage.getItem("token");

    const fetchInvoices = async () => {
        if (!token) {
            setLoading(false);
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const res = await axios.get(`${API_BASE_URL}/api/invoices`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            const data = Array.isArray(res.data) ? res.data : [];
            setInvoices(data);

            // Fetch current subscription plan
            try {
                const subRes = await axios.get(`${API_BASE_URL}/api/subscription/current`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                if (subRes.data && subRes.data.planName) {
                    setActivePlan(subRes.data.planName);
                }
            } catch (_) {
                // Ignore sub fetch fallback
            }
        } catch (err) {
            console.error("Failed to fetch billing history:", err);
            setError(err?.response?.data?.error || err?.response?.data?.message || "Unable to retrieve your invoice history. Please check your connection.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchInvoices();
    }, [token]);

    const handleDownloadInvoice = async (invoiceId, invoiceNumber) => {
        if (!token) return;
        setDownloadingId(invoiceId);
        try {
            const response = await axios.get(
                `${API_BASE_URL}/api/invoices/${invoiceId}/pdf`,
                {
                    headers: { Authorization: `Bearer ${token}` },
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
            alert("Failed to download invoice PDF. Please try again.");
        } finally {
            setDownloadingId(null);
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
                        <div className="bh-stat-icon">
                            <FaCreditCard />
                        </div>
                        <div className="bh-stat-info">
                            <h3>Payment Security</h3>
                            <div className="bh-stat-val">
                                256-Bit Encrypted
                            </div>
                        </div>
                    </div>
                </div>

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
                            <h3>No Billing Records Found</h3>
                            <p>
                                You haven't made any purchases yet. When you upgrade to Pro (₹1) or Elite (₹2),
                                all tax receipts and downloadable PDF invoices will be available here.
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
