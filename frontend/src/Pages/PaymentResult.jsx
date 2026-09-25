import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import {
    FiCheckCircle,
    FiXCircle,
    FiHome,
    FiLogIn,
    FiDownload,
    FiLoader,
    FiShield,
    FiFileText,
    FiArrowRight,
    FiLock,
    FiZap,
    FiCode,
    FiBarChart2
} from "react-icons/fi";
import { API_BASE_URL } from "../config";
import { verifyPayment } from "../services/paymentService";
import { getActiveSubscription } from "../services/subscriptionService";
import Logo from "../assets/Logo.png";

export default function PaymentResult() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [state, setState] = useState("processing");
    const [message, setMessage] = useState(
        "Confirming your payment securely..."
    );
    const [invoice, setInvoice] = useState(null);
    const [downloading, setDownloading] = useState(false);
    const [showCongratulations, setShowCongratulations] = useState(false);
    const [congratulationStep, setCongratulationStep] = useState(1);
    const [activePlan, setActivePlan] = useState("PRO");

    const orderId = useMemo(
        () =>
            searchParams.get("order_id") ||
            searchParams.get("orderId") ||
            searchParams.get("cf_order_id") ||
            "",
        [searchParams]
    );

    const paymentStatus = useMemo(
        () =>
            (
                searchParams.get("payment_status") ||
                searchParams.get("status") ||
                ""
            ).toLowerCase(),
        [searchParams]
    );

    const getInvoice = useCallback(async () => {
        const token = localStorage.getItem("token");

        if (!token || !orderId) {
            return null;
        }

        const response = await axios.get(
            `${API_BASE_URL}/api/invoices`,
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

        const invoices = Array.isArray(response.data)
            ? response.data
            : [];

        return (
            invoices.find(
                (item) => item.cashfreeOrderId === orderId
            ) || null
        );
    }, [orderId]);

    const downloadInvoice = useCallback(
        async (invoiceId, invoiceNumber) => {
            const token = localStorage.getItem("token");

            if (!token || !invoiceId) {
                navigate("/login", {
                    state: {
                        from: "/payment/verify",
                        orderId
                    }
                });
                return;
            }

            try {
                setDownloading(true);

                const response = await axios.get(
                    `${API_BASE_URL}/api/invoices/${invoiceId}/pdf`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`
                        },
                        responseType: "blob"
                    }
                );

                const blob = new Blob(
                    [response.data],
                    { type: "application/pdf" }
                );

                const url = window.URL.createObjectURL(blob);
                const anchor = document.createElement("a");

                anchor.href = url;
                anchor.download = invoiceNumber
                    ? `Samprepix-Invoice-${invoiceNumber}.pdf`
                    : "Samprepix-Invoice.pdf";

                document.body.appendChild(anchor);
                anchor.click();
                anchor.remove();

                window.URL.revokeObjectURL(url);
            } catch {
                setMessage(
                    "Payment was verified successfully, but the invoice could not be downloaded automatically. You can download it from Billing."
                );
            } finally {
                setDownloading(false);
            }
        },
        [navigate, orderId]
    );

    const showSuccessFlow = useCallback(async () => {
        setState("success");
        setMessage(
            "Your payment has been securely verified and your premium access is active."
        );

        try {
            const sub = await getActiveSubscription();
            if (sub?.plan?.name) {
                setActivePlan(sub.plan.name.toUpperCase());
            }
        } catch {
            // keep default plan
        }

        let invoiceData = null;

        try {
            invoiceData = await getInvoice();

            if (invoiceData) {
                setInvoice(invoiceData);

                setTimeout(() => {
                    downloadInvoice(
                        invoiceData.id,
                        invoiceData.invoiceNumber
                    );
                }, 800);
            }
        } catch {
            setMessage(
                "Payment verified successfully. Your premium access is active. Your invoice is available from Billing."
            );
        }

        setTimeout(() => {
            setShowCongratulations(true);
            setCongratulationStep(1);
        }, 500);
    }, [downloadInvoice, getInvoice]);

    useEffect(() => {
        let cancelled = false;
        let retryTimer = null;
        let attempts = 0;

        const processPayment = async () => {
            if (
                paymentStatus === "failed" ||
                paymentStatus === "cancelled" ||
                paymentStatus === "canceled"
            ) {
                if (!cancelled) {
                    setState("failed");
                    setMessage(
                        "The payment was not completed. No premium access was granted."
                    );
                }
                return;
            }

            if (!orderId) {
                if (!cancelled) {
                    setState("failed");
                    setMessage(
                        "We could not identify this payment session. Please return to Samprepix and try again."
                    );
                }
                return;
            }

            const token = localStorage.getItem("token");

            if (!token) {
                if (!cancelled) {
                    setState("login-required");
                    setMessage(
                        "Your payment session was received. Sign in to securely verify the payment and receive your invoice."
                    );
                }
                return;
            }

            try {
                const verification = await verifyPayment({
                    order_id: orderId
                });

                if (cancelled) {
                    return;
                }

                const verificationStatus = String(
                    verification?.status || ""
                ).toLowerCase();

                if (
                    verificationStatus === "success" ||
                    verificationStatus === "paid" ||
                    verificationStatus === "completed" ||
                    verificationStatus === "already_processed"
                ) {
                    await showSuccessFlow();
                    return;
                }

                if (verificationStatus === "pending") {
                    setState("verification-pending");
                    setMessage(
                        "Your payment is being confirmed. We are checking the final payment status securely."
                    );

                    attempts += 1;

                    if (attempts < 12) {
                        retryTimer = setTimeout(
                            processPayment,
                            5000
                        );
                    } else {
                        setMessage(
                            "Your payment is still pending confirmation. Your premium access will be activated automatically after successful confirmation."
                        );
                    }

                    return;
                }

                if (
                    verificationStatus === "failed" ||
                    verificationStatus === "error"
                ) {
                    setState("failed");
                    setMessage(
                        verification?.message ||
                            "Payment verification failed. Your account has not been granted premium access."
                    );
                    return;
                }

                setState("verification-pending");
                setMessage(
                    "Payment status is being confirmed securely. Please wait while we verify the transaction."
                );

                attempts += 1;

                if (attempts < 12) {
                    retryTimer = setTimeout(
                        processPayment,
                        5000
                    );
                }
            } catch (error) {
                if (cancelled) {
                    return;
                }

                const responseStatus = error?.response?.status;
                const responseData = error?.response?.data;

                if (responseStatus === 401) {
                    setState("login-required");
                    setMessage(
                        "Your payment session was received. Sign in to securely verify the payment and access your invoice."
                    );
                    return;
                }

                const backendStatus = String(
                    responseData?.status || ""
                ).toLowerCase();

                if (backendStatus === "pending") {
                    setState("verification-pending");
                    setMessage(
                        "Your payment is still pending confirmation. We are continuing secure verification."
                    );

                    attempts += 1;

                    if (attempts < 12) {
                        retryTimer = setTimeout(
                            processPayment,
                            5000
                        );
                    }

                    return;
                }

                if (
                    backendStatus === "failed" ||
                    responseStatus === 402
                ) {
                    setState("failed");
                    setMessage(
                        responseData?.error ||
                            responseData?.message ||
                            "The payment was not completed. No premium access was granted."
                    );
                    return;
                }

                if (responseStatus === 400) {
                    setState("verification-pending");
                    setMessage(
                        "We could not complete the verification yet. Please wait while the payment status is reconciled."
                    );

                    attempts += 1;

                    if (attempts < 12) {
                        retryTimer = setTimeout(
                            processPayment,
                            5000
                        );
                    }

                    return;
                }

                if (
                    paymentStatus === "success" ||
                    paymentStatus === "paid" ||
                    paymentStatus === "completed"
                ) {
                    setState("verification-pending");
                    setMessage(
                        "Your payment was received by the payment gateway. Secure verification is still being completed."
                    );
                    return;
                }

                setState("failed");
                setMessage(
                    "We could not securely verify this payment. Please do not make another payment yet."
                );
            }
        };

        processPayment();

        return () => {
            cancelled = true;

            if (retryTimer) {
                clearTimeout(retryTimer);
            }
        };
    }, [
        orderId,
        paymentStatus,
        showSuccessFlow
    ]);

    useEffect(() => {
        if (
            !showCongratulations ||
            congratulationStep !== 1
        ) {
            return;
        }

        const timer = setTimeout(() => {
            setCongratulationStep(2);
        }, 2800);

        return () => clearTimeout(timer);
    }, [
        showCongratulations,
        congratulationStep
    ]);

    const content = {
        processing: {
            icon: (
                <FiLoader
                    size={58}
                    className="payment-result-spin"
                />
            ),
            title: "Confirming Payment",
            badge: "SECURE VERIFICATION"
        },
        success: {
            icon: <FiCheckCircle size={62} />,
            title: "Payment Successful",
            badge: "PAYMENT VERIFIED"
        },
        failed: {
            icon: <FiXCircle size={62} />,
            title: "Payment Unsuccessful",
            badge: "PAYMENT NOT COMPLETED"
        },
        "login-required": {
            icon: <FiShield size={62} />,
            title: "Sign In Required",
            badge: "SECURE ACCESS"
        },
        "verification-pending": {
            icon: (
                <FiLoader
                    size={58}
                    className="payment-result-spin"
                />
            ),
            title: "Payment Verification Pending",
            badge: "SECURE VERIFICATION"
        }
    };

    const current =
        content[state] || content.processing;

    return (
        <div className="payment-result-page">
            <style>
                {`
                    .payment-result-page {
                        min-height: 100vh;
                        width: 100%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 24px;
                        box-sizing: border-box;
                        background:
                            radial-gradient(circle at 15% 15%, rgba(76,110,245,.16), transparent 32%),
                            radial-gradient(circle at 85% 85%, rgba(124,58,237,.15), transparent 32%),
                            #070b14;
                        color: #fff;
                        overflow: hidden;
                    }

                    .payment-result-wrapper {
                        width: 100%;
                        max-width: 580px;
                    }

                    .payment-result-card {
                        background: linear-gradient(
                            145deg,
                            rgba(20,27,43,.97),
                            rgba(10,15,27,.99)
                        );
                        border: 1px solid rgba(255,255,255,.09);
                        border-radius: 28px;
                        padding: 42px 38px;
                        text-align: center;
                        box-shadow: 0 30px 80px rgba(0,0,0,.42);
                        backdrop-filter: blur(24px);
                    }

                    .payment-result-brand {
                        display: inline-flex;
                        align-items: center;
                        gap: 10px;
                        margin-bottom: 30px;
                    }

                    .payment-result-brand img {
                        width: 42px;
                        height: 42px;
                        object-fit: contain;
                    }

                    .payment-result-brand span {
                        font-size: 20px;
                        font-weight: 800;
                        letter-spacing: -.4px;
                    }

                    .payment-result-icon {
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        width: 92px;
                        height: 92px;
                        border-radius: 50%;
                        margin-bottom: 22px;
                    }

                    .payment-result-button {
                        min-height: 48px;
                        border: 0;
                        border-radius: 14px;
                        padding: 0 20px;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        gap: 9px;
                        font-size: 14px;
                        font-weight: 700;
                        cursor: pointer;
                        transition: .2s ease;
                    }

                    .payment-result-button:hover {
                        transform: translateY(-1px);
                    }

                    .payment-result-button:disabled {
                        opacity: .65;
                        cursor: not-allowed;
                        transform: none;
                    }

                    .payment-result-primary {
                        background: linear-gradient(135deg, #5b7cfa, #7657e8);
                        color: #fff;
                        box-shadow: 0 12px 30px rgba(91,124,250,.25);
                    }

                    .payment-result-secondary {
                        background: rgba(255,255,255,.06);
                        color: #e8ecf5;
                        border: 1px solid rgba(255,255,255,.1);
                    }

                    .payment-result-actions {
                        margin-top: 28px;
                        display: flex;
                        gap: 10px;
                        justify-content: center;
                    }

                    .payment-result-spin {
                        animation: paymentResultSpin 1.1s linear infinite;
                    }

                    .payment-result-invoice {
                        margin-top: 18px;
                        padding: 15px;
                        border-radius: 14px;
                        background: rgba(34,197,94,.07);
                        border: 1px solid rgba(34,197,94,.14);
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        text-align: left;
                    }

                    .payment-result-invoice-info {
                        min-width: 0;
                        flex: 1;
                    }

                    .payment-result-invoice-title {
                        font-size: 12px;
                        font-weight: 700;
                        color: #e7f8ed;
                    }

                    .payment-result-invoice-number {
                        margin-top: 3px;
                        font-size: 11px;
                        color: #8ea498;
                    }

                    .payment-result-modal-overlay {
                        position: fixed;
                        inset: 0;
                        z-index: 1000;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 20px;
                        background: rgba(2,5,12,.78);
                        backdrop-filter: blur(14px);
                    }

                    .payment-result-modal {
                        width: 100%;
                        max-width: 500px;
                        padding: 38px 32px 30px;
                        border-radius: 28px;
                        text-align: center;
                        background:
                            radial-gradient(circle at 50% 0%, rgba(91,124,250,.16), transparent 42%),
                            linear-gradient(145deg, #141b2b, #0b101c);
                        border: 1px solid rgba(255,255,255,.1);
                        box-shadow: 0 40px 100px rgba(0,0,0,.6);
                        animation: paymentModalIn .35s ease;
                    }

                    .payment-confetti-container {
                        position: relative;
                        width: 100%;
                        display: flex;
                        justify-content: center;
                        margin-bottom: 18px;
                    }

                    .payment-unlock-badge {
                        position: relative;
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        padding: 8px 18px;
                        border-radius: 9999px;
                        font-size: 13px;
                        font-weight: 800;
                        letter-spacing: .5px;
                        animation: badgePulse 2s infinite ease-in-out;
                    }

                    .payment-unlock-badge.pro {
                        background: linear-gradient(135deg, rgba(79,70,229,.22), rgba(99,102,241,.18));
                        border: 1.5px solid rgba(129,140,248,.6);
                        color: #c7d2fe;
                        box-shadow: 0 0 24px rgba(99,102,241,.4);
                    }

                    .payment-unlock-badge.elite {
                        background: linear-gradient(135deg, rgba(217,70,239,.22), rgba(168,85,247,.18));
                        border: 1.5px solid rgba(192,132,252,.6);
                        color: #f3e8ff;
                        box-shadow: 0 0 24px rgba(168,85,247,.4);
                    }

                    @keyframes badgePulse {
                        0%, 100% { transform: scale(1); filter: drop-shadow(0 0 10px rgba(99,102,241,.4)); }
                        50% { transform: scale(1.04); filter: drop-shadow(0 0 18px rgba(168,85,247,.6)); }
                    }

                    .payment-modal-icon {
                        width: 78px;
                        height: 78px;
                        margin: 0 auto 20px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        background: rgba(34,197,94,.12);
                        color: #4ade80;
                        border: 1px solid rgba(74,222,128,.2);
                    }

                    .payment-modal-kicker {
                        color: #7f9cff;
                        font-size: 10px;
                        font-weight: 800;
                        letter-spacing: 1.5px;
                        margin-bottom: 10px;
                    }

                    .payment-modal-title {
                        margin: 0;
                        font-size: clamp(25px, 6vw, 34px);
                        line-height: 1.15;
                        font-weight: 850;
                        letter-spacing: -.8px;
                    }

                    .payment-modal-text {
                        margin: 13px auto 0;
                        max-width: 390px;
                        color: #aeb8ca;
                        font-size: 14px;
                        line-height: 1.7;
                    }

                    .payment-benefits {
                        margin-top: 22px;
                        display: grid;
                        gap: 10px;
                        text-align: left;
                    }

                    .payment-benefit {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        padding: 12px 14px;
                        border-radius: 14px;
                        background: rgba(255,255,255,.045);
                        border: 1px solid rgba(255,255,255,.07);
                    }

                    .payment-benefit-icon {
                        width: 34px;
                        height: 34px;
                        flex: 0 0 34px;
                        border-radius: 10px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #9eb1ff;
                        background: rgba(91,124,250,.11);
                    }

                    .payment-benefit strong {
                        display: block;
                        font-size: 12px;
                        color: #f2f5fb;
                    }

                    .payment-benefit span {
                        display: block;
                        margin-top: 2px;
                        font-size: 10px;
                        color: #7e8ba1;
                    }

                    .payment-modal-progress {
                        display: flex;
                        justify-content: center;
                        gap: 7px;
                        margin: 22px 0 18px;
                    }

                    .payment-modal-dot {
                        width: 7px;
                        height: 7px;
                        border-radius: 50%;
                        background: rgba(255,255,255,.2);
                        transition: .25s ease;
                    }

                    .payment-modal-dot.active {
                        width: 22px;
                        border-radius: 20px;
                        background: #718cff;
                    }

                    .payment-modal-action {
                        width: 100%;
                        margin-top: 4px;
                    }

                    @keyframes paymentResultSpin {
                        from {
                            transform: rotate(0deg);
                        }
                        to {
                            transform: rotate(360deg);
                        }
                    }

                    @keyframes paymentModalIn {
                        from {
                            opacity: 0;
                            transform: translateY(18px) scale(.97);
                        }
                        to {
                            opacity: 1;
                            transform: translateY(0) scale(1);
                        }
                    }

                    @media (max-width: 560px) {
                        .payment-result-page {
                            padding: 14px;
                        }

                        .payment-result-card {
                            padding: 28px 20px;
                            border-radius: 22px;
                        }

                        .payment-result-actions {
                            flex-direction: column;
                        }

                        .payment-result-button {
                            width: 100%;
                        }

                        .payment-result-modal-overlay {
                            padding: 14px;
                        }

                        .payment-result-modal {
                            padding: 32px 20px 24px;
                            border-radius: 24px;
                        }
                    }
                `}
            </style>

            <div className="payment-result-wrapper">
                <div className="payment-result-card">
                    <div className="payment-result-brand">
                        <img src={Logo} alt="Samprepix" />
                        <span>Samprepix</span>
                    </div>

                    <div
                        className="payment-result-icon"
                        style={{
                            background:
                                state === "success"
                                    ? "rgba(34,197,94,.12)"
                                    : state === "failed"
                                      ? "rgba(239,68,68,.12)"
                                      : "rgba(91,124,250,.12)",
                            color:
                                state === "success"
                                    ? "#4ade80"
                                    : state === "failed"
                                      ? "#f87171"
                                      : "#8ba4ff"
                        }}
                    >
                        {current.icon}
                    </div>

                    <div
                        style={{
                            fontSize: "10px",
                            fontWeight: 800,
                            letterSpacing: "1.5px",
                            color:
                                state === "success"
                                    ? "#4ade80"
                                    : state === "failed"
                                      ? "#f87171"
                                      : "#8ba4ff",
                            marginBottom: "12px"
                        }}
                    >
                        {current.badge}
                    </div>

                    <h1
                        style={{
                            margin: 0,
                            fontSize: "clamp(26px, 7vw, 36px)",
                            lineHeight: 1.15,
                            fontWeight: 800,
                            letterSpacing: "-.7px"
                        }}
                    >
                        {current.title}
                    </h1>

                    <p
                        style={{
                            margin: "16px auto 0",
                            maxWidth: "440px",
                            color: "#aeb8ca",
                            fontSize: "14px",
                            lineHeight: 1.7
                        }}
                    >
                        {message}
                    </p>

                    {orderId && (
                        <div
                            style={{
                                marginTop: "22px",
                                padding: "12px 14px",
                                borderRadius: "12px",
                                background: "rgba(255,255,255,.035)",
                                border: "1px solid rgba(255,255,255,.07)",
                                textAlign: "left"
                            }}
                        >
                            <div
                                style={{
                                    fontSize: "10px",
                                    color: "#77839a",
                                    fontWeight: 700,
                                    textTransform: "uppercase",
                                    letterSpacing: ".8px"
                                }}
                            >
                                Payment Reference
                            </div>

                            <div
                                style={{
                                    marginTop: "5px",
                                    fontSize: "12px",
                                    color: "#dce3ef",
                                    wordBreak: "break-all",
                                    fontFamily: "monospace"
                                }}
                            >
                                {orderId}
                            </div>
                        </div>
                    )}

                    {state === "success" && invoice && (
                        <div className="payment-result-invoice">
                            <FiFileText size={24} color="#4ade80" />

                            <div className="payment-result-invoice-info">
                                <div className="payment-result-invoice-title">
                                    Invoice Ready
                                </div>

                                <div className="payment-result-invoice-number">
                                    {invoice.invoiceNumber}
                                </div>
                            </div>

                            <button
                                type="button"
                                className="payment-result-button payment-result-secondary"
                                style={{
                                    minHeight: "38px",
                                    padding: "0 12px"
                                }}
                                onClick={() =>
                                    downloadInvoice(
                                        invoice.id,
                                        invoice.invoiceNumber
                                    )
                                }
                                disabled={downloading}
                                aria-label="Download invoice"
                            >
                                {downloading ? (
                                    <FiLoader className="payment-result-spin" />
                                ) : (
                                    <FiDownload />
                                )}
                            </button>
                        </div>
                    )}

                    <div className="payment-result-actions">
                        {state === "success" && (
                            <button
                                type="button"
                                className="payment-result-button payment-result-primary"
                                onClick={() => navigate("/dashboard")}
                            >
                                Continue to Samprepix
                                <FiArrowRight />
                            </button>
                        )}

                        {(state === "login-required" ||
                            state === "verification-pending") && (
                            <button
                                type="button"
                                className="payment-result-button payment-result-primary"
                                onClick={() =>
                                    navigate("/login", {
                                        state: {
                                            from: "/payment/verify",
                                            orderId
                                        }
                                    })
                                }
                            >
                                <FiLogIn />
                                Sign In & Continue
                            </button>
                        )}

                        {state === "failed" && (
                            <button
                                type="button"
                                className="payment-result-button payment-result-primary"
                                onClick={() => navigate("/pricing")}
                            >
                                Try Again
                                <FiArrowRight />
                            </button>
                        )}

                        <button
                            type="button"
                            className="payment-result-button payment-result-secondary"
                            onClick={() => navigate("/")}
                        >
                            <FiHome />
                            Home
                        </button>
                    </div>

                    <div
                        style={{
                            marginTop: "26px",
                            paddingTop: "18px",
                            borderTop: "1px solid rgba(255,255,255,.06)",
                            fontSize: "10px",
                            color: "#69758a",
                            lineHeight: 1.6
                        }}
                    >
                        <FiShield
                            size={12}
                            style={{
                                verticalAlign: "middle",
                                marginRight: "5px"
                            }}
                        />
                        Secure 256-bit encrypted payment verification by Samprepix.
                    </div>
                </div>
            </div>

            {showCongratulations && (
                <div
                    className="payment-result-modal-overlay"
                    role="dialog"
                    aria-modal="true"
                >
                    <div className="payment-result-modal">
                        <div className="payment-modal-icon">
                            <FiCheckCircle size={40} />
                        </div>

                        {congratulationStep === 1 ? (
                            <>
                                <div className="payment-confetti-container">
                                    <div className={`payment-unlock-badge ${activePlan === "ELITE" ? "elite" : "pro"}`}>
                                        <span>{activePlan === "ELITE" ? "⚡ ELITE MEMBER UNLOCKED" : "⭐ PRO MEMBER UNLOCKED"}</span>
                                    </div>
                                </div>

                                <div className="payment-modal-kicker">
                                    MEMBERSHIP ACTIVATED
                                </div>

                                <h2 className="payment-modal-title">
                                    Welcome to {activePlan}! 🎉
                                </h2>

                                <p className="payment-modal-text">
                                    Your payment was verified securely. Your account now has unrestricted access to premium platform intelligence, unlimited tests, and personalized analytics.
                                </p>
                            </>
                        ) : (
                            <>
                                <div className="payment-modal-kicker">
                                    INCLUDED WITH YOUR {activePlan} PLAN
                                </div>

                                <h2 className="payment-modal-title">
                                    Everything You Need to Get Placed
                                </h2>

                                <p className="payment-modal-text">
                                    All advanced modules are unlocked on your account with authoritative server-side entitlement.
                                </p>

                                <div className="payment-benefits">
                                    <div className="payment-benefit">
                                        <div className="payment-benefit-icon">
                                            <FiCode size={17} />
                                        </div>
                                        <div>
                                            <strong>GitHub Profile Analyzer</strong>
                                            <span>
                                                0–100 scoring, tailored README generator & recruiter view
                                            </span>
                                        </div>
                                    </div>

                                    <div className="payment-benefit">
                                        <div className="payment-benefit-icon">
                                            <FiZap size={17} />
                                        </div>
                                        <div>
                                            <strong>AI Personalized Roadmap</strong>
                                            <span>
                                                8 curated tracks, progressive milestones & PDF exports
                                            </span>
                                        </div>
                                    </div>

                                    <div className="payment-benefit">
                                        <div className="payment-benefit-icon">
                                            <FiBarChart2 size={17} />
                                        </div>
                                        <div>
                                            <strong>Coding Arena & Piston Sandboxes</strong>
                                            <span>
                                                5,050+ DSA problems, multi-tiered hints & GitHub auto-sync
                                            </span>
                                        </div>
                                    </div>

                                    <div className="payment-benefit">
                                        <div className="payment-benefit-icon">
                                            <FiLock size={17} />
                                        </div>
                                        <div>
                                            <strong>AI Voice Mock Interviews & ATS Resume</strong>
                                            <span>
                                                Priority AI evaluation, tone feedback & keyword auditing
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </>
                        )}

                        <div className="payment-modal-progress">
                            <span
                                className={`payment-modal-dot ${
                                    congratulationStep === 1
                                        ? "active"
                                        : ""
                                }`}
                            />
                            <span
                                className={`payment-modal-dot ${
                                    congratulationStep === 2
                                        ? "active"
                                        : ""
                                }`}
                            />
                        </div>

                        {congratulationStep === 1 ? (
                            <button
                                type="button"
                                className="payment-result-button payment-result-primary payment-modal-action"
                                onClick={() =>
                                    setCongratulationStep(2)
                                }
                            >
                                See Your Benefits
                                <FiArrowRight />
                            </button>
                        ) : (
                            <button
                                type="button"
                                className="payment-result-button payment-result-primary payment-modal-action"
                                onClick={() => {
                                    setShowCongratulations(false);
                                    navigate("/dashboard");
                                }}
                            >
                                Go to Dashboard
                                <FiArrowRight />
                            </button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}