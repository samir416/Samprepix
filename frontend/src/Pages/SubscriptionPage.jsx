import "../styles/admin.css";
import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import {
    getActiveSubscription,
    cancelSubscription,
    getCapabilities
} from "../services/subscriptionService";

import { getActivePlans } from "../services/planService";

import {
    createTestOrder,
    verifyPayment,
    markPaymentFailed
} from "../services/paymentService";

import {
    FaCheck,
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
    FaUnlock,
    FaChartLine
} from "react-icons/fa";
import ConfirmModal from "../Components/Common/ConfirmModal";

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
    const [confirmModal, setConfirmModal] = useState({ isOpen: false, message: "", onConfirm: null });

    const [currency, setCurrency] = useState("INR");

    const [capabilities, setCapabilities] = useState(null);

    // Test-mode display pricing.
    // Actual payment amount is always determined by backend.
    const [testPricing] = useState({
        PRO: {
            INR: 1.0,
            USD: 1.0
        },
        ELITE: {
            INR: 2.0,
            USD: 2.0
        },
        STARTER: {
            INR: 0.0,
            USD: 0.0
        }
    });

    const pricingMode = "test";

    // =========================================================
    // INITIAL LOAD
    // =========================================================

    useEffect(() => {

        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }

        setCurrency(detectCurrency());

        loadData();

    }, [navigate]);

    // =========================================================
    // SELECT PLAN FROM QUERY PARAM
    // =========================================================

    useEffect(() => {

        if (!planId) {
            setSelectedPlan(null);
            return;
        }

        const selected =
            plans.find(
                plan => plan.id === Number(planId)
            ) || null;

        setSelectedPlan(selected);

    }, [planId, plans]);

    // =========================================================
    // LOAD SUBSCRIPTION DATA
    // =========================================================

    const loadData = async () => {

        try {

            setError(null);

            const [
                plansData,
                subData,
                capsData
            ] = await Promise.all([
                getActivePlans(),
                getActiveSubscription(),
                getCapabilities()
            ]);

            setPlans(
                Array.isArray(plansData)
                    ? plansData
                    : []
            );

            setCapabilities(capsData || null);

            if (subData && subData.active) {
                setActiveSub(subData);
            } else {
                setActiveSub(null);
            }

        } catch (err) {

            console.error(
                "Failed to load subscription data:",
                err
            );

            setError(
                err?.response?.data?.message ||
                err?.response?.data ||
                "Failed to load subscription data"
            );

        } finally {

            setLoading(false);
        }
    };

    // =========================================================
    // PRICE DISPLAY
    // =========================================================

    const getDisplayPrice = (plan) => {

        if (!plan) {
            return 0;
        }

        if (plan.name === "STARTER") {
            return 0;
        }

        if (pricingMode === "test") {

            return (
                testPricing?.[plan.name]?.[currency] ??
                0
            );
        }

        return currency === "USD"
            ? (plan.priceUsd ?? 0)
            : (plan.priceInr ?? 0);
    };

    // =========================================================
    // SUBSCRIBE
    // =========================================================

    const handleSubscribe = async (plan) => {

        if (!plan) {
            return;
        }

        if (plan.name === "STARTER") {
            navigate("/dashboard");
            return;
        }

        if (capabilities?.isAdmin) {
            return;
        }

        if (processing) {
            return;
        }

        setProcessing(true);
        setError(null);
        setSuccess(false);

        try {

            const referralCode =
                localStorage.getItem("referralCode") || null;

            /*
             * Backend determines:
             * - plan
             * - amount
             * - currency validation
             * - referral discount
             * - Razorpay order
             */
            const order = await createTestOrder(
                plan.id,
                currency,
                "cashfree",
                referralCode
            );

            if (!order?.cashfreeOrderId) {
                throw new Error(
                    "Invalid Razorpay order response."
                );
            }

            initiateCashfreeCheckout(
                order,
                plan
            );

        } catch (err) {

            console.error(
                "Failed to create Razorpay order:",
                err
            );

            setError(
                err?.response?.data?.message ||
                err?.response?.data ||
                err?.message ||
                "Failed to create order. Please try again."
            );

            setProcessing(false);
        }
    };

    // =========================================================
    // RAZORPAY CHECKOUT
    // =========================================================

    const initiateCashfreeCheckout = (
        order,
        plan
    ) => {

        if (!window.Razorpay) {

            setError(
                "Razorpay checkout is not loaded. Please refresh the page and try again."
            );

            setProcessing(false);

            return;
        }

        const storedUser =
            localStorage.getItem("user");

        let user = {};

        try {

            user = storedUser
                ? JSON.parse(storedUser)
                : {};

        } catch {
            user = {};
        }

        const options = {

            key: order.key,

            /*
             * Razorpay expects amount in paise.
             * Backend returns amount in major currency unit.
             */
            amount:
                Number(order.amount || 0) * 100,

            currency:
                order.currency || currency,

            name: "Samprepix",

            description:
                `Subscription to ${plan.name} Plan`,

            order_id:
                order.cashfreeOrderId,

            handler: function (response) {

                verifyAndActivate(
                    response,
                    plan
                );
            },

            prefill: {

                name:
                    user?.name || "",

                email:
                    user?.email || "",

                contact:
                    user?.phone || ""
            },

            notes: {
                plan: plan.name
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

        const razorpay =
            new window.Razorpay(options);

        razorpay.on(
            "payment.failed",
            async function (response) {

                try {

                    await markPaymentFailed(
                        order.cashfreeOrderId
                    );

                } catch (error) {

                    console.error(
                        "Failed to mark payment as failed:",
                        error
                    );
                }

                setError(
                    "Payment failed: " +
                    (
                        response?.error?.description ||
                        "Please try again."
                    )
                );

                setProcessing(false);
            }
        );

        razorpay.open();
    };

    // =========================================================
    // PAYMENT VERIFICATION
    // =========================================================

    const verifyAndActivate = async (
        cashfreeResponse,
        plan
    ) => {

        try {

            setError(null);

            const result =
                await verifyPayment({

                    razorpay_order_id:
                        cashfreeResponse.order_id,

                    razorpay_payment_id:
                        cashfreeResponse.razorpay_payment_id,

                    razorpay_signature:
                        cashfreeResponse.razorpay_signature
                });

            if (
                result?.status === "success" ||
                result?.status === "already_success"
            ) {

                /*
                 * IMPORTANT:
                 * Do NOT create/fake subscription
                 * on frontend.
                 *
                 * Backend verification activates it.
                 */
                setSuccess(true);

                await loadData();

                setTimeout(() => {
                    setSuccess(false);
                }, 5000);

            } else {

                setError(
                    result?.message ||
                    "Payment verification was unsuccessful."
                );
            }

        } catch (err) {

            console.error(
                "Payment verification failed:",
                err
            );

            setError(
                err?.response?.data?.message ||
                err?.response?.data ||
                "Payment verification failed. Please contact support."
            );

        } finally {

            setProcessing(false);
        }
    };

    // =========================================================
    // CANCEL SUBSCRIPTION
    // =========================================================

    const handleCancel = async () => {

        if (!activeSub) {
            return;
        }

        setConfirmModal({
            isOpen: true,
            message: "Cancel your subscription?",
            onConfirm: async () => {
                setConfirmModal({ isOpen: false, message: "", onConfirm: null });
                try {

            setError(null);
            setProcessing(true);

            await cancelSubscription(
                activeSub.subscriptionId ||
                activeSub.id
            );

            setActiveSub(null);

            setSuccess(true);

            await loadData();

            setTimeout(() => {
                setSuccess(false);
            }, 3000);

        } catch (err) {

            console.error(
                "Failed to cancel subscription:",
                err
            );

            setError(
                err?.response?.data?.message ||
                err?.response?.data ||
                "Failed to cancel subscription"
            );

        } finally {

            setProcessing(false);
        }
            }
        });
    };

    // =========================================================
    // CURRENCY DETECTION
    // =========================================================

    const detectCurrency = () => {

        try {

            const timezone =
                Intl.DateTimeFormat()
                    .resolvedOptions()
                    .timeZone || "";

            if (
                timezone.includes("Calcutta") ||
                timezone.includes("Kolkata") ||
                timezone.includes("India")
            ) {
                return "INR";
            }

            const languages =
                navigator.languages ||
                [
                    navigator.language || ""
                ];

            if (
                languages.some(
                    language =>
                        /-(IN|in)$|^hi/.test(language)
                )
            ) {
                return "INR";
            }

        } catch {
            // Fallback below.
        }

        return "USD";
    };

    // =========================================================
    // LOADING
    // =========================================================

    if (loading) {

        return (
            <div className="subscription-page">
                <div className="loading">
                    Loading...
                </div>
            </div>
        );
    }

    // =========================================================
    // PAGE
    // =========================================================

    return (

        <div className="subscription-page">

            <div className="subscription-container">

                {/* SUCCESS */}

                {success && (

                    <div className="success-banner">

                        <FaCheckCircle />

                        Operation completed successfully!

                    </div>
                )}

                {/* ERROR */}

                {error && (

                    <div className="error-banner">

                        <FaExclamationTriangle />

                        {error}

                    </div>
                )}

                {/* CAPABILITIES */}

                {capabilities && (

                    <div className="capabilities-bar">

                        <span
                            className={
                                `cap-badge ${
                                    capabilities.isAdmin
                                        ? "admin"
                                        : ""
                                }`
                            }
                        >

                            {capabilities.isAdmin
                                ? <FaShieldAlt />
                                : null
                            }

                            {
                                capabilities.isAdmin
                                    ? "ADMIN Unlimited"
                                    : capabilities.effectivePlan
                            }

                        </span>

                        <span>
                            Mocks: {
                                capabilities.maxMockInterviews
                            }
                            /mo
                        </span>

                        <span>
                            AI Hints: {
                                capabilities.includesAIHints
                                    ? <FaUnlock />
                                    : <FaLock />
                            }
                        </span>

                        <span>
                            Analytics: {
                                capabilities.includesAnalytics
                                    ? <FaUnlock />
                                    : <FaLock />
                            }
                        </span>

                        <span>
                            Tier-1: {
                                capabilities.includesTier1Companies
                                    ? <FaUnlock />
                                    : <FaLock />
                            }
                        </span>

                        <span>
                            Priority Compute: {
                                capabilities.includesPriorityCompute
                                    ? <FaUnlock />
                                    : <FaLock />
                            }
                        </span>

                    </div>
                )}

                {/* ACTIVE SUBSCRIPTION */}

                {activeSub &&
                    !capabilities?.isAdmin && (

                    <div className="active-subscription-card">

                        <h1>

                            <FaCheckCircle />

                            Active Subscription

                        </h1>

                        <div className="active-sub-details">

                            <div className="sub-plan-name">

                                {
                                    activeSub.plan ||
                                    activeSub.planName
                                }

                            </div>

                            <div className="sub-status">

                                Status:

                                <span className="status-badge active">

                                    {
                                        activeSub.subscriptionStatus ||
                                        activeSub.status
                                    }

                                </span>

                            </div>

                            <div className="sub-expiry">

                                Expires:{" "}

                                {
                                    activeSub.expiresAt
                                        ? new Date(
                                            activeSub.expiresAt
                                        ).toLocaleDateString()
                                        : "N/A"
                                }

                            </div>

                            <div className="sub-amount">

                                Amount:{" "}

                                {
                                    activeSub.currency === "USD"
                                        ? "$"
                                        : "₹"
                                }

                                {
                                    activeSub.amountPaid ??
                                    activeSub.amount ??
                                    0
                                }

                                {" / "}

                                {
                                    activeSub.currency ||
                                    "INR"
                                }

                            </div>

                            <button
                                className="cancel-sub-btn"
                                onClick={handleCancel}
                                disabled={processing}
                            >
                                Cancel Subscription
                            </button>

                        </div>

                    </div>
                )}

                {/* PLAN SELECTION */}

                {(!activeSub ||
                    capabilities?.isAdmin) && (

                    <div className="subscribe-section">

                        <h1>
                            Choose Your Plan
                        </h1>

                        {capabilities?.isAdmin && (

                            <div className="admin-notice">

                                <FaShieldAlt />

                                Admin Access:
                                All premium features unlocked.
                                No payment required.

                            </div>
                        )}

                        <p className="sub-subtitle">

                            Upgrade to unlock premium
                            features for your placement
                            preparation

                        </p>

                        <div className="sub-plans-grid">

                            {plans.map(plan => {

                                const price =
                                    getDisplayPrice(plan);

                                const isDisabled =
                                    capabilities?.isAdmin;

                                return (

                                    <div
                                        key={plan.id}
                                        className={
                                            `sub-plan-card ${
                                                plan.featured
                                                    ? "featured"
                                                    : ""
                                            }`
                                        }
                                    >

                                        {plan.featured && (

                                            <div className="featured-badge">

                                                ⭐ Recommended

                                            </div>
                                        )}

                                        <h2>
                                            {plan.name}
                                        </h2>

                                        <p>
                                            {plan.description}
                                        </p>

                                        <div className="sub-price">

                                            <span className="price-currency">

                                                {
                                                    currency === "INR"
                                                        ? "₹"
                                                        : "$"
                                                }

                                            </span>

                                            <span className="price-amount">

                                                {price}

                                            </span>

                                            {pricingMode === "test" &&
                                                plan.name !== "STARTER" && (

                                                <span className="test-badge">

                                                    TEST

                                                </span>
                                            )}

                                            <span className="price-period">

                                                /{plan.interval}

                                            </span>

                                        </div>

                                        <ul className="sub-features">

                                            <li>
                                                <FaCheck />
                                                {" "}
                                                {plan.maxMockInterviews}
                                                {" "}AI Mock Interviews
                                            </li>

                                            <li>
                                                <FaCheck />
                                                {" "}
                                                {plan.maxResumeScans}
                                                {" "}Resume Scans
                                            </li>

                                            <li>
                                                <FaCheck />
                                                {" "}
                                                {plan.maxCodingProblems}
                                                {" "}Coding Problems
                                            </li>

                                            <li>
                                                <FaCheck />
                                                {" "}
                                                {plan.maxAptitudeQuestions}
                                                {" "}Aptitude Questions
                                            </li>

                                            {plan.includesAIHints && (

                                                <li>
                                                    <FaBolt />
                                                    {" "}AI Hints
                                                </li>
                                            )}

                                            {plan.includesAnalytics && (

                                                <li>
                                                    <FaChartLine />
                                                    {" "}Analytics
                                                </li>
                                            )}

                                            {plan.includesTier1Companies && (

                                                <li>
                                                    <FaCrown />
                                                    {" "}Tier-1 Company Prep
                                                </li>
                                            )}

                                            {plan.includesPriorityCompute && (

                                                <li>
                                                    <FaBolt />
                                                    {" "}Priority Compute
                                                </li>
                                            )}

                                        </ul>

                                        <button
                                            className={
                                                `subscribe-btn ${
                                                    plan.featured
                                                        ? "primary"
                                                        : ""
                                                }`
                                            }
                                            onClick={() =>
                                                !isDisabled &&
                                                handleSubscribe(plan)
                                            }
                                            disabled={
                                                processing ||
                                                isDisabled
                                            }
                                        >

                                            {
                                                isDisabled
                                                    ? "ADMIN - Unlimited Access"
                                                    : processing
                                                        ? "Processing..."
                                                        : plan.name === "STARTER"
                                                            ? "Continue with Starter"
                                                            : `Subscribe to ${plan.name}`
                                            }

                                        </button>

                                    </div>
                                );
                            })}

                        </div>

                    </div>
                )}

                {/* TEST MODE NOTE */}

                <div className="pricing-note">

                    <p>
                        Test Mode pricing:
                        Pro ₹1, Elite ₹2
                        (Production: Pro ₹399, Elite ₹799)
                    </p>

                    <p>
                        All payments processed securely
                        via Cashfree Test Mode.
                    </p>

                    <p>
                        Test card:
                        4111 1111 1111 1111
                    </p>

                </div>

            </div>

            <ConfirmModal
                isOpen={confirmModal.isOpen}
                message={confirmModal.message}
                onConfirm={confirmModal.onConfirm}
                onCancel={() => setConfirmModal({ isOpen: false, message: "", onConfirm: null })}
            />

        </div>
    );
}