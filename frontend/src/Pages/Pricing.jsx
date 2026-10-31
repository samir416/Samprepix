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
  FaSpinner,
  FaTimes,
  FaDownload
} from "react-icons/fa";
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import { API_BASE_URL } from "../config";
import { createOrder } from "../services/paymentService";
import {
  getActiveSubscription,
  getUpgradeQuote,
  createUpgradeOrder
} from "../services/subscriptionService";
import { toast } from "react-toastify";

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
  const [searchParams] = useSearchParams();

  useEffect(() => {
    if (searchParams.get("invoices") === "true" || searchParams.get("viewInvoices") === "true") {
      navigate("/billing-history");
    }
  }, [searchParams, navigate]);
  const [referralInput, setReferralInput] = useState("");
  const [referralMessage, setReferralMessage] = useState("");
  const [referralError, setReferralError] = useState(false);
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [checkoutPlan, setCheckoutPlan] = useState(null);
  const [checkoutError, setCheckoutError] = useState("");
  const [showCheckoutError, setShowCheckoutError] = useState(false);
  const [activeSubscription, setActiveSubscription] = useState(null);
  const [upgradeQuote, setUpgradeQuote] = useState(null);
  const [enableAutoPay, setEnableAutoPay] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return;

    getActiveSubscription()
      .then((sub) => {
        if (sub) {
          setActiveSubscription(sub);
          const plan = (sub.effectivePlan || sub.plan || "").toUpperCase();
          if (plan === "PRO") {
            getUpgradeQuote()
              .then((quote) => setUpgradeQuote(quote))
              .catch(() => {});
          }
        }
      })
      .catch(() => {});
  }, []);



  useEffect(() => {
    updatePageSEO({
      title: "Pricing & Plans | Samprepix",
      description:
        "Transparent monthly pricing for candidates and university placement teams. Choose between Starter, Pro, and Elite with AI interview practice, coding arena, aptitude training, and resume analysis.",
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
        `Referral applied successfully. ${discountLabel} unlocked on Pro and Elite monthly access.`
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
        unit: "Month",
        period: "/ Month"
      },
      elite: {
        regular: "₹799",
        discounted: "₹699",
        offAmount: "₹100",
        offLabel: "₹100 OFF",
        unit: "Month",
        period: "/ Month"
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
        unit: "Month",
        period: "/ Month"
      },
      elite: {
        regular: "$19",
        discounted: "$18",
        offAmount: "$1",
        offLabel: "$1 OFF",
        unit: "Month",
        period: "/ Month"
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

  const storedUser = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "{}");
    } catch (_) {
      return {};
    }
  })();
  const isAdmin = storedUser?.role === "ADMIN" || storedUser?.role === "ROLE_ADMIN";

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

    if (isAdmin) {
      toast.info("Admin accounts already have full Pro and Elite access.");
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
      const order = await createOrder(
        planId,
        currency,
        "cashfree",
        discountApplied
          ? referralInput.trim().toUpperCase()
          : null,
        window.location.origin + "/payment/verify",
        enableAutoPay
      );

      if (!order?.paymentSessionId) {
        throw new Error(
          "Payment session was not created. Please try again."
        );
      }

      const Cashfree = await loadCashfree();

      const cashfree = Cashfree({
        mode: "production"
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

  const handleUpgradeCheckout = async () => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login", {
        state: {
          redirectTo: "/pricing"
        }
      });
      return;
    }

    if (isAdmin) {
      toast.info("Admin accounts already have full Pro and Elite access.");
      return;
    }

    if (checkoutLoading) {
      return;
    }

    setCheckoutPlan("UPGRADE");
    setCheckoutLoading(true);
    setCheckoutError("");
    setShowCheckoutError(false);

    trackEvent("upgrade_checkout_started", {
      fromPlan: "PRO",
      toPlan: "ELITE",
      currency
    });

    try {
      const order = await createUpgradeOrder();

      if (!order?.paymentSessionId) {
        throw new Error(
          "Payment session was not created for upgrade. Please try again."
        );
      }

      const Cashfree = await loadCashfree();

      const cashfree = Cashfree({
        mode: "production"
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
        "Unable to start upgrade payment. Please try again.";

      setCheckoutError(message);
      setShowCheckoutError(true);

      trackEvent("upgrade_checkout_failed", {
        currency
      });
    } finally {
      setCheckoutLoading(false);
      setCheckoutPlan(null);
    }
  };

  const userActivePlan = (activeSubscription?.effectivePlan || activeSubscription?.plan || "").toUpperCase();
  const isProActive = userActivePlan === "PRO";
  const isEliteActive = userActivePlan === "ELITE";

  return (
    <div className="pricing-page">
      <Navbar />

      <section className="pricing-hero">
        <span className="pricing-badge">
          ✦ Transparent Monthly Plans
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
          className="currency-toggle-wrapper"
          role="radiogroup"
          aria-label="Currency Selector"
        >
          <span className="currency-toggle-label">
            <FaGlobe style={{ display: "inline", marginRight: "6px" }} />
            Region:
          </span>

          <div className="currency-segmented-track">
            <div
              className={`currency-sliding-pill ${currency === "USD" ? "slide-usd" : "slide-inr"}`}
              aria-hidden="true"
            />
            <button
              type="button"
              className={`currency-option-btn ${currency === "INR" ? "active" : ""}`}
              onClick={() => setCurrency("INR")}
              aria-checked={currency === "INR"}
              role="radio"
            >
              ₹ INR (India)
            </button>
            <button
              type="button"
              className={`currency-option-btn ${currency === "USD" ? "active" : ""}`}
              onClick={() => setCurrency("USD")}
              aria-checked={currency === "USD"}
              role="radio"
            >
              $ USD (International)
            </button>
          </div>
        </div>
      </section>

      {isAdmin && (
        <div
          role="status"
          style={{
            maxWidth: "860px",
            margin: "0 auto 28px",
            padding: "16px 20px",
            borderRadius: "14px",
            background: "rgba(99, 102, 241, 0.08)",
            border: "1px solid rgba(99, 102, 241, 0.25)",
            display: "flex",
            alignItems: "center",
            gap: "14px",
            color: "var(--text-color, #4338ca)"
          }}
        >
          <FaCrown style={{ color: "#6366f1", fontSize: "1.3rem", flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            <strong style={{ display: "block", fontSize: "0.95rem" }}>
              Admin Account Active
            </strong>
            <span style={{ fontSize: "0.88rem", opacity: 0.85 }}>
              Your account has unrestricted full access to all Pro and Elite features. Payment checkout is bypassed for administrators.
            </span>
          </div>
        </div>
      )}

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

      <div
        className="autopay-option-box"
        style={{
          maxWidth: "860px",
          margin: "0 auto 28px",
          padding: "16px 20px",
          borderRadius: "14px",
          background: "rgba(99, 102, 241, 0.05)",
          border: "1px solid rgba(99, 102, 241, 0.2)",
          display: "flex",
          alignItems: "center",
          gap: "14px"
        }}
      >
        <input
          type="checkbox"
          id="enableAutoPay"
          checked={enableAutoPay}
          onChange={(e) => setEnableAutoPay(e.target.checked)}
          style={{ width: "18px", height: "18px", cursor: "pointer", accentColor: "#6366f1" }}
        />
        <label htmlFor="enableAutoPay" style={{ flex: 1, cursor: "pointer", fontSize: "0.92rem", margin: 0 }}>
          <strong style={{ color: "var(--text-color, #1e293b)" }}>
            Enable AutoPay for future renewals (Recommended)
          </strong>
          <span style={{ display: "block", fontSize: "0.82rem", color: "var(--text-muted, #64748b)", marginTop: "2px" }}>
            Convenient automatic renewal every 30 days. You can disable AutoPay anytime under Billing History without losing access for your active paid period.
          </span>
        </label>
      </div>

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

            <div>
              <FaCheck /> Basic GitHub profile analysis & repository health checks
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
          {isProActive ? (
            <div className="popular-badge" style={{ background: "#10b981" }}>
              CURRENT PLAN
            </div>
          ) : (
            <div className="popular-badge">
              RECOMMENDED
            </div>
          )}

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

            <div>
              <FaBolt /> Comprehensive GitHub Profile Analyzer & recruiter view
            </div>

            <div>
              <FaBolt /> Personalized AI Career Roadmap & Progression
            </div>
          </div>

          {isProActive ? (
            <button
              className="plan-btn pro-btn"
              disabled
              style={{ opacity: 0.9, cursor: "default", background: "#10b981", borderColor: "#10b981", color: "#ffffff" }}
              aria-label="Current Active Plan"
            >
              <FaCheckCircle style={{ marginRight: "6px" }} />
              Current Plan
            </button>
          ) : isEliteActive ? (
            <button
              className="plan-btn pro-btn"
              disabled
              style={{ opacity: 0.7, cursor: "default" }}
              aria-label="Included in Elite"
            >
              <FaCheckCircle style={{ marginRight: "6px" }} />
              Included in Elite
            </button>
          ) : (
            <button
              className="plan-btn pro-btn"
              onClick={() => handlePlanCheckout("PRO")}
              disabled={checkoutLoading}
              aria-label="Get Pro Monthly Plan"
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
              ) : isAdmin ? (
                <>
                  <FaCrown />
                  Admin Access Active
                </>
              ) : (
                <>
                  <FaLock />
                  Choose Pro
                </>
              )}
            </button>
          )}
        </div>

        <div className="pricing-card">
          {isEliteActive ? (
            <div className="popular-badge" style={{ background: "#10b981" }}>
              CURRENT PLAN
            </div>
          ) : isProActive && upgradeQuote?.eligibleForUpgrade ? (
            <div className="popular-badge" style={{ background: "#8b5cf6" }}>
              UPGRADE OPTION
            </div>
          ) : null}
          <div className="plan-tag elite-tag">
            Elite
          </div>

          <h2 className="pricing-card-headline">
            {isProActive && upgradeQuote?.eligibleForUpgrade ? (
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
                    aria-label={`Upgrade difference price ₹${upgradeQuote.upgradePriceDifference}`}
                  >
                    {currency === "INR" ? `₹${upgradeQuote.upgradePriceDifference}` : `$${upgradeQuote.upgradePriceDifference}`}
                  </ins>
                  <span className="price-period frequency-container">
                    <span className="frequency-slash">
                      /
                    </span>
                    <span className="frequency-unit">
                      Upgrade Difference
                    </span>
                  </span>
                </span>
                <span className="discount-badge" style={{ background: "#8b5cf6" }}>
                  SAVE {currency === "INR" ? "₹399" : "$9"}
                </span>
              </div>
            ) : discountApplied ? (
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

          {isProActive && upgradeQuote?.eligibleForUpgrade ? (
            <p className="card-subtitle" style={{ color: "#8b5cf6", fontWeight: "600" }}>
              Pay only the ₹{upgradeQuote.upgradePriceDifference} difference for your remaining {upgradeQuote.remainingDays} days of Elite access. Retains your original cycle expiry ({upgradeQuote.currentExpiresAt ? new Date(upgradeQuote.currentExpiresAt).toLocaleDateString() : "current cycle"}).
            </p>
          ) : (
            <p className="card-subtitle">
              Maximum placement acceleration with company-focused
              preparation and priority AI evaluation.
            </p>
          )}

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

            <div>
              <FaCrown /> Priority AI Roadmap PDF Export & Milestone Verification
            </div>

            <div>
              <FaCrown /> Deepest GitHub analysis & advanced recruiter presentation
            </div>
          </div>

          {isEliteActive ? (
            <button
              className="plan-btn elite-btn"
              disabled
              style={{ opacity: 0.9, cursor: "default", background: "#10b981", borderColor: "#10b981", color: "#ffffff" }}
              aria-label="Current Active Elite Plan"
            >
              <FaCrown style={{ marginRight: "6px" }} />
              Current Plan
            </button>
          ) : isProActive && upgradeQuote?.eligibleForUpgrade ? (
            <button
              className="plan-btn elite-btn"
              onClick={handleUpgradeCheckout}
              disabled={checkoutLoading}
              style={{ background: "linear-gradient(135deg, #7c3aed, #4f46e5)", color: "#fff", border: "none" }}
              aria-label="Upgrade to Elite Plan"
            >
              {checkoutLoading && checkoutPlan === "UPGRADE" ? (
                <>
                  <FaSpinner style={{ animation: "spin 1s linear infinite", marginRight: "6px" }} />
                  Opening Upgrade Checkout...
                </>
              ) : (
                <>
                  <FaCrown style={{ marginRight: "6px" }} />
                  Upgrade to Elite — Pay {currency === "INR" ? `₹${upgradeQuote.upgradePriceDifference}` : `$${upgradeQuote.upgradePriceDifference}`}
                </>
              )}
            </button>
          ) : (
            <button
              className="plan-btn elite-btn"
              onClick={() => handlePlanCheckout("ELITE")}
              disabled={checkoutLoading}
              aria-label="Get Elite Monthly Plan"
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
              ) : isAdmin ? (
                <>
                  <FaCrown />
                  Admin Access Active
                </>
              ) : (
                <>
                  <FaLock />
                  Choose Elite
                </>
              )}
            </button>
          )}
        </div>
      </section>

      <div
        style={{
          maxWidth: "860px",
          margin: "24px auto 36px",
          padding: "16px 20px",
          borderRadius: "12px",
          background: "rgba(100, 116, 139, 0.06)",
          border: "1px solid rgba(100, 116, 139, 0.2)",
          textAlign: "center",
          fontSize: "0.86rem",
          color: "var(--text-muted, #64748b)",
          lineHeight: "1.5"
        }}
      >
        <FaLock style={{ display: "inline", marginRight: "6px", color: "#6366f1" }} />
        <strong>Strict Non-Refundable Policy:</strong> All subscription purchases and tier upgrade difference payments are final and non-refundable. Mid-cycle cancellations for refunds are not supported. AutoPay can be disabled anytime under <a href="/billing-history" style={{ color: "#6366f1", textDecoration: "underline", fontWeight: "600" }}>Billing History</a> to prevent future charges while keeping your paid access active until the end of your billing cycle.
      </div>

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
            on Pro and Elite monthly access.
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

          <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
            <button
              type="button"
              className="history-open-btn"
              onClick={() => navigate("/subscription/policy")}
              style={{
                background: "transparent",
                border: "1px solid rgba(255,255,255,0.2)",
                color: "inherit"
              }}
              aria-label="View Subscription and Refund Policy"
            >
              Subscription Policy
            </button>
            <button
              type="button"
              className="history-open-btn"
              onClick={() => navigate("/billing-history")}
              aria-label="View Invoices and Payment History"
            >
              View Invoices
            </button>
          </div>
        </div>
      </section>

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