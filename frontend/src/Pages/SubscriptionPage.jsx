import "../styles/admin.css";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSearchParams } from "react-router-dom";
import { getCurrentUser } from "../services/authService";
import { getActiveSubscription, subscribe, cancelSubscription, getCapabilities } from "../services/subscriptionService";
import { getActivePlans } from "../services/planService";
import { createTestOrder, verifyPayment } from "../services/paymentService";
import { getPricing } from "../services/planService";
import {
FaCheck,
    FaTimes,
    FaCreditCard,
    FaCrown,
    FaBolt,
    FaGift,
    FaArrowLeft,
    FaShieldAlt,
    FaCheckCircle,
    FaTimesCircle,
    FaExclamationTriangle,
    FaLock,
    FaUnlock
} from "react-icons/fa";

export default function SubscriptionPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const planId = searchParams.get("plan");

    const [plans, setPlans] = useState([]);
    const [selectedPlan, setSelectedPlan] = useState(null);
    const [activeSub, setActiveSub] = useState(null);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [currency, setCurrency] = useState("INR");
    const [capabilities, setCapabilities] = useState(null);
    const [testPricing, setTestPricing] = useState({ PRO: 1.0, ELITE: 2.0, STARTER: 0.0 });
    const [pricingMode, setPricingMode] = useState("test");

    useEffect(() => {
        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }
        loadData();
    }, [navigate]);

    useEffect(() => {
        if (planId) {
            setSelectedPlan(plans.find(p => p.id === Number(planId)) || null);
        }
    }, [planId, plans]);

    const loadData = async () => {
        try {
            const [plansData, subData, capsData, pricingData] = await Promise.all([
                getActivePlans(),
                getActiveSubscription(),
                getCapabilities(),
                getPricing()
            ]);
            setPlans(plansData);
            setCapabilities(capsData);
            if (pricingData && pricingData.testMode) {
                setTestPricing(pricingData.testMode);
                setPricingMode("test");
            }
            if (subData && subData.active) {
                setActiveSub(subData);
            }
        } catch (err) {
            setError("Failed to load subscription data");
        } finally {
            setLoading(false);
        }
    };

    const currentPrice = pricingMode === "test" ? testPricing[selectedPlan?.name] : selectedPlan?.priceInr;

    const handleSubscribe = async (plan) => {
        setProcessing(true);
        setError(null);
        try {
            const order = await createTestOrder(plan.id, currency, "razorpay");
            initiateRazorpayCheckout(order, plan);
        } catch (err) {
            setError("Failed to create order. Please try again.");
        } finally {
            setProcessing(false);
        }
    };

    const initiateRazorpayCheckout = (order, plan) => {
        const options = {
            key: order.key,
            amount: order.amount * 100,
            currency: order.currency,
            name: "Samprepix",
            description: `Subscription to ${plan.name} Plan`,
            order_id: order.razorpayOrderId,
            handler: function (response) {
                verifyAndActivate(response, plan);
            },
            prefill: {
                name: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")).name : "",
                email: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")).email : "",
                contact: ""
            },
            theme: {
                color: "#4f46e5"
            },
            modal: {
                ondismiss: function () {
                    setProcessing(false);
                }
            }
        };

        const rzp = new window.Razorpay(options);
        rzp.on("payment.failed", function (response) {
            setError("Payment failed: " + response.error.description);
            setProcessing(false);
        });
        rzp.open();
    };

    const verifyAndActivate = async (razorpayResponse, plan) => {
        try {
            const result = await verifyPayment({
                razorpay_order_id: razorpayResponse.razorpay_order_id,
                razorpay_payment_id: razorpayResponse.razorpay_payment_id,
                razorpay_signature: razorpayResponse.razorpay_signature
            });
            if (result.status === "success" || result.status === "already_success") {
                setSuccess(true);
                setActiveSub({ planName: plan.name, status: "ACTIVE", expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) });
                setTimeout(() => setSuccess(false), 5000);
            }
        } catch (err) {
            setError("Payment verification failed. Please contact support.");
        }
    };

    const handleCancel = async () => {
        if (!activeSub || !window.confirm("Cancel your subscription?")) return;
        try {
            await cancelSubscription(activeSub.subscriptionId || activeSub.id);
            setActiveSub(null);
            setSuccess(true);
            setTimeout(() => setSuccess(false), 3000);
        } catch (err) {
            setError("Failed to cancel subscription");
        }
    };

    const detectCurrency = () => {
        try {
            const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || "";
            if (tz.includes("Calcutta") || tz.includes("Kolkata") || tz.includes("India")) return "INR";
            const langs = navigator.languages || [navigator.language || ""];
            if (langs.some(l => /-(IN|in)$|^hi/.test(l))) return "INR";
        } catch {}
        return "USD";
    };

    if (loading) {
        return <div className="subscription-page"><div className="loading">Loading...</div></div>;
    }

    return (
        <div className="subscription-page">
            <div className="subscription-container">
                {success && (
                    <div className="success-banner">
                        <FaCheckCircle /> Operation completed successfully!
                    </div>
                )}

                {error && (
                    <div className="error-banner">
                        <FaExclamationTriangle /> {error}
                    </div>
                )}

                {capabilities && (
                    <div className="capabilities-bar">
                        <span className={`cap-badge ${capabilities.isAdmin ? "admin" : ""}`}>
                            {capabilities.isAdmin ? <FaShieldAlt /> : null} {capabilities.isAdmin ? "ADMIN Unlimited" : capabilities.effectivePlan}
                        </span>
                        <span>Mocks: {capabilities.maxMockInterviews}/mo</span>
                        <span>AI Hints: {capabilities.includesAIHints ? <FaUnlock /> : <FaLock />}</span>
                        <span>Analytics: {capabilities.includesAnalytics ? <FaUnlock /> : <FaLock />}</span>
                        <span>Tier-1: {capabilities.includesTier1Companies ? <FaUnlock /> : <FaLock />}</span>
                        <span>Priority Compute: {capabilities.includesPriorityCompute ? <FaUnlock /> : <FaLock />}</span>
                    </div>
                )}

                {activeSub && !capabilities?.isAdmin && (
                    <div className="active-subscription-card">
                        <h1>
                            <FaCheckCircle /> Active Subscription
                        </h1>
                        <div className="active-sub-details">
                            <div className="sub-plan-name">{activeSub.plan || activeSub.planName}</div>
                            <div className="sub-status">Status: <span className="status-badge active">{activeSub.subscriptionStatus || activeSub.status}</span></div>
                            <div className="sub-expiry">Expires: {activeSub.expiresAt || activeSub.expiresAt ? new Date(activeSub.expiresAt).toLocaleDateString() : "N/A"}</div>
                            <div className="sub-amount">Amount: ₹{activeSub.amountPaid || activeSub.amount}/{activeSub.currency}</div>
                            <button className="cancel-sub-btn" onClick={handleCancel}>Cancel Subscription</button>
                        </div>
                    </div>
                )}

                {!activeSub || capabilities?.isAdmin ? (
                    <div className="subscribe-section">
                        <h1>Choose Your Plan</h1>
                        {capabilities?.isAdmin && (
                            <div className="admin-notice">
                                <FaShieldAlt /> Admin Access: All premium features unlocked. No payment required.
                            </div>
                        )}
                        <p className="sub-subtitle">Upgrade to unlock premium features for your placement preparation</p>

                        <div className="sub-plans-grid">
                            {plans.map(plan => {
                                const price = pricingMode === "test" ? testPricing[plan.name] : plan.priceInr;
                                const isDisabled = capabilities?.isAdmin;
                                return (
                                    <div key={plan.id} className={`sub-plan-card ${plan.featured ? "featured" : ""}`}>
                                        {plan.featured && <div className="featured-badge">⭐ Recommended</div>}
                                        <h2>{plan.name}</h2>
                                        <p>{plan.description}</p>
                                        <div className="sub-price">
                                            <span className="price-currency">{currency === "INR" ? "₹" : "$"}</span>
                                            <span className="price-amount">{price}</span>
                                            {pricingMode === "test" && <span className="test-badge">TEST</span>}
                                            <span className="price-period">/{plan.interval}</span>
                                        </div>
                                        <ul className="sub-features">
                                            <li><FaCheck /> {plan.maxMockInterviews} AI Mock Interviews/month</li>
                                            <li><FaCheck /> {plan.maxResumeScans} Resume Scans</li>
                                            <li><FaCheck /> {plan.maxCodingProblems} Coding Problems</li>
                                            <li><FaCheck /> {plan.maxAptitudeQuestions} Aptitude Questions</li>
                                            {plan.includesAIHints && <li><FaBolt /> AI Hints</li>}
                                            {plan.includesAnalytics && <li><FaChartLine /> Analytics</li>}
                                            {plan.includesTier1Companies && <li><FaCrown /> Tier-1 Company Prep</li>}
                                            {plan.includesPriorityCompute && <li><FaBolt /> Priority Compute</li>}
                                        </ul>
                                        <button
                                            className={`subscribe-btn ${plan.featured ? "primary" : ""}`}
                                            onClick={() => !isDisabled && handleSubscribe(plan)}
                                            disabled={processing || isDisabled}
                                        >
                                            {isDisabled ? "ADMIN - Unlimited Access" : processing ? "Processing..." : `Subscribe to ${plan.name}`}
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ) : null}

                <div className="pricing-note">
                    <p>Test Mode pricing: Pro ₹1, Elite ₹2 (Production: Pro ₹399, Elite ₹799)</p>
                    <p>All payments processed securely via Razorpay Test Mode.</p>
                    <p>Test card: 4111 1111 1111 1111</p>
                </div>
            </div>
        </div>
    );
}
