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
  FaTag
} from "react-icons/fa";

import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Pricing() {
  const navigate = useNavigate();

  // Country/Locale detection: default to INR for Indian users, USD for international users
  const [currency, setCurrency] = useState(() => {
    try {
      const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || "";
      if (tz.includes("Calcutta") || tz.includes("Kolkata") || tz.includes("India")) {
        return "INR";
      }
      const langs = navigator.languages || [navigator.language || ""];
      const isIndiaLocale = langs.some((l) =>
        /-(IN|in)$|^hi|^ta|^te|^gu|^mr|^bn|^kn|^ml|^pa/i.test(l)
      );
      if (isIndiaLocale) {
        return "INR";
      }
    } catch (_) {}
    return "USD";
  });

  const [discountApplied, setDiscountApplied] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [selectedPlanNotice, setSelectedPlanNotice] = useState(null);
  const [referralInput, setReferralInput] = useState("");
  const [referralMessage, setReferralMessage] = useState("");
  const [referralError, setReferralError] = useState(false);

  useEffect(() => {
    updatePageSEO({
      title: "Pricing & Plans | Samprepix",
      description: "Transparent pricing for candidates and university placement teams. Choose between Starter, Pro, and Elite with full AI interview practice and coding arena access.",
      canonicalPath: "/pricing"
    });
    trackPageView("/pricing", "Pricing & Plans | Samprepix");
  }, []);

  // Demonstration codes for referral discounts
  const validReferralCodes = ["YOUR-REFERRAL-CODE", "SAMIR100", "SAMPREPIX100", "PROMO100"];

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
      const discountLabel = currency === "INR" ? "₹100 OFF" : "$1 OFF";
      setReferralMessage(`Referral applied successfully. ${discountLabel} unlocked on Pro and Elite.`);
      trackEvent("referral_applied", { currency, code: trimmed });
    } else {
      setReferralError(true);
      setDiscountApplied(false);
      setReferralMessage("Invalid referral code. Please check and try again.");
    }
  };

  const pasteReferral = async () => {
    try {
      const text = await navigator.clipboard.readText();
      setReferralInput(text.trim());
    } catch (_) {}
  };

  const handlePlanSelect = (planName) => {
    if (planName === "Starter") {
      const token = localStorage.getItem("token");
      if (token) {
        navigate("/dashboard");
      } else {
        navigate("/register");
      }
      return;
    }

    trackEvent("plan_intent_selected", { plan: planName, currency });
    setSelectedPlanNotice({
      plan: planName,
      message: `You selected the ${planName} Plan (${currency === "INR" ? (planName === "Pro" ? (discountApplied ? "₹299" : "₹399") : (discountApplied ? "₹699" : "₹799")) : (planName === "Pro" ? (discountApplied ? "$8" : "$9") : (discountApplied ? "$18" : "$19"))} / Month). Online checkout integration is coming soon. Early-access pricing is reserved for your account.`
    });
  };

  // Pricing Matrix Definition
  const pricingData = {
    INR: {
      symbol: "₹",
      starter: { price: "₹0", unit: "Forever", period: "/ Forever" },
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
      starter: { price: "$0", unit: "Forever", period: "/ Forever" },
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

  /* PAYMENT HISTORY VIEW */
  if (showHistory) {
    return (
      <div className="pricing-page">
        <Navbar />
        <div className="history-wrapper" style={{ maxWidth: "860px", margin: "40px auto", padding: "0 20px" }}>
          <button
            className="back-btn"
            onClick={() => setShowHistory(false)}
            aria-label="Back to Pricing Plans"
          >
            <FaArrowLeft /> Back to Plans
          </button>

          <div className="history-header" style={{ marginBottom: "24px", marginTop: "16px" }}>
            <h1 style={{ fontSize: "2rem", fontWeight: "700" }}>Payment History & Invoices</h1>
            <p style={{ color: "var(--text-muted, #64748b)", fontSize: "0.95rem" }}>
              Review your billing records, receipts, and subscription invoices.
            </p>
          </div>

          <div className="history-table" style={{ background: "var(--card-bg, #ffffff)", border: "1px solid var(--border-color, #e2e8f0)", borderRadius: "16px", overflow: "hidden" }}>
            <div className="history-row history-head" style={{ display: "grid", gridTemplateColumns: "1.5fr 1fr 1fr 1.2fr", padding: "14px 20px", fontWeight: "600", borderBottom: "1px solid var(--border-color, #e2e8f0)" }}>
              <span>Plan</span>
              <span>Status</span>
              <span>Amount</span>
              <span>Date</span>
            </div>
            <div style={{ textAlign: "center", padding: "48px 24px" }}>
              <FaHistory style={{ fontSize: "2.5rem", color: "var(--text-muted, #94a3b8)", marginBottom: "12px" }} />
              <h3 style={{ fontSize: "1.1rem", fontWeight: "600", marginBottom: "6px" }}>No Billing Transactions Recorded</h3>
              <p style={{ fontSize: "0.9rem", color: "var(--text-muted, #64748b)", maxWidth: "480px", margin: "auto" }}>
                Your account is currently active on the <strong>Starter Free Plan</strong>. Invoices and payment receipts will be automatically cataloged here once online purchases are enabled.
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

      {/* HERO SECTION */}
      <section className="pricing-hero">
        <span className="pricing-badge">
          ✦ Transparent Plans for Ambitious Engineers
        </span>

        <h1>
          Accelerate Your Placement Preparation
        </h1>

        <p>
          AI-driven mock interviews, interactive coding arena with 5,000+ problems,
          aptitude training with 22,000+ questions, and in-depth ATS resume optimization.
        </p>

        {/* CURRENCY TOGGLE */}
        <div
          className="currency-toggle-container"
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: "8px",
            marginTop: "24px",
            padding: "4px",
            background: "rgba(99, 102, 241, 0.08)",
            borderRadius: "999px",
            border: "1px solid rgba(99, 102, 241, 0.18)"
          }}
          role="radiogroup"
          aria-label="Currency Selector"
        >
          <span style={{ fontSize: "0.85rem", fontWeight: "600", color: "#4f46e5", padding: "0 10px" }}>
            <FaGlobe style={{ display: "inline", marginRight: "4px" }} /> Region:
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
              background: currency === "INR" ? "#4f46e5" : "transparent",
              color: currency === "INR" ? "#ffffff" : "#64748b"
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
              background: currency === "USD" ? "#4f46e5" : "transparent",
              color: currency === "USD" ? "#ffffff" : "#64748b"
            }}
            aria-checked={currency === "USD"}
            role="radio"
          >
            $ USD (International)
          </button>
        </div>
      </section>

      {/* PRICING CARDS GRID */}
      <section className="pricing-grid">
        {/* STARTER CARD */}
        <div className="pricing-card">
          <div className="plan-tag">Starter</div>

          <h2 className="pricing-card-headline">
            <span className="current-price">{currentPrices.starter.price}</span>
            <span className="price-period frequency-container">
              <span className="frequency-slash">/</span>
              <span className="frequency-unit">{currentPrices.starter.unit}</span>
            </span>
          </h2>

          <p className="card-subtitle">
            Essential placement preparation toolkit to practice algorithms and assess your interview readiness.
          </p>

          <div className="plan-features">
            <div>
              <FaCheck /> Standard AI mock interview sessions
            </div>
            <div>
              <FaCheck /> Full access to 5,050+ DSA problems
            </div>
            <div>
              <FaCheck /> 1,200+ SQL & Database sandboxes
            </div>
            <div>
              <FaCheck /> Real-time code execution in 50+ runtimes
            </div>
            <div>
              <FaCheck /> 5 ATS resume scans & structural score
            </div>
            <div>
              <FaCheck /> Aptitude practice modules (Quantitative & Verbal)
            </div>
          </div>

          <button
            className="plan-btn"
            onClick={() => handlePlanSelect("Starter")}
            aria-label="Start Free with Starter Plan"
          >
            Start Free
          </button>
        </div>

        {/* PRO CARD */}
        <div className="pricing-card pro-card">
          <div className="popular-badge">RECOMMENDED</div>

          <div className="plan-tag pro-tag">Pro</div>

          <h2 className="pricing-card-headline">
            {discountApplied ? (
              <div className="discounted-price-display">
                <span className="price-figures-wrap">
                  <del className="old-price" aria-label={`Original price ${currentPrices.pro.regular}`}>
                    {currentPrices.pro.regular}
                  </del>
                  <ins className="discounted-price" aria-label={`Current discounted price ${currentPrices.pro.discounted}`}>
                    {currentPrices.pro.discounted}
                  </ins>
                  <span className="price-period frequency-container">
                    <span className="frequency-slash">/</span>
                    <span className="frequency-unit">{currentPrices.pro.unit}</span>
                  </span>
                </span>
                <span className="discount-badge" aria-label={currentPrices.pro.offLabel}>
                  <FaTag className="discount-badge-icon" aria-hidden="true" />
                  <span className="discount-badge-amount">{currentPrices.pro.offAmount}</span>
                  <span className="discount-badge-suffix">OFF</span>
                </span>
              </div>
            ) : (
              <div className="regular-price-display">
                <span className="current-price">{currentPrices.pro.regular}</span>
                <span className="price-period frequency-container">
                  <span className="frequency-slash">/</span>
                  <span className="frequency-unit">{currentPrices.pro.unit}</span>
                </span>
              </div>
            )}
          </h2>

          <p className="card-subtitle">
            Comprehensive suite for engineering students actively interviewing with top product and IT firms.
          </p>

          <div className="plan-features">
            <div>
              <FaBolt /> Unlimited AI technical & behavioral interviews
            </div>
            <div>
              <FaBolt /> Complete ATS resume analyzer with role keyword matching
            </div>
            <div>
              <FaBolt /> Multi-level AI hints (Concept, Approach, Solution)
            </div>
            <div>
              <FaBolt /> Automatic GitHub solution sync & commit tracking
            </div>
            <div>
              <FaBolt /> In-depth performance analytics & readiness indicators
            </div>
            <div>
              <FaBolt /> 22,060 aptitude questions with detailed explanations
            </div>
          </div>

          <button
            className="plan-btn pro-btn"
            onClick={() => handlePlanSelect("Pro")}
            aria-label="Choose Pro Plan"
          >
            Choose Pro
          </button>
        </div>

        {/* ELITE CARD */}
        <div className="pricing-card">
          <div className="plan-tag elite-tag">Elite</div>

          <h2 className="pricing-card-headline">
            {discountApplied ? (
              <div className="discounted-price-display">
                <span className="price-figures-wrap">
                  <del className="old-price" aria-label={`Original price ${currentPrices.elite.regular}`}>
                    {currentPrices.elite.regular}
                  </del>
                  <ins className="discounted-price" aria-label={`Current discounted price ${currentPrices.elite.discounted}`}>
                    {currentPrices.elite.discounted}
                  </ins>
                  <span className="price-period frequency-container">
                    <span className="frequency-slash">/</span>
                    <span className="frequency-unit">{currentPrices.elite.unit}</span>
                  </span>
                </span>
                <span className="discount-badge" aria-label={currentPrices.elite.offLabel}>
                  <FaTag className="discount-badge-icon" aria-hidden="true" />
                  <span className="discount-badge-amount">{currentPrices.elite.offAmount}</span>
                  <span className="discount-badge-suffix">OFF</span>
                </span>
              </div>
            ) : (
              <div className="regular-price-display">
                <span className="current-price">{currentPrices.elite.regular}</span>
                <span className="price-period frequency-container">
                  <span className="frequency-slash">/</span>
                  <span className="frequency-unit">{currentPrices.elite.unit}</span>
                </span>
              </div>
            )}
          </h2>

          <p className="card-subtitle">
            Maximum acceleration with full company-specific question packs and priority AI evaluation.
          </p>

          <div className="plan-features">
            <div>
              <FaCrown /> Tier-1 company preparation tracks (FAANG/MNC focus)
            </div>
            <div>
              <FaCrown /> System design and architectural problem modules
            </div>
            <div>
              <FaCrown /> Advanced audio feedback and speech pacing analysis
            </div>
            <div>
              <FaCrown /> Full access to detailed test cases and hidden test verdicts
            </div>
            <div>
              <FaCrown /> Priority AI model compute & low-latency execution
            </div>
            <div>
              <FaCrown /> Peer comparison benchmark analytics
            </div>
          </div>

          <button
            className="plan-btn elite-btn"
            onClick={() => handlePlanSelect("Elite")}
            aria-label="Choose Elite Plan"
          >
            Choose Elite
          </button>
        </div>
      </section>

      {/* PLAN INTENT NOTICE MODAL (NO FAKE PAYMENTS) */}
      {selectedPlanNotice && (
        <div
          role="dialog"
          aria-labelledby="plan-notice-title"
          style={{
            maxWidth: "680px",
            margin: "0 auto 32px auto",
            padding: "20px 24px",
            borderRadius: "14px",
            background: "rgba(99, 102, 241, 0.08)",
            border: "1px solid rgba(99, 102, 241, 0.2)",
            display: "flex",
            alignItems: "flex-start",
            gap: "14px"
          }}
        >
          <FaInfoCircle style={{ color: "#4f46e5", fontSize: "1.4rem", flexShrink: 0, marginTop: "2px" }} />
          <div style={{ flex: 1 }}>
            <h4 id="plan-notice-title" style={{ margin: "0 0 6px 0", fontSize: "1rem", fontWeight: "700", color: "#1e1b4b" }}>
              Billing Gateway Notice
            </h4>
            <p style={{ margin: 0, fontSize: "0.9rem", lineHeight: "1.5", color: "#4338ca" }}>
              {selectedPlanNotice.message}
            </p>
          </div>
          <button
            type="button"
            onClick={() => setSelectedPlanNotice(null)}
            style={{
              background: "transparent",
              border: "none",
              cursor: "pointer",
              color: "#6366f1",
              fontWeight: "600",
              fontSize: "0.85rem"
            }}
          >
            Dismiss
          </button>
        </div>
      )}

      {/* REFERRAL REWARDS SECTION */}
      <section className="referral-section">
        <div className="referral-left">
          <span>
            <FaGift /> Referral Program
          </span>

          <h2>
            Have a Referral Code?
          </h2>

          <p>
            Enter your community referral code to unlock {currency === "INR" ? "₹100 OFF" : "$1 OFF"} on Pro and Elite subscription tiers.
          </p>
        </div>

        <div className="referral-right">
          <div className="referral-box">
            <input
              type="text"
              placeholder="e.g. YOUR-REFERRAL-CODE"
              value={referralInput}
              onChange={(e) => setReferralInput(e.target.value)}
              aria-label="Referral Code Input"
            />
            <button type="button" onClick={pasteReferral}>
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
              className={referralError ? "referral-error" : "referral-success"}
              role="alert"
            >
              {referralError ? <FaTimesCircle /> : <FaCheckCircle />}
              {referralMessage}
            </div>
          )}
        </div>
      </section>

      {/* PAYMENT HISTORY ACCESS */}
      <section className="payment-history-section">
        <div className="history-card">
          <div className="history-icon">
            <FaHistory />
          </div>

          <div className="history-content">
            <h3>Billing & Invoices</h3>
            <p>
              Access your historical transaction records, download PDF receipts, and view subscription details.
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

      {/* REUSABLE PREMIUM FOOTER */}
      <Footer />
    </div>
  );
}