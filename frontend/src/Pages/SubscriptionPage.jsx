import "../styles/admin.css";
import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import {
    getActiveSubscription,
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

    useEffect(() => {

        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }

        setCurrency(detectCurrency());

        loadData();

    }, [navigate]);

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

    const loadCashfree = () => {

        return new Promise((resolve, reject) => {

            if (window.Cashfree) {
                resolve(window.Cashfree);
                return;
            }

            const existingScript =
                document.querySelector(
                    'script[src="https://sdk.cashfree.com/js/v3/cashfree.js"]'
                );

            if (existingScript) {

                existingScript.addEventListener(
                    "load",
                    () => {

                        if (window.Cashfree) {
                            resolve(window.Cashfree);
                        } else {
                            reject(
                                new Error(
                                    "Cashfree SDK failed to initialize."
                                )
                            );
                        }

                    },
                    { once: true }
                );

                existingScript.addEventListener(
                    "error",
                    () => {
                        reject(
                            new Error(
                                "Cashfree SDK failed to load."
                            )
                        );
                    },
                    { once: true }
                );

                return;
            }

            const script =
                document.createElement("script");

            script.src =
                "https://sdk.cashfree.com/js/v3/cashfree.js";

            script.async = true;

            script.onload = () => {

                if (window.Cashfree) {
                    resolve(window.Cashfree);
                } else {
                    reject(
                        new Error(
                            "Cashfree SDK failed to initialize."
                        )
                    );
                }
            };

            script.onerror = () => {

                reject(
                    new Error(
                        "Cashfree SDK failed to load."
                    )
                );
            };

            document.body.appendChild(script);
        });
    };

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

            const order = await createTestOrder(
                plan.id,
                currency,
                "cashfree",
                referralCode
            );

            if (!order?.cashfreeOrderId) {
                throw new Error(
                    "Invalid Cashfree order response."
                );
            }

            if (!order?.paymentSessionId) {
                throw new Error(
                    "Cashfree payment session was not returned by the server."
                );
            }

            await initiateCashfreeCheckout(
                order,
                plan
            );

        } catch (err) {

            console.error(
                "Failed to create Cashfree order:",
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

    const initiateCashfreeCheckout = async (
        order,
        plan
    ) => {

        try {

            const Cashfree = await loadCashfree();

            const cashfree =
                Cashfree({
                    mode: "sandbox"
                });

            const result =
                await cashfree.checkout({
                    paymentSessionId:
                        order.paymentSessionId,
                    redirectTarget: "_modal"
                });

            if (result?.error) {

                try {
                    await markPaymentFailed(
                        order.cashfreeOrderId
                    );
                } catch (paymentError) {
                    console.error(
                        "Failed to mark payment as failed:",
                        paymentError
                    );
                }

                setError(
                    result.error.message ||
                    "Payment could not be completed."
                );

                setProcessing(false);
                return;
            }

            if (result?.redirect) {
                setProcessing(false);
                return;
            }

            await verifyAndActivate(
                order.cashfreeOrderId,
                plan
            );

        } catch (err) {

            console.error(
                "Cashfree checkout failed:",
                err
            );

            try {
                await markPaymentFailed(
                    order.cashfreeOrderId
                );
            } catch (paymentError) {
                console.error(
                    "Failed to mark payment as failed:",
                    paymentError
                );
            }

            setError(
                err?.message ||
                "Cashfree checkout failed. Please try again."
            );

            setProcessing(false);
        }
    };

    const verifyAndActivate = async (
        cashfreeOrderId,
        plan
    ) => {

        try {

            setError(null);

            const result =
                await verifyPayment({
                    order_id: cashfreeOrderId
                });

            const status =
                String(result?.status || "").toLowerCase();

            if (
                status === "success" ||
                status === "paid" ||
                status === "completed" ||
                status === "already_success" ||
                status === "already_processed"
            ) {

                setSuccess(true);

                await loadData();

                setTimeout(() => {
                    setSuccess(false);
                    navigate(
                        `/payment/verify?order_id=${encodeURIComponent(
                            cashfreeOrderId
                        )}`
                    );
                }, 1200);

                return;
            }

            if (status === "pending") {

                setProcessing(false);

                navigate(
                    `/payment/verify?order_id=${encodeURIComponent(
                        cashfreeOrderId
                    )}&status=pending`
                );

                return;
            }

            if (status === "failed") {

                setProcessing(false);

                navigate(
                    `/payment/verify?order_id=${encodeURIComponent(
                        cashfreeOrderId
                    )}&status=failed`
                );

                return;
            }

            setError(
                result?.message ||
                "Payment verification is still in progress."
            );

            setProcessing(false);

        } catch (err) {

            console.error(
                "Cashfree payment verification failed:",
                err
            );

            if (err?.response?.status === 202) {

                setProcessing(false);

                navigate(
                    `/payment/verify?order_id=${encodeURIComponent(
                        cashfreeOrderId
                    )}&status=pending`
                );

                return;
            }

            if (err?.response?.status === 402) {

                setProcessing(false);

                navigate(
                    `/payment/verify?order_id=${encodeURIComponent(
                        cashfreeOrderId
                    )}&status=failed`
                );

                return;
            }

            setError(
                err?.response?.data?.message ||
                err?.response?.data?.error ||
                err?.message ||
                "Payment verification failed. Please contact support."
            );

            setProcessing(false);
        }
    };

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
        }

        return "USD";
    };

    if (loading) {

        return (
            <div className="subscription-page">
                <div className="loading">
                    Loading...
                </div>
            </div>
        );
    }

    return (

        <div className="subscription-page">

            <div className="subscription-container">

                {success && (

                    <div className="success-banner">

                        <FaCheckCircle />

                        Payment successful. Premium access is being activated...

                    </div>
                )}

                {error && (

                    <div className="error-banner">

                        <FaExclamationTriangle />

                        {error}

                    </div>
                )}

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

                {activeSub &&
                    !capabilities?.isAdmin && (

                    <div className="active-subscription-card">

                        <h1>

                            <FaCheckCircle />

                            Active Plan

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

                                Access Until:{" "}

                                {
                                    activeSub.expiresAt
                                        ? new Date(
                                            activeSub.expiresAt
                                        ).toLocaleDateString()
                                        : "N/A"
                                }

                            </div>

                            <div className="sub-amount">

                                Paid:{" "}

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

                                {" "}

                                {
                                    activeSub.currency ||
                                    "INR"
                                }

                            </div>

                            <div
                                style={{
                                    marginTop: "12px",
                                    color: "#8a94a8",
                                    fontSize: "13px"
                                }}
                            >
                                One-time payment. Your premium access remains
                                active according to your plan period.
                            </div>

                        </div>

                    </div>
                )}

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

                                                One-time

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
                                                            : `Pay Once for ${plan.name}`
                                            }

                                        </button>

                                    </div>
                                );
                            })}

                        </div>

                    </div>
                )}

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
                        Cashfree Sandbox payment testing is enabled.
                    </p>

                </div>

            </div>

        </div>
    );
}