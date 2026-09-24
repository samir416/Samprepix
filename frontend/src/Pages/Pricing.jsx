import "../styles/Pricing.css";
import Navbar from "../Components/Common/Navbar";
import Footer from "../Components/Common/Footer";
import { updatePageSEO } from "../utils/seo";
import { trackPageView, trackEvent } from "../utils/analytics";
import {
  FaCheck,
  FaBolt,
  FaCrown,
  FaGift,
  FaHistory,
  FaArrowLeft,
  FaCheckCircle,
  FaTimesCircle,
  FaInfoCircle,
  FaGlobe,
  FaTag,
  FaLock,
  FaSpinner
} from "react-icons/fa";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createTestOrder } from "../services/paymentService";

const loadCashfree = () =>
  new Promise((resolve, reject) => {
    if (window.Cashfree) {
      resolve(window.Cashfree);
      return;
    }

    const existingScript = document.querySelector(
      'script[src="https://sdk.cashfree.com/js/v3/cashfree.js"]'
    );

    if (existingScript) {
      existingScript.addEventListener("load", () =>
        resolve(window.Cashfree)
      );
      existingScript.addEventListener("error", () =>
        reject(new Error("Failed to load Cashfree SDK"))
      );
      return;
    }

    const script = document.createElement("script");
    script.src = "https://sdk.cashfree.com/js/v3/cashfree.js";
    script.async = true;

    script.onload = () => {
      if (window.Cashfree) {
        resolve(window.Cashfree);
      } else {
        reject(new Error("Cashfree SDK unavailable"));
      }
    };

    script.onerror = () =>
      reject(new Error("Failed to load Cashfree SDK"));

    document.body.appendChild(script);
  });

export default function Pricing() {
  const navigate = useNavigate();

  const [currency, setCurrency] = useState(() => {
    try {
      const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || "";

      if (
        tz.includes("Calcutta") ||
        tz.includes("Kolkata") ||
        tz.includes("India")
      ) {
        return "INR";
      }

      const langs = navigator.languages || [
        navigator.language || ""
      ];

      const isIndiaLocale = langs.some((language) =>
        /-(IN|in)$|^hi|^ta|^te|^gu|^mr|^bn|^kn|^ml|^pa/i.test(
          language
        )
      );

      if (isIndiaLocale) {
        return "INR";
      }
    } catch (_) {}

    return "USD";
  });

  const [discountApplied, setDiscountApplied] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [referralInput, setReferralInput] = useState("");
  const [referralMessage, setReferralMessage] = useState("");
  const [referralError, setReferralError] = useState(false);
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [checkoutPlan, setCheckoutPlan] = useState(null);
  const [checkoutError, setCheckoutError] = useState("");
  const [showCheckoutError, setShowCheckoutError] = useState(false);

  useEffect(() => {
    updatePageSEO({
      title: "Pricing & Plans | Samprepix",
      description:
        "Transparent pricing for candidates and university placement teams. Choose between Starter, Pro, and Elite with AI interview practice, coding arena, aptitude training, and resume analysis.",
      canonicalPath: "/pricing"
    });

    trackPageView(
      "/pricing",
      "Pricing & Plans | Samprepix"
    );
  }, []);

  const validReferralCodes = ["SAMIR100"];

  const applyReferral = () => {
    const trimmed = referralInput.trim().toUpperCase();

    if (!trimmed) {
      setReferralError(true);
      setDiscountApplied(false);
      setReferralMessage("Please enter a referral code.");
      return;
    }

    if (validReferralCodes.includes(trimmed)) {
      setReferralError(false);
      setDiscountApplied(true);

      const discountLabel =
        currency === "INR" ? "₹100 OFF" : "$1 OFF";

      setReferralMessage(
        `Referral applied successfully. ${discountLabel} unlocked on Pro and Elite lifetime access.`
      );

      trackEvent("referral_applied", {
        currency,
        code: trimmed
      });

      return;
    }

    setReferralError(true);
    setDiscountApplied(false);
    setReferralMessage(
      "Invalid referral code. Please check and try again."
    );
  };

  const pasteReferral = async () => {
    try {
      const text = await navigator.clipboard.readText();
      setReferralInput(text.trim());
    } catch (_) {}
  };

  const pricingData = {
    INR: {
      symbol: "₹",
      starter: {
        price: "₹0",
        unit: "Forever",
        period: "/ Forever"
      },
      pro: {
        regular: "₹399",
        discounted: "₹299",
        offAmount: "₹100",
        offLabel: "₹100 OFF",
        unit: "Lifetime",
        period: "/ Lifetime"
      },
      elite: {
        regular: "₹799",
        discounted: "₹699",
        offAmount: "₹100",
        offLabel: "₹100 OFF",
        unit: "Lifetime",
        period: "/ Lifetime"
      }
    },
    USD: {
      symbol: "$",
      starter: {
        price: "$0",
        unit: "Forever",
        period: "/ Forever"
      },
      pro: {
        regular: "$9",
        discounted: "$8",
        offAmount: "$1",
        offLabel: "$1 OFF",
        unit: "Lifetime",
        period: "/ Lifetime"
      },
      elite: {
        regular: "$19",
        discounted: "$18",
        offAmount: "$1",
        offLabel: "$1 OFF",
        unit: "Lifetime",
        period: "/ Lifetime"
      }
    }
  };

  const currentPrices = pricingData[currency];

  const getDisplayPrice = (plan) => {
    if (plan === "PRO") {
      return discountApplied
        ? currentPrices.pro.discounted
        : currentPrices.pro.regular;
    }

    if (plan === "ELITE") {
      return discountApplied
        ? currentPrices.elite.discounted
        : currentPrices.elite.regular;
    }

    return currentPrices.starter.price;
  };

  const handleStarter = () => {
    const token = localStorage.getItem("token");

    if (token) {
      navigate("/dashboard");
    } else {
      navigate("/register");
    }
  };

  const handlePlanCheckout = async (planId) => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login", {
        state: {
          redirectTo: "/pricing",
          selectedPlan: planId
        }
      });
      return;
    }

    if (checkoutLoading) {
      return;
    }

    setCheckoutPlan(planId);
    setCheckoutLoading(true);
    setCheckoutError("");
    setShowCheckoutError(false);

    trackEvent("payment_checkout_started", {
      plan: planId,
      currency,
      referralApplied: discountApplied
    });

    try {
      const order = await createTestOrder(
        planId,
        currency,
        "cashfree",
        discountApplied
          ? referralInput.trim().toUpperCase()
          : null
      );

      if (!order?.paymentSessionId) {
        throw new Error(
          "Payment session was not created. Please try again."
        );
      }

      const Cashfree = await loadCashfree();

      const cashfree = Cashfree({
        mode: "sandbox"
      });

      const checkoutResult = await cashfree.checkout({
        paymentSessionId: order.paymentSessionId,
        redirectTarget: "_modal"
      });

      if (checkoutResult?.error) {
        throw new Error(
          checkoutResult.error.message ||
            "Payment checkout could not be opened."
        );
      }

      if (checkoutResult?.redirect) {
        return;
      }

      if (order.cashfreeOrderId) {
        navigate(
          `/payment/verify?order_id=${encodeURIComponent(
            order.cashfreeOrderId
          )}`
        );
      }
    } catch (error) {
      const message =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        "Unable to start payment. Please try again.";

      setCheckoutError(message);
      setShowCheckoutError(true);

      trackEvent("payment_checkout_failed", {
        plan: planId,
        currency
      });
    } finally {
      setCheckoutLoading(false);
      setCheckoutPlan(null);
    }
  };

  if (showHistory) {
    return (
      <div className="pricing-page">
        <Navbar />

        <div
          className="history-wrapper"
          style={{
            maxWidth: "860px",
            margin: "40px auto",
            padding: "0 20px"
          }}
        >
          <button
            className="back-btn"
            onClick={() => setShowHistory(false)}
            aria-label="Back to Pricing Plans"
          >
            <FaArrowLeft /> Back to Plans
          </button>

          <div
            className="history-header"
            style={{
              marginBottom: "24px",
              marginTop: "16px"
            }}
          >
            <h1
              style={{
                fontSize: "2rem",
                fontWeight: "700"
              }}
            >
              Payment History & Invoices
            </h1>

            <p
              style={{
                color: "var(--text-muted, #64748b)",
                fontSize: "0.95rem"
              }}
            >
              Review your billing records, receipts, and
              subscription invoices.
            </p>
          </div>

          <div
            className="history-table"
            style={{
              background: "var(--card-bg, #ffffff)",
              border:
                "1px solid var(--border-color, #e2e8f0)",
              borderRadius: "16px",
              overflow: "hidden"
            }}
          >
            <div
              className="history-row history-head"
              style={{
                display: "grid",
                gridTemplateColumns:
                  "1.5fr 1fr 1fr 1.2fr",
                padding: "14px 20px",
                fontWeight: "600",
                borderBottom:
                  "1px solid var(--border-color, #e2e8f0)"
              }}
            >
              <span>Plan</span>
              <span>Status</span>
              <span>Amount</span>
              <span>Date</span>
            </div>

            <div
              style={{
                textAlign: "center",
                padding: "48px 24px"
              }}
            >
              <FaHistory
                style={{
                  fontSize: "2.5rem",
                  color:
                    "var(--text-muted, #94a3b8)",
                  marginBottom: "12px"
                }}
              />

              <h3
                style={{
                  fontSize: "1.1rem",
                  fontWeight: "600",
                  marginBottom: "6px"
                }}
              >
                Billing Records
              </h3>

              <p
                style={{
                  fontSize: "0.9rem",
                  color:
                    "var(--text-muted, #64748b)",
                  maxWidth: "480px",
                  margin: "auto"
                }}
              >
                Your completed Cashfree payments and
                invoices will appear here after the
                billing history API is connected.
              </p>
            </div>
          </div>
        </div>

        <Footer />
      </div>
    );
  }

  return (
    <div className="pricing-page">
      <Navbar />

      <section className="pricing-hero">
        <span className="pricing-badge">
          ✦ Transparent Plans for Ambitious Engineers
        </span>

        <h1>
          Accelerate Your Placement Preparation
        </h1>

        <p>
          AI-driven mock interviews, interactive coding
          arena with 5,000+ problems, aptitude training
          with 22,000+ questions, and in-depth ATS resume
          optimization.
        </p>

        <div
          className="currency-toggle-container"
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: "8px",
            marginTop: "24px",
            padding: "4px",
            background:
              "rgba(99, 102, 241, 0.08)",
            borderRadius: "999px",
            border:
              "1px solid rgba(99, 102, 241, 0.18)"
          }}
          role="radiogroup"
          aria-label="Currency Selector"
        >
          <span
            style={{
              fontSize: "0.85rem",
              fontWeight: "600",
              color: "#4f46e5",
              padding: "0 10px"
            }}
          >
            <FaGlobe
              style={{
                display: "inline",
                marginRight: "4px"
              }}
            />
            Region:
          </span>

          <button
            type="button"
            onClick={() => setCurrency("INR")}
            style={{
              padding: "6px 14px",
              borderRadius: "999px",
              border: "none",
              cursor: "pointer",
              fontWeight: "700",
              fontSize: "0.85rem",
              transition: "all 0.2s ease",
              background:
                currency === "INR"
                  ? "#4f46e5"
                  : "transparent",
              color:
                currency === "INR"
                  ? "#ffffff"
                  : "#64748b"
            }}
            aria-checked={currency === "INR"}
            role="radio"
          >
            ₹ INR (India)
          </button>

          <button
            type="button"
            onClick={() => setCurrency("USD")}
            style={{
              padding: "6px 14px",
              borderRadius: "999px",
              border: "none",
              cursor: "pointer",
              fontWeight: "700",
              fontSize: "0.85rem",
              transition: "all 0.2s ease",
              background:
                currency === "USD"
                  ? "#4f46e5"
                  : "transparent",
              color:
                currency === "USD"
                  ? "#ffffff"
                  : "#64748b"
            }}
            aria-checked={currency === "USD"}
            role="radio"
          >
            $ USD (International)
          </button>
        </div>
      </section>

      {showCheckoutError && (
        <div
          role="alert"
          style={{
            maxWidth: "860px",
            margin: "0 auto 28px",
            padding: "16px 20px",
            borderRadius: "14px",
            background:
              "rgba(239, 68, 68, 0.08)",
            border:
              "1px solid rgba(239, 68, 68, 0.2)",
            display: "flex",
            alignItems: "center",
            gap: "12px",
            color: "#b91c1c"
          }}
        >
          <FaTimesCircle />

          <div style={{ flex: 1 }}>
            <strong>
              Payment checkout could not start
            </strong>

            <div
              style={{
                marginTop: "4px",
                fontSize: "0.9rem"
              }}
            >
              {checkoutError}
            </div>
          </div>

          <button
            type="button"
            onClick={() => {
              setShowCheckoutError(false);
              setCheckoutError("");
            }}
            style={{
              border: "none",
              background: "transparent",
              cursor: "pointer",
              color: "#b91c1c",
              fontWeight: "700"
            }}
          >
            Dismiss
          </button>
        </div>
      )}

      <section className="pricing-grid">
        <div className="pricing-card">
          <div className="plan-tag">
            Starter
          </div>

          <h2 className="pricing-card-headline">
            <span className="current-price">
              {currentPrices.starter.price}
            </span>

            <span className="price-period frequency-container">
              <span className="frequency-slash">
                /
              </span>

              <span className="frequency-unit">
                {currentPrices.starter.unit}
              </span>
            </span>
          </h2>

          <p className="card-subtitle">
            Essential placement preparation toolkit to
            practice algorithms and assess your interview
            readiness.
          </p>

          <div className="plan-features">
            <div>
              <FaCheck /> Standard AI mock interview
              sessions
            </div>

            <div>
              <FaCheck /> Full access to 5,050+ DSA
              problems
            </div>

            <div>
              <FaCheck /> 1,200+ SQL & Database
              sandboxes
            </div>

            <div>
              <FaCheck /> Real-time code execution in
              50+ runtimes
            </div>

            <div>
              <FaCheck /> 5 ATS resume scans &
              structural score
            </div>

            <div>
              <FaCheck /> Aptitude practice modules
            </div>
          </div>

          <button
            className="plan-btn"
            onClick={handleStarter}
            aria-label="Start Free with Starter Plan"
          >
            Start Free
          </button>
        </div>

        <div className="pricing-card pro-card">
          <div className="popular-badge">
            RECOMMENDED
          </div>

          <div className="plan-tag pro-tag">
            Pro
          </div>

          <h2 className="pricing-card-headline">
            {discountApplied ? (
              <div className="discounted-price-display">
                <span className="price-figures-wrap">
                  <del
                    className="old-price"
                    aria-label={`Original price ${currentPrices.pro.regular}`}
                  >
                    {currentPrices.pro.regular}
                  </del>

                  <ins
                    className="discounted-price"
                    aria-label={`Current discounted price ${currentPrices.pro.discounted}`}
                  >
                    {currentPrices.pro.discounted}
                  </ins>

                  <span className="price-period frequency-container">
                    <span className="frequency-slash">
                      /
                    </span>

                    <span className="frequency-unit">
                      {currentPrices.pro.unit}
                    </span>
                  </span>
                </span>

                <span
                  className="discount-badge"
                  aria-label={currentPrices.pro.offLabel}
                >
                  <FaTag
                    className="discount-badge-icon"
                    aria-hidden="true"
                  />

                  <span className="discount-badge-amount">
                    {currentPrices.pro.offAmount}
                  </span>

                  <span className="discount-badge-suffix">
                    OFF
                  </span>
                </span>
              </div>
            ) : (
              <div className="regular-price-display">
                <span className="current-price">
                  {currentPrices.pro.regular}
                </span>

                <span className="price-period frequency-container">
                  <span className="frequency-slash">
                    /
                  </span>

                  <span className="frequency-unit">
                    {currentPrices.pro.unit}
                  </span>
                </span>
              </div>
            )}
          </h2>

          <p className="card-subtitle">
            Advanced placement preparation for students
            actively targeting software engineering and IT
            roles.
          </p>

          <div className="plan-features">
            <div>
              <FaBolt /> Unlimited AI technical &
              behavioral interviews
            </div>

            <div>
              <FaBolt /> Complete ATS resume analyzer
              with role keyword matching
            </div>

            <div>
              <FaBolt /> Multi-level AI hints
              (Concept, Approach, Solution)
            </div>

            <div>
              <FaBolt /> Automatic GitHub solution
              sync & commit tracking
            </div>

            <div>
              <FaBolt /> In-depth performance analytics
              & readiness indicators
            </div>

            <div>
              <FaBolt /> 22,060 aptitude questions
              with detailed explanations
            </div>
          </div>

          <button
            className="plan-btn pro-btn"
            onClick={() => handlePlanCheckout("PRO")}
            disabled={checkoutLoading}
            aria-label="Get Pro Lifetime Plan"
          >
            {checkoutLoading &&
            checkoutPlan === "PRO" ? (
              <>
                <FaSpinner
                  style={{
                    animation: "spin 1s linear infinite"
                  }}
                />
                Opening Checkout...
              </>
            ) : (
              <>
                <FaLock />
                Choose Pro
              </>
            )}
          </button>
        </div>

        <div className="pricing-card">
          <div className="plan-tag elite-tag">
            Elite
          </div>

          <h2 className="pricing-card-headline">
            {discountApplied ? (
              <div className="discounted-price-display">
                <span className="price-figures-wrap">
                  <del
                    className="old-price"
                    aria-label={`Original price ${currentPrices.elite.regular}`}
                  >
                    {currentPrices.elite.regular}
                  </del>

                  <ins
                    className="discounted-price"
                    aria-label={`Current discounted price ${currentPrices.elite.discounted}`}
                  >
                    {currentPrices.elite.discounted}
                  </ins>

                  <span className="price-period frequency-container">
                    <span className="frequency-slash">
                      /
                    </span>

                    <span className="frequency-unit">
                      {currentPrices.elite.unit}
                    </span>
                  </span>
                </span>

                <span
                  className="discount-badge"
                  aria-label={currentPrices.elite.offLabel}
                >
                  <FaTag
                    className="discount-badge-icon"
                    aria-hidden="true"
                  />

                  <span className="discount-badge-amount">
                    {currentPrices.elite.offAmount}
                  </span>

                  <span className="discount-badge-suffix">
                    OFF
                  </span>
                </span>
              </div>
            ) : (
              <div className="regular-price-display">
                <span className="current-price">
                  {currentPrices.elite.regular}
                </span>

                <span className="price-period frequency-container">
                  <span className="frequency-slash">
                    /
                  </span>

                  <span className="frequency-unit">
                    {currentPrices.elite.unit}
                  </span>
                </span>
              </div>
            )}
          </h2>

          <p className="card-subtitle">
            Maximum placement acceleration with lifetime
            company-focused preparation and priority AI
            evaluation.
          </p>

          <div className="plan-features">
            <div>
              <FaCrown /> Tier-1 company preparation
              tracks (FAANG/MNC focus)
            </div>

            <div>
              <FaCrown /> System design and architectural
              problem modules
            </div>

            <div>
              <FaCrown /> Advanced audio feedback and
              speech pacing analysis
            </div>

            <div>
              <FaCrown /> Full access to detailed test
              cases and hidden test verdicts
            </div>

            <div>
              <FaCrown /> Priority AI model compute &
              low-latency execution
            </div>

            <div>
              <FaCrown /> Peer comparison benchmark
              analytics
            </div>
          </div>

          <button
            className="plan-btn elite-btn"
            onClick={() => handlePlanCheckout("ELITE")}
            disabled={checkoutLoading}
            aria-label="Get Elite Lifetime Plan"
          >
            {checkoutLoading &&
            checkoutPlan === "ELITE" ? (
              <>
                <FaSpinner
                  style={{
                    animation: "spin 1s linear infinite"
                  }}
                />
                Opening Checkout...
              </>
            ) : (
              <>
                <FaLock />
                Choose Elite
              </>
            )}
          </button>
        </div>
      </section>

      <section className="referral-section">
        <div className="referral-left">
          <span>
            <FaGift /> Referral Program
          </span>

          <h2>
            Have a Referral Code?
          </h2>

          <p>
            Enter your community referral code to unlock{" "}
            {currency === "INR"
              ? "₹100 OFF"
              : "$1 OFF"}{" "}
            on Pro and Elite lifetime access.
          </p>
        </div>

        <div className="referral-right">
          <div className="referral-box">
            <input
              type="text"
              placeholder="e.g. YOUR-REFERRAL-CODE"
              value={referralInput}
              onChange={(e) =>
                setReferralInput(e.target.value)
              }
              aria-label="Referral Code Input"
            />

            <button
              type="button"
              onClick={pasteReferral}
            >
              Paste
            </button>
          </div>

          <button
            type="button"
            className="apply-btn"
            onClick={applyReferral}
          >
            Apply Referral Code
          </button>

          {referralMessage && (
            <div
              className={
                referralError
                  ? "referral-error"
                  : "referral-success"
              }
              role="alert"
            >
              {referralError ? (
                <FaTimesCircle />
              ) : (
                <FaCheckCircle />
              )}

              {referralMessage}
            </div>
          )}
        </div>
      </section>

      <section className="payment-history-section">
        <div className="history-card">
          <div className="history-icon">
            <FaHistory />
          </div>

          <div className="history-content">
            <h3>Billing & Invoices</h3>

            <p>
              Access your historical transaction records,
              download PDF receipts, and view subscription
              details.
            </p>
          </div>

          <button
            type="button"
            className="history-open-btn"
            onClick={() => setShowHistory(true)}
            aria-label="View Invoices and Payment History"
          >
            View Invoices
          </button>
        </div>
      </section>

      <div
        style={{
          maxWidth: "860px",
          margin: "0 auto 32px",
          padding: "14px 18px",
          borderRadius: "12px",
          background: "rgba(99, 102, 241, 0.06)",
          border:
            "1px solid rgba(99, 102, 241, 0.14)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: "8px",
          textAlign: "center",
          color: "var(--text-muted, #64748b)",
          fontSize: "0.82rem"
        }}
      >
        <FaInfoCircle />
        Secure checkout powered by Cashfree. Payment
        credentials are handled by the payment gateway.
      </div>

      <Footer />

      <style>{`
        @keyframes spin {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }

        .plan-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }

        .plan-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
        }

        .discounted-price {
          text-decoration: none;
        }
      `}</style>
    </div>
  );
}